package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.input.FalseFilterInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.partitions.support.filters.FalseFilter;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain FalseFilterInput} and
 * {@linkplain FalseFilter}
 */
public class FalseFilterTranslationSpec extends ProtobufTranslationSpec<FalseFilterInput, FalseFilter> {

    @Override
    protected FalseFilter convertInputObject(FalseFilterInput inputObject) {
        return new FalseFilter();
    }

    @Override
    protected FalseFilterInput convertAppObject(FalseFilter appObject) {
        return FalseFilterInput.newBuilder().build();
    }

    @Override
    public Class<FalseFilter> getAppObjectClass() {
        return FalseFilter.class;
    }

    @Override
    public Class<FalseFilterInput> getInputObjectClass() {
        return FalseFilterInput.class;
    }

}
