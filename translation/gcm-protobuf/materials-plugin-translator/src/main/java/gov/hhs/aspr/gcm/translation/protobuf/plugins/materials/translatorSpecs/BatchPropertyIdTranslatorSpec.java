package gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.BatchPropertyIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorSpec;
import plugins.materials.support.BatchPropertyId;

public class BatchPropertyIdTranslatorSpec extends ProtobufTranslatorSpec<BatchPropertyIdInput, BatchPropertyId> {

    @Override
    protected BatchPropertyId convertInputObject(BatchPropertyIdInput inputObject) {
        return this.translatorCore.getObjectFromAny(inputObject.getId());
    }

    @Override
    protected BatchPropertyIdInput convertAppObject(BatchPropertyId appObject) {
        return BatchPropertyIdInput.newBuilder().setId(this.translatorCore.getAnyFromObject(appObject)).build();
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
