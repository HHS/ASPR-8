package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourceIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorSpec;
import plugins.resources.support.ResourceId;

public class ResourceIdTranslatorSpec extends ProtobufTranslatorSpec<ResourceIdInput, ResourceId> {

    @Override
    protected ResourceId convertInputObject(ResourceIdInput inputObject) {
        return this.translatorCore.getObjectFromAny(inputObject.getId());
    }

    @Override
    protected ResourceIdInput convertAppObject(ResourceId simObject) {
        return ResourceIdInput.newBuilder().setId(this.translatorCore.getAnyFromObject(simObject)).build();
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
