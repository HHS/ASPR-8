package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourceIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourceInitializationInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourceInitialization;

public class ResourceInitializationTranslationSpec
        extends ProtobufTranslationSpec<ResourceInitializationInput, ResourceInitialization> {

    @Override
    protected ResourceInitialization convertInputObject(ResourceInitializationInput inputObject) {
        ResourceId resourceId = this.translationEnine.convertObject(inputObject.getResourceId());
        long amount = inputObject.getAmount();
        return new ResourceInitialization(resourceId, amount);
    }

    @Override
    protected ResourceInitializationInput convertAppObject(ResourceInitialization appObject) {
        ResourceIdInput resourceIdInput = this.translationEnine.convertObjectAsSafeClass(appObject.getResourceId(), ResourceId.class);
        return ResourceInitializationInput.newBuilder().setAmount(appObject.getAmount()).setResourceId(
                resourceIdInput).build();
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
