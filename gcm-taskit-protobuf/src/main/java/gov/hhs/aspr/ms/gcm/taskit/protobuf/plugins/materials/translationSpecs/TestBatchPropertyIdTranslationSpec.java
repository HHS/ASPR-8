package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.testsupport.input.TestBatchPropertyIdInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import plugins.materials.testsupport.TestBatchPropertyId;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain TestBatchPropertyIdInput} and
 * {@linkplain TestBatchPropertyId}
 */
public class TestBatchPropertyIdTranslationSpec
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
