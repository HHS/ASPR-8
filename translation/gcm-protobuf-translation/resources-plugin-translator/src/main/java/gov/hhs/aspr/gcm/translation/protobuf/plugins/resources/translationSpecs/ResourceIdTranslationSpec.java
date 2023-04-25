package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourceIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.resources.support.ResourceId;

public class ResourceIdTranslationSpec extends ProtobufTranslationSpec<ResourceIdInput, ResourceId> {

    @Override
    protected ResourceId convertInputObject(ResourceIdInput inputObject) {
        return this.translationEnine.getObjectFromAny(inputObject.getId());
    }

    @Override
    protected ResourceIdInput convertAppObject(ResourceId appObject) {
        return ResourceIdInput.newBuilder().setId(this.translationEnine.getAnyFromObject(appObject)).build();
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
