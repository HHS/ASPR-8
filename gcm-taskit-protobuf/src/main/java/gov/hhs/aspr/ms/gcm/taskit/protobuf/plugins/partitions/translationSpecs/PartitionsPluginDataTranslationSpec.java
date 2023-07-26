package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.data.input.PartitionsPluginDataInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.partitions.datamanagers.PartitionsPluginData;

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

        builder.setRunContinuitySupport(inputObject.getSupportRunContinuity());

        return builder.build();
    }

    @Override
    protected PartitionsPluginDataInput convertAppObject(PartitionsPluginData appObject) {
        PartitionsPluginDataInput.Builder builder = PartitionsPluginDataInput.newBuilder();

        builder.setSupportRunContinuity(appObject.supportsRunContinuity());

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
