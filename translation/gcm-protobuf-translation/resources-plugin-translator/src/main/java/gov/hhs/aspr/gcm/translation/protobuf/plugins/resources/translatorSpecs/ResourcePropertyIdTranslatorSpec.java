package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourcePropertyIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.resources.support.ResourcePropertyId;

public class ResourcePropertyIdTranslatorSpec
        extends ProtobufTranslationSpec<ResourcePropertyIdInput, ResourcePropertyId> {

    @Override
    protected ResourcePropertyId convertInputObject(ResourcePropertyIdInput inputObject) {
        return this.translatorCore.getObjectFromAny(inputObject.getId());
    }

    @Override
    protected ResourcePropertyIdInput convertAppObject(ResourcePropertyId appObject) {
        return ResourcePropertyIdInput.newBuilder().setId(this.translatorCore.getAnyFromObject(appObject)).build();
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
