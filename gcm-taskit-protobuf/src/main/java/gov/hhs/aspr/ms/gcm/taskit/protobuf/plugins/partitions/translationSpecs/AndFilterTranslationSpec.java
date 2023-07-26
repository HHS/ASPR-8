package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.filters.input.AndFilterInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.filters.input.FilterInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.partitions.support.filters.AndFilter;
import plugins.partitions.support.filters.Filter;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain AndFilterInput} and
 * {@linkplain AndFilter}
 */
public class AndFilterTranslationSpec extends ProtobufTranslationSpec<AndFilterInput, AndFilter> {

    @Override
    protected AndFilter convertInputObject(AndFilterInput inputObject) {
        return new AndFilter(this.translationEngine.convertObject(inputObject.getA()),
                this.translationEngine.convertObject(inputObject.getB()));
    }

    @Override
    protected AndFilterInput convertAppObject(AndFilter appObject) {
        FilterInput a = this.translationEngine.convertObjectAsSafeClass(appObject.getFirstFilter(), Filter.class);
        FilterInput b = this.translationEngine.convertObjectAsSafeClass(appObject.getSecondFilter(), Filter.class);
        return AndFilterInput.newBuilder().setA(a).setB(b).build();
    }

    @Override
    public Class<AndFilter> getAppObjectClass() {
        return AndFilter.class;
    }

    @Override
    public Class<AndFilterInput> getInputObjectClass() {
        return AndFilterInput.class;
    }

}
