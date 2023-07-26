package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.filters.input.FilterInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.filters.Filter;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain FilterInput} and
 * {@linkplain Filter}
 */
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
