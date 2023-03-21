package gov.hhs.aspr.gcm.translation.plugins.resources.translatorSpecs;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.resources.input.ResourceIdInput;
import gov.hhs.aspr.gcm.translation.plugins.resources.input.ResourceInitializationInput;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourceInitialization;

public class ResourceInitializationTranslatorSpec
        extends AObjectTranslatorSpec<ResourceInitializationInput, ResourceInitialization> {

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
