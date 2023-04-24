package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourceIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourceInitializationInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorSpec;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourceInitialization;

public class ResourceInitializationTranslatorSpec
        extends ProtobufTranslatorSpec<ResourceInitializationInput, ResourceInitialization> {

    @Override
    protected ResourceInitialization convertInputObject(ResourceInitializationInput inputObject) {
        ResourceId resourceId = this.translatorCore.convertObject(inputObject.getResourceId());
        long amount = inputObject.getAmount();
        return new ResourceInitialization(resourceId, amount);
    }

    @Override
    protected ResourceInitializationInput convertAppObject(ResourceInitialization appObject) {
        ResourceIdInput resourceIdInput = this.translatorCore.convertObjectAsSafeClass(appObject.getResourceId(), ResourceId.class);
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
