package gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.TestBatchPropertyIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.materials.testsupport.TestBatchPropertyId;

public class TestBatchPropertyIdTranslatorSpec
        extends ProtobufTranslationSpec<TestBatchPropertyIdInput, TestBatchPropertyId> {

    @Override
    protected TestBatchPropertyId convertInputObject(TestBatchPropertyIdInput inputObject) {
        return TestBatchPropertyId.valueOf(inputObject.name());
    }

    @Override
    protected TestBatchPropertyIdInput convertAppObject(TestBatchPropertyId appObject) {
        return TestBatchPropertyIdInput.valueOf(appObject.name());
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
