package gov.hhs.aspr.gcm.translation.plugins.resources.translatorSpecs;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.resources.input.ResourceIdInput;
import plugins.resources.support.ResourceId;

public class ResourceIdTranslatorSpec extends AObjectTranslatorSpec<ResourceIdInput, ResourceId> {

    @Override
    protected ResourceId convertInputObject(ResourceIdInput inputObject) {
        return this.translator.getObjectFromAny(inputObject.getId(), getAppObjectClass());
    }

    @Override
    protected ResourceIdInput convertAppObject(ResourceId simObject) {
        return ResourceIdInput.newBuilder().setId(this.translator.getAnyFromObject(simObject)).build();
    }

    @Override
    public ResourceIdInput getDefaultInstanceForInputObject() {
        return ResourceIdInput.getDefaultInstance();
    }

    @Override
    public Class<ResourceId> getAppObjectClass() {
        return ResourceId.class;
    }

    @Override
    public Class<ResourceIdInput> getInputObjectClass() {
        return ResourceIdInput.class;
    }

}
