package gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.TestMaterialsProducerIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;
import plugins.materials.testsupport.TestMaterialsProducerId;

public class TestMaterialsProducerIdTranslatorSpec
        extends AbstractTranslatorSpec<TestMaterialsProducerIdInput, TestMaterialsProducerId> {

    @Override
    protected TestMaterialsProducerId convertInputObject(TestMaterialsProducerIdInput inputObject) {
        return TestMaterialsProducerId.valueOf(inputObject.name());
    }

    @Override
    protected TestMaterialsProducerIdInput convertAppObject(TestMaterialsProducerId simObject) {
        return TestMaterialsProducerIdInput.valueOf(simObject.name());
    }

    @Override
    public TestMaterialsProducerIdInput getDefaultInstanceForInputObject() {
        return TestMaterialsProducerIdInput.forNumber(0);
    }

    @Override
    public Class<TestMaterialsProducerId> getAppObjectClass() {
        return TestMaterialsProducerId.class;
    }

    @Override
    public Class<TestMaterialsProducerIdInput> getInputObjectClass() {
        return TestMaterialsProducerIdInput.class;
    }

}
