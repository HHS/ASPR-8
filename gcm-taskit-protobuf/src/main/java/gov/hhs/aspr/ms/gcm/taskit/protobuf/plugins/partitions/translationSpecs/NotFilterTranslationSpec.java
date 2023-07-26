package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.filters.input.FilterInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.filters.input.NotFilterInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import plugins.partitions.support.filters.Filter;
import plugins.partitions.support.filters.NotFilter;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain NotFilterInput} and
 * {@linkplain NotFilter}
 */
public class NotFilterTranslationSpec extends ProtobufTranslationSpec<NotFilterInput, NotFilter> {

    @Override
    protected NotFilter convertInputObject(NotFilterInput inputObject) {
        return new NotFilter(this.translationEngine.convertObject(inputObject.getA()));
    }

    @Override
    protected NotFilterInput convertAppObject(NotFilter appObject) {
        FilterInput a = this.translationEngine.convertObjectAsSafeClass(appObject.getSubFilter(), Filter.class);
        return NotFilterInput.newBuilder().setA(a).build();
    }

    @Override
    public Class<NotFilter> getAppObjectClass() {
        return NotFilter.class;
    }

    @Override
    public Class<NotFilterInput> getInputObjectClass() {
        return NotFilterInput.class;
    }

}
