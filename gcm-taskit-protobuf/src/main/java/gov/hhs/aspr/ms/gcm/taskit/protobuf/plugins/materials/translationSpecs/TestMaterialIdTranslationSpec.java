package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.testsupport.input.TestMaterialIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.materials.testsupport.TestMaterialId;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain TestMaterialIdInput} and
 * {@linkplain TestMaterialId}
 */
public class TestMaterialIdTranslationSpec
        extends ProtobufTranslationSpec<TestMaterialIdInput, TestMaterialId> {

    @Override
    protected TestMaterialId convertInputObject(TestMaterialIdInput inputObject) {
        return TestMaterialId.valueOf(inputObject.name());
    }

    @Override
    protected TestMaterialIdInput convertAppObject(TestMaterialId appObject) {
        return TestMaterialIdInput.valueOf(appObject.name());
    }

    @Override
    public Class<TestMaterialId> getAppObjectClass() {
        return TestMaterialId.class;
    }

    @Override
    public Class<TestMaterialIdInput> getInputObjectClass() {
        return TestMaterialIdInput.class;
    }

}
