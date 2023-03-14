package gov.hhs.aspr.gcm.gcmprotobuf.plugins.resources.translators;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.gcmprotobuf.core.AbstractTranslator;
import plugins.resources.input.ResourceIdInput;
import plugins.resources.support.ResourceId;

public class ResourceIdTranslator extends AbstractTranslator<ResourceIdInput, ResourceId> {

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
