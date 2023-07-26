package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.attributes.input.AttributeIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.partitions.testsupport.attributes.support.AttributeId;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain AttributeIdInput} and
 * {@linkplain AttributeId}
 */
public class AttributeIdTranslationSpec extends ProtobufTranslationSpec<AttributeIdInput, AttributeId> {

    @Override
    protected AttributeId convertInputObject(AttributeIdInput inputObject) {
        return this.translationEngine.getObjectFromAny(inputObject.getId());
    }

    @Override
    protected AttributeIdInput convertAppObject(AttributeId appObject) {
        return AttributeIdInput.newBuilder()
                .setId(this.translationEngine.getAnyFromObject(appObject)).build();
    }

    @Override
    public Class<AttributeId> getAppObjectClass() {
        return AttributeId.class;
    }

    @Override
    public Class<AttributeIdInput> getInputObjectClass() {
        return AttributeIdInput.class;
    }

}
