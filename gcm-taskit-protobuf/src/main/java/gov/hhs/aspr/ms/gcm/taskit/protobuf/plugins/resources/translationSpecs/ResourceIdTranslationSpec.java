package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import plugins.resources.support.ResourceId;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain ResourceIdInput} and
 * {@linkplain ResourceId}
 */
public class ResourceIdTranslationSpec extends ProtobufTranslationSpec<ResourceIdInput, ResourceId> {

    @Override
    protected ResourceId convertInputObject(ResourceIdInput inputObject) {
        return this.translationEngine.getObjectFromAny(inputObject.getId());
    }

    @Override
    protected ResourceIdInput convertAppObject(ResourceId appObject) {
        return ResourceIdInput.newBuilder().setId(this.translationEngine.getAnyFromObject(appObject)).build();
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
