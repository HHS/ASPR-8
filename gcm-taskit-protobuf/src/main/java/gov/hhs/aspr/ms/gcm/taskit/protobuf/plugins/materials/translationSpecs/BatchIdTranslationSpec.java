package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.BatchIdInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.BatchId;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain BatchIdInput} and
 * {@linkplain BatchId}
 */
public class BatchIdTranslationSpec extends ProtobufTranslationSpec<BatchIdInput, BatchId> {

    @Override
    protected BatchId convertInputObject(BatchIdInput inputObject) {
        return new BatchId(inputObject.getId());
    }

    @Override
    protected BatchIdInput convertAppObject(BatchId appObject) {
        return BatchIdInput.newBuilder().setId(appObject.getValue()).build();
    }

    @Override
    public Class<BatchId> getAppObjectClass() {
        return BatchId.class;
    }

    @Override
    public Class<BatchIdInput> getInputObjectClass() {
        return BatchIdInput.class;
    }

}
