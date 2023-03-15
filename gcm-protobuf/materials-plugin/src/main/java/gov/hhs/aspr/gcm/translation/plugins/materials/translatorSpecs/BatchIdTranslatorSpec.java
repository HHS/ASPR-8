package gov.hhs.aspr.gcm.translation.plugins.materials.translatorSpecs;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.materials.input.BatchIdInput;
import plugins.materials.support.BatchId;


public class BatchIdTranslatorSpec extends AObjectTranslatorSpec<BatchIdInput, BatchId> {

    @Override
    protected BatchId convertInputObject(BatchIdInput inputObject) {
        return new BatchId(inputObject.getId());
    }

    @Override
    protected BatchIdInput convertSimObject(BatchId simObject) {
        return BatchIdInput.newBuilder().setId(simObject.getValue()).build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return BatchIdInput.getDescriptor();
    }

    @Override
    public BatchIdInput getDefaultInstanceForInputObject() {
        return BatchIdInput.getDefaultInstance();
    }

    @Override
    public Class<BatchId> getSimObjectClass() {
        return BatchId.class;
    }

    @Override
    public Class<BatchIdInput> getInputObjectClass() {
        return BatchIdInput.class;
    }

}
