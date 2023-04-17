package gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.input.TestRandomGeneratorIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractProtobufTranslatorSpec;
import plugins.stochastics.testsupport.TestRandomGeneratorId;

public class TestRandomGeneratorIdTranslatorSpec
        extends AbstractProtobufTranslatorSpec<TestRandomGeneratorIdInput, TestRandomGeneratorId> {

    @Override
    protected TestRandomGeneratorId convertInputObject(TestRandomGeneratorIdInput inputObject) {
        return TestRandomGeneratorId.valueOf(inputObject.name());
    }

    @Override
    protected TestRandomGeneratorIdInput convertAppObject(TestRandomGeneratorId simObject) {
        return TestRandomGeneratorIdInput.valueOf(simObject.name());
    }

    @Override
    public TestRandomGeneratorIdInput getDefaultInstanceForInputObject() {
        return TestRandomGeneratorIdInput.forNumber(0);
    }

    @Override
    public Class<TestRandomGeneratorId> getAppObjectClass() {
        return TestRandomGeneratorId.class;
    }

    @Override
    public Class<TestRandomGeneratorIdInput> getInputObjectClass() {
        return TestRandomGeneratorIdInput.class;
    }

}
