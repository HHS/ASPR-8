package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.BatchPropertyIdInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import plugins.materials.support.BatchPropertyId;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain BatchPropertyIdInput} and
 * {@linkplain BatchPropertyId}
 */
public class BatchPropertyIdTranslationSpec extends ProtobufTranslationSpec<BatchPropertyIdInput, BatchPropertyId> {

    @Override
    protected BatchPropertyId convertInputObject(BatchPropertyIdInput inputObject) {
        return this.translationEngine.getObjectFromAny(inputObject.getId());
    }

    @Override
    protected BatchPropertyIdInput convertAppObject(BatchPropertyId appObject) {
        return BatchPropertyIdInput.newBuilder().setId(this.translationEngine.getAnyFromObject(appObject)).build();
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
