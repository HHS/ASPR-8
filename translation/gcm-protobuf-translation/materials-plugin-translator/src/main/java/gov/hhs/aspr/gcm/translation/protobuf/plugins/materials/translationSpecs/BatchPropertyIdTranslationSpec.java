package gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.BatchPropertyIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.materials.support.BatchPropertyId;

public class BatchPropertyIdTranslationSpec extends ProtobufTranslationSpec<BatchPropertyIdInput, BatchPropertyId> {

    @Override
    protected BatchPropertyId convertInputObject(BatchPropertyIdInput inputObject) {
        return this.translationEnine.getObjectFromAny(inputObject.getId());
    }

    @Override
    protected BatchPropertyIdInput convertAppObject(BatchPropertyId appObject) {
        return BatchPropertyIdInput.newBuilder().setId(this.translationEnine.getAnyFromObject(appObject)).build();
    }

    @Override
    public Class<BatchPropertyId> getAppObjectClass() {
        return BatchPropertyId.class;
    }

    @Override
    public Class<BatchPropertyIdInput> getInputObjectClass() {
        return BatchPropertyIdInput.class;
    }

}
