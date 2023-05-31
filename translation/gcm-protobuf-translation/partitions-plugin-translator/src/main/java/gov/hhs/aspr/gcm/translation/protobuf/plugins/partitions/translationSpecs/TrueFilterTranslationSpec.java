package gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.input.TrueFilterInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.partitions.support.filters.TrueFilter;

public class TrueFilterTranslationSpec extends ProtobufTranslationSpec<TrueFilterInput, TrueFilter> {

    @Override
    protected TrueFilter convertInputObject(TrueFilterInput inputObject) {
        return new TrueFilter();
    }

    @Override
    protected TrueFilterInput convertAppObject(TrueFilter appObject) {
        return TrueFilterInput.newBuilder().build();
    }

    @Override
    public Class<TrueFilter> getAppObjectClass() {
        return TrueFilter.class;
    }

    @Override
    public Class<TrueFilterInput> getInputObjectClass() {
        return TrueFilterInput.class;
    }

}
