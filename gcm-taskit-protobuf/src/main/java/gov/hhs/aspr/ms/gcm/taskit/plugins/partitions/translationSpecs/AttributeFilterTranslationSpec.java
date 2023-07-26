package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.translationSpecs;

import com.google.protobuf.Any;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.input.AttributeFilterInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.input.AttributeIdInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.input.EqualityInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.partitions.support.Equality;
import plugins.partitions.testsupport.attributes.support.AttributeFilter;
import plugins.partitions.testsupport.attributes.support.AttributeId;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain AttributeFilterInput} and
 * {@linkplain AttributeFilter}
 */
public class AttributeFilterTranslationSpec extends ProtobufTranslationSpec<AttributeFilterInput, AttributeFilter> {

    @Override
    protected AttributeFilter convertInputObject(AttributeFilterInput inputObject) {
        AttributeId attributeId = this.translationEngine.convertObject(inputObject.getAttributeId());
        Equality equality = this.translationEngine.convertObject(inputObject.getEquality());
        Object value = this.translationEngine.getObjectFromAny(inputObject.getValue());

        return new AttributeFilter(attributeId, equality, value);
    }

    @Override
    protected AttributeFilterInput convertAppObject(AttributeFilter appObject) {
        AttributeIdInput attributeIdInput = this.translationEngine.convertObjectAsSafeClass(appObject.getAttributeId(),
                AttributeId.class);
        EqualityInput equalityInput = this.translationEngine.convertObjectAsSafeClass(appObject.getEquality(), Equality.class);
        Any value = this.translationEngine.getAnyFromObject(appObject.getValue());

        return AttributeFilterInput.newBuilder()
                .setAttributeId(attributeIdInput)
                .setEquality(equalityInput)
                .setValue(value)
                .build();
    }

    @Override
    public Class<AttributeFilter> getAppObjectClass() {
        return AttributeFilter.class;
    }

    @Override
    public Class<AttributeFilterInput> getInputObjectClass() {
        return AttributeFilterInput.class;
    }

}
