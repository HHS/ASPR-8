package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translatorSpecs;

import gov.hhs.aspr.gcm.translation.plugins.resources.input.ResourcePropertyIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;
import plugins.resources.support.ResourcePropertyId;

public class ResourcePropertyIdTranslatorSpec
        extends AbstractTranslatorSpec<ResourcePropertyIdInput, ResourcePropertyId> {

    @Override
    protected ResourcePropertyId convertInputObject(ResourcePropertyIdInput inputObject) {
        return this.translator.getObjectFromAny(inputObject.getId(), getAppObjectClass());
    }

    @Override
    protected ResourcePropertyIdInput convertAppObject(ResourcePropertyId simObject) {
        return ResourcePropertyIdInput.newBuilder().setId(this.translator.getAnyFromObject(simObject)).build();
    }

    @Override
    public ResourcePropertyIdInput getDefaultInstanceForInputObject() {
        return ResourcePropertyIdInput.getDefaultInstance();
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
