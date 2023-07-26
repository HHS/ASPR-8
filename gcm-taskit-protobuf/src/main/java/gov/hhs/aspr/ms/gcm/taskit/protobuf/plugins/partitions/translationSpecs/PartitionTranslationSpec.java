package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.filters.input.FilterInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.LabelerInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.PartitionInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.partitions.support.Labeler;
import plugins.partitions.support.Partition;
import plugins.partitions.support.filters.Filter;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain PartitionInput} and
 * {@linkplain Partition}
 */
public class PartitionTranslationSpec extends ProtobufTranslationSpec<PartitionInput, Partition> {

    @Override
    protected Partition convertInputObject(PartitionInput inputObject) {
        Partition.Builder builder = Partition.builder();

        builder.setRetainPersonKeys(inputObject.getRetainPersonKeys());

        if (inputObject.hasFilter()) {
            Filter filter = this.translationEngine.convertObject(inputObject.getFilter());

            builder.setFilter(filter);
        }

        for (LabelerInput labelerInput : inputObject.getLabelersList()) {
            Labeler labeler = this.translationEngine.convertObject(labelerInput);

            builder.addLabeler(labeler);
        }

        return builder.build();
    }

    @Override
    protected PartitionInput convertAppObject(Partition appObject) {
        PartitionInput.Builder builder = PartitionInput.newBuilder();

        builder.setRetainPersonKeys(appObject.retainPersonKeys());

        if (appObject.getFilter().isPresent()) {
            FilterInput filterInput = this.translationEngine.convertObjectAsSafeClass(appObject.getFilter().get(),
                    Filter.class);

            builder.setFilter(filterInput);
        }

        for (Labeler labeler : appObject.getLabelers()) {
            LabelerInput labelerInput = this.translationEngine.convertObjectAsSafeClass(labeler, Labeler.class);

            builder.addLabelers(labelerInput);
        }

        return builder.build();
    }

    @Override
    public Class<Partition> getAppObjectClass() {
        return Partition.class;
    }

    @Override
    public Class<PartitionInput> getInputObjectClass() {
        return PartitionInput.class;
    }

}
