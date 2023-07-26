package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.filters.input.TrueFilterInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import plugins.partitions.support.filters.TrueFilter;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain TrueFilterInput} and
 * {@linkplain TrueFilter}
 */
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
