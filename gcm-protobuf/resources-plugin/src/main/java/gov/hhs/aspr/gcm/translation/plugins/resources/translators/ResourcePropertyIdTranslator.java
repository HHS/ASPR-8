package gov.hhs.aspr.gcm.translation.plugins.resources.translators;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.translation.core.AbstractTranslator;
import gov.hhs.aspr.gcm.translation.plugins.resources.input.ResourcePropertyIdInput;
import plugins.resources.support.ResourcePropertyId;

public class ResourcePropertyIdTranslator extends AbstractTranslator<ResourcePropertyIdInput, ResourcePropertyId> {

    @Override
    protected ResourcePropertyId convertInputObject(ResourcePropertyIdInput inputObject) {
       return this.translator.getObjectFromAny(inputObject.getId(), getSimObjectClass());
    }

    @Override
    protected ResourcePropertyIdInput convertSimObject(ResourcePropertyId simObject) {
        return ResourcePropertyIdInput.newBuilder().setId(this.translator.getAnyFromObject(simObject)).build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return ResourcePropertyIdInput.getDescriptor();
    }

    @Override
    public ResourcePropertyIdInput getDefaultInstanceForInputObject() {
       return ResourcePropertyIdInput.getDefaultInstance();
    }

    @Override
    public Class<ResourcePropertyId> getSimObjectClass() {
        return ResourcePropertyId.class;
    }

    @Override
    public Class<ResourcePropertyIdInput> getInputObjectClass() {
       return ResourcePropertyIdInput.class;
    }
    
}
