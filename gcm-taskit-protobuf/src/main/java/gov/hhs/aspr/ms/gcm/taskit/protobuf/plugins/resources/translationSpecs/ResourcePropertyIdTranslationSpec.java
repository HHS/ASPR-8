package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyIdInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourcePropertyId;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain ResourcePropertyIdInput} and
 * {@linkplain ResourcePropertyId}
 */
public class ResourcePropertyIdTranslationSpec
        extends ProtobufTranslationSpec<ResourcePropertyIdInput, ResourcePropertyId> {

    @Override
    protected ResourcePropertyId convertInputObject(ResourcePropertyIdInput inputObject) {
        return this.translationEngine.getObjectFromAny(inputObject.getId());
    }

    @Override
    protected ResourcePropertyIdInput convertAppObject(ResourcePropertyId appObject) {
        return ResourcePropertyIdInput.newBuilder().setId(this.translationEngine.getAnyFromObject(appObject)).build();
    }

    @Override
    public Class<ResourcePropertyId> getAppObjectClass() {
        return ResourcePropertyId.class;
    }

    @Override
    public Class<ResourcePropertyIdInput> getInputObjectClass() {
        return ResourcePropertyIdInput.class;
    }

}
