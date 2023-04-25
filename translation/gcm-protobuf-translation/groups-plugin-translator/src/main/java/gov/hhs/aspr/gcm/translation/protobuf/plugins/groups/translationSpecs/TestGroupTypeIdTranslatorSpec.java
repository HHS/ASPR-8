package gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.input.TestGroupTypeIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.groups.testsupport.TestGroupTypeId;

public class TestGroupTypeIdTranslatorSpec extends ProtobufTranslationSpec<TestGroupTypeIdInput, TestGroupTypeId> {

    @Override
    protected TestGroupTypeId convertInputObject(TestGroupTypeIdInput inputObject) {
        return TestGroupTypeId.valueOf(inputObject.name());
    }

    @Override
    protected TestGroupTypeIdInput convertAppObject(TestGroupTypeId appObject) {
        return TestGroupTypeIdInput.valueOf(appObject.name());
    }

    @Override
    public Class<TestGroupTypeId> getAppObjectClass() {
        return TestGroupTypeId.class;
    }

    @Override
    public Class<TestGroupTypeIdInput> getInputObjectClass() {
        return TestGroupTypeIdInput.class;
    }

}
