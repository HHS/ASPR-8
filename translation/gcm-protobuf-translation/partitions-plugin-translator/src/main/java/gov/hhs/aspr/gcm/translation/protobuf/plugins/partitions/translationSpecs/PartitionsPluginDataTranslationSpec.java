package gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs;

import com.google.protobuf.Any;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.input.PartitionInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.input.PartitionMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.input.PartitionsPluginDataInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.partitions.datamanagers.PartitionsPluginData;
import plugins.partitions.support.Partition;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain PartitionsPluginDataInput} and
 * {@linkplain PartitionsPluginData}
 */
public class PartitionsPluginDataTranslationSpec
        extends ProtobufTranslationSpec<PartitionsPluginDataInput, PartitionsPluginData> {

    @Override
    protected PartitionsPluginData convertInputObject(PartitionsPluginDataInput inputObject) {
        PartitionsPluginData.Builder builder = PartitionsPluginData.builder();

        for (PartitionMapInput partitionMapInput : inputObject.getPartitionsList()) {
            Partition partition = this.translationEngine.convertObject(partitionMapInput.getPartition());
            Object key = this.translationEngine.getObjectFromAny(partitionMapInput.getKey());

            builder.addPartition(key, partition);
        }

        return builder.build();
    }

    @Override
    protected PartitionsPluginDataInput convertAppObject(PartitionsPluginData appObject) {
        PartitionsPluginDataInput.Builder builder = PartitionsPluginDataInput.newBuilder();

        for (Object key : appObject.getPartitionKeys()) {
            Any anyKey = this.translationEngine.getAnyFromObject(key);
            PartitionInput partitionInput = this.translationEngine.convertObject(appObject.getPartition(key));

            builder.addPartitions(PartitionMapInput.newBuilder().setKey(anyKey).setPartition(partitionInput).build());
        }

        return builder.build();
    }

    @Override
    public Class<PartitionsPluginData> getAppObjectClass() {
        return PartitionsPluginData.class;
    }

    @Override
    public Class<PartitionsPluginDataInput> getInputObjectClass() {
        return PartitionsPluginDataInput.class;
    }

}
