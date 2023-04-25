package gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.input.TestRandomGeneratorIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.stochastics.testsupport.TestRandomGeneratorId;

public class TestRandomGeneratorIdTranslatorSpec
        extends ProtobufTranslationSpec<TestRandomGeneratorIdInput, TestRandomGeneratorId> {

    @Override
    protected TestRandomGeneratorId convertInputObject(TestRandomGeneratorIdInput inputObject) {
        return TestRandomGeneratorId.valueOf(inputObject.name());
    }

    @Override
    protected TestRandomGeneratorIdInput convertAppObject(TestRandomGeneratorId appObject) {
        return TestRandomGeneratorIdInput.valueOf(appObject.name());
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
