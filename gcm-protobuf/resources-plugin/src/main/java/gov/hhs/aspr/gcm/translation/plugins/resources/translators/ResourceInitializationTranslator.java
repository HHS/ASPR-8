package gov.hhs.aspr.gcm.translation.plugins.resources.translators;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.translation.core.AbstractTranslator;
import gov.hhs.aspr.gcm.translation.plugins.resources.input.ResourceIdInput;
import gov.hhs.aspr.gcm.translation.plugins.resources.input.ResourceInitializationInput;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourceInitialization;

public class ResourceInitializationTranslator
        extends AbstractTranslator<ResourceInitializationInput, ResourceInitialization> {

    @Override
    protected ResourceInitialization convertInputObject(ResourceInitializationInput inputObject) {
        ResourceId resourceId = this.translator.convertInputObject(inputObject.getResourceId(), ResourceId.class);
        long amount = inputObject.getAmount();
        return new ResourceInitialization(resourceId, amount);
    }

    @Override
    protected ResourceInitializationInput convertSimObject(ResourceInitialization simObject) {
        ResourceIdInput resourceIdInput = this.translator.convertSimObject(simObject.getResourceId(), ResourceId.class);
        return ResourceInitializationInput.newBuilder().setAmount(simObject.getAmount()).setResourceId(
                resourceIdInput).build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return ResourceInitializationInput.getDescriptor();
    }

    @Override
    public ResourceInitializationInput getDefaultInstanceForInputObject() {
        return ResourceInitializationInput.getDefaultInstance();
    }

    @Override
    public Class<ResourceInitialization> getSimObjectClass() {
        return ResourceInitialization.class;
    }

    @Override
    public Class<ResourceInitializationInput> getInputObjectClass() {
        return ResourceInitializationInput.class;
    }

}
