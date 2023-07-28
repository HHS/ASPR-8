package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.filters.input.FilterInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.filters.input.OrFilterInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.filters.Filter;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.filters.OrFilter;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain OrFilterInput} and
 * {@linkplain OrFilter}
 */
public class OrFilterTranslationSpec extends ProtobufTranslationSpec<OrFilterInput, OrFilter> {

    @Override
    protected OrFilter convertInputObject(OrFilterInput inputObject) {
        return new OrFilter(this.translationEngine.convertObject(inputObject.getA()),
                this.translationEngine.convertObject(inputObject.getB()));
    }

    @Override
    protected OrFilterInput convertAppObject(OrFilter appObject) {
        FilterInput a = this.translationEngine.convertObjectAsSafeClass(appObject.getFirstFilter(), Filter.class);
        FilterInput b = this.translationEngine.convertObjectAsSafeClass(appObject.getSecondFilter(), Filter.class);
        return OrFilterInput.newBuilder().setA(a).setB(b).build();
    }

    @Override
    public Class<OrFilter> getAppObjectClass() {
        return OrFilter.class;
    }

    @Override
    public Class<OrFilterInput> getInputObjectClass() {
        return OrFilterInput.class;
    }

}
