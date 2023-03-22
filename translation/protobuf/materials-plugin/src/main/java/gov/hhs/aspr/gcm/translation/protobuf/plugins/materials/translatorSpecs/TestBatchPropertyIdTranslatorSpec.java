package gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.TestBatchPropertyIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;
import plugins.materials.testsupport.TestBatchPropertyId;

public class TestBatchPropertyIdTranslatorSpec
        extends AbstractTranslatorSpec<TestBatchPropertyIdInput, TestBatchPropertyId> {

    @Override
    protected TestBatchPropertyId convertInputObject(TestBatchPropertyIdInput inputObject) {
        return TestBatchPropertyId.valueOf(inputObject.name());
    }

    @Override
    protected TestBatchPropertyIdInput convertAppObject(TestBatchPropertyId simObject) {
        return TestBatchPropertyIdInput.valueOf(simObject.name());
    }

    @Override
    public TestBatchPropertyIdInput getDefaultInstanceForInputObject() {
        return TestBatchPropertyIdInput.forNumber(0);
    }

    @Override
    public Class<TestBatchPropertyId> getAppObjectClass() {
        return TestBatchPropertyId.class;
    }

    @Override
    public Class<TestBatchPropertyIdInput> getInputObjectClass() {
        return TestBatchPropertyIdInput.class;
    }

}