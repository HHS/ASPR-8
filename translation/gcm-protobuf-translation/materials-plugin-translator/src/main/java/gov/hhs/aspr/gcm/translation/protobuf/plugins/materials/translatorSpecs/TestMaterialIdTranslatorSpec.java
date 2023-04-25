package gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.TestMaterialIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.materials.testsupport.TestMaterialId;

public class TestMaterialIdTranslatorSpec
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
