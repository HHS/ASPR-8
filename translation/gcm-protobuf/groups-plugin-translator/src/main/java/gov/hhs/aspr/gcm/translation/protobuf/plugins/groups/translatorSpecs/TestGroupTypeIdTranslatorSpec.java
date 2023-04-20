package gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.input.TestGroupTypeIdInput;
import gov.hhs.aspr.translation.protobuf.core.AbstractProtobufTranslatorSpec;
import plugins.groups.testsupport.TestGroupTypeId;

public class TestGroupTypeIdTranslatorSpec extends AbstractProtobufTranslatorSpec<TestGroupTypeIdInput, TestGroupTypeId> {

    @Override
    protected TestGroupTypeId convertInputObject(TestGroupTypeIdInput inputObject) {
        return TestGroupTypeId.valueOf(inputObject.name());
    }

    @Override
    protected TestGroupTypeIdInput convertAppObject(TestGroupTypeId simObject) {
        return TestGroupTypeIdInput.valueOf(simObject.name());
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
