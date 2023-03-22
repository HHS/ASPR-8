package gov.hhs.aspr.gcm.translation.plugins.materials.translatorSpecs;

import gov.hhs.aspr.gcm.translation.core.AbstractTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.materials.input.TestMaterialIdInput;
import plugins.materials.testsupport.TestMaterialId;

public class TestMaterialIdTranslatorSpec
        extends AbstractTranslatorSpec<TestMaterialIdInput, TestMaterialId> {

    @Override
    protected TestMaterialId convertInputObject(TestMaterialIdInput inputObject) {
        return TestMaterialId.valueOf(inputObject.name());
    }

    @Override
    protected TestMaterialIdInput convertAppObject(TestMaterialId simObject) {
        return TestMaterialIdInput.valueOf(simObject.name());
    }

    @Override
    public TestMaterialIdInput getDefaultInstanceForInputObject() {
        return TestMaterialIdInput.forNumber(0);
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
