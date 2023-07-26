package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceInitializationInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourceInitialization;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain ResourceInitializationInput} and
 * {@linkplain ResourceInitialization}
 */
public class ResourceInitializationTranslationSpec
        extends ProtobufTranslationSpec<ResourceInitializationInput, ResourceInitialization> {

    @Override
    protected ResourceInitialization convertInputObject(ResourceInitializationInput inputObject) {
        ResourceId resourceId = this.translationEngine.convertObject(inputObject.getResourceId());
        long amount = inputObject.getAmount();
        return new ResourceInitialization(resourceId, amount);
    }

    @Override
    protected ResourceInitializationInput convertAppObject(ResourceInitialization appObject) {
        return ResourceInitializationInput
                .newBuilder()
                .setAmount(appObject.getAmount())
                .setResourceId(this.translationEngine.getAnyFromObject(appObject.getResourceId())).build();
    }

    @Override
    public Class<ResourceInitialization> getAppObjectClass() {
        return ResourceInitialization.class;
    }

    @Override
    public Class<ResourceInitializationInput> getInputObjectClass() {
        return ResourceInitializationInput.class;
    }

}
