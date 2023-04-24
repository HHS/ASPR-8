package gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.BatchIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorSpec;
import plugins.materials.support.BatchId;

public class BatchIdTranslatorSpec extends ProtobufTranslatorSpec<BatchIdInput, BatchId> {

    @Override
    protected BatchId convertInputObject(BatchIdInput inputObject) {
        return new BatchId(inputObject.getId());
    }

    @Override
    protected BatchIdInput convertAppObject(BatchId simObject) {
        return BatchIdInput.newBuilder().setId(simObject.getValue()).build();
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
