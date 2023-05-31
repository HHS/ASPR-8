package gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.input.FilterInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.partitions.support.filters.Filter;

public class FilterTranslationSpec extends ProtobufTranslationSpec<FilterInput, Filter> {

    @Override
    protected Filter convertInputObject(FilterInput inputObject) {
       return this.translationEngine.getObjectFromAny(inputObject.getFilter());
    }

    @Override
    protected FilterInput convertAppObject(Filter appObject) {
        return FilterInput.newBuilder().setFilter(this.translationEngine.getAnyFromObject(appObject)).build();
    }

    @Override
    public Class<Filter> getAppObjectClass() {
      return Filter.class;
    }

    @Override
    public Class<FilterInput> getInputObjectClass() {
       return FilterInput.class;
    }
    
}
