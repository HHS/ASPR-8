package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourceIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourceInitializationInput;
import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractProtobufTranslatorSpec;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourceInitialization;

public class ResourceInitializationTranslatorSpec
        extends AbstractProtobufTranslatorSpec<ResourceInitializationInput, ResourceInitialization> {

    @Override
    protected ResourceInitialization convertInputObject(ResourceInitializationInput inputObject) {
        ResourceId resourceId = this.translator.convertInputObject(inputObject.getResourceId(), ResourceId.class);
        long amount = inputObject.getAmount();
        return new ResourceInitialization(resourceId, amount);
    }

    @Override
    protected ResourceInitializationInput convertAppObject(ResourceInitialization simObject) {
        ResourceIdInput resourceIdInput = this.translator.convertSimObject(simObject.getResourceId(), ResourceId.class);
        return ResourceInitializationInput.newBuilder().setAmount(simObject.getAmount()).setResourceId(
                resourceIdInput).build();
    }

    @Override
    public ResourceInitializationInput getDefaultInstanceForInputObject() {
        return ResourceInitializationInput.getDefaultInstance();
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
