package gov.hhs.aspr.gcm.translation.plugins.resources.translatorSpecs;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.resources.input.ResourceIdInput;
import plugins.resources.support.ResourceId;

public class ResourceIdTranslator extends AObjectTranslatorSpec<ResourceIdInput, ResourceId> {

    @Override
    protected ResourceId convertInputObject(ResourceIdInput inputObject) {
       return this.translator.getObjectFromAny(inputObject.getId(), getSimObjectClass());
    }

    @Override
    protected ResourceIdInput convertSimObject(ResourceId simObject) {
        return ResourceIdInput.newBuilder().setId(this.translator.getAnyFromObject(simObject)).build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return ResourceIdInput.getDescriptor();
    }

    @Override
    public ResourceIdInput getDefaultInstanceForInputObject() {
       return ResourceIdInput.getDefaultInstance();
    }

    @Override
    public Class<ResourceId> getSimObjectClass() {
        return ResourceId.class;
    }

    @Override
    public Class<ResourceIdInput> getInputObjectClass() {
       return ResourceIdInput.class;
    }
    
}
