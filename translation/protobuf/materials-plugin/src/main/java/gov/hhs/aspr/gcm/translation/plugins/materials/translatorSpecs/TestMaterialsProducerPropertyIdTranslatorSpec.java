package gov.hhs.aspr.gcm.translation.plugins.materials.translatorSpecs;

import gov.hhs.aspr.gcm.translation.core.AbstractTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.materials.input.TestMaterialsProducerPropertyIdInput;
import plugins.materials.testsupport.TestMaterialsProducerPropertyId;

public class TestMaterialsProducerPropertyIdTranslatorSpec
        extends AbstractTranslatorSpec<TestMaterialsProducerPropertyIdInput, TestMaterialsProducerPropertyId> {

    @Override
    protected TestMaterialsProducerPropertyId convertInputObject(TestMaterialsProducerPropertyIdInput inputObject) {
        return TestMaterialsProducerPropertyId.valueOf(inputObject.name());
    }

    @Override
    protected TestMaterialsProducerPropertyIdInput convertAppObject(TestMaterialsProducerPropertyId simObject) {
        return TestMaterialsProducerPropertyIdInput.valueOf(simObject.name());
    }

    @Override
    public TestMaterialsProducerPropertyIdInput getDefaultInstanceForInputObject() {
        return TestMaterialsProducerPropertyIdInput.forNumber(0);
    }

    @Override
    public Class<TestMaterialsProducerPropertyId> getAppObjectClass() {
        return TestMaterialsProducerPropertyId.class;
    }

    @Override
    public Class<TestMaterialsProducerPropertyIdInput> getInputObjectClass() {
        return TestMaterialsProducerPropertyIdInput.class;
    }

}