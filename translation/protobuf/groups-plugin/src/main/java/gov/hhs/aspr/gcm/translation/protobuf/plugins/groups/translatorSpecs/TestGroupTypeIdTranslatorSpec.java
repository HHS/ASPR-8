package gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.input.TestGroupTypeIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;
import plugins.groups.testsupport.TestGroupTypeId;

public class TestGroupTypeIdTranslatorSpec extends AbstractTranslatorSpec<TestGroupTypeIdInput, TestGroupTypeId> {

    @Override
    protected TestGroupTypeId convertInputObject(TestGroupTypeIdInput inputObject) {
        return TestGroupTypeId.valueOf(inputObject.name());
    }

    @Override
    protected TestGroupTypeIdInput convertAppObject(TestGroupTypeId simObject) {
        return TestGroupTypeIdInput.valueOf(simObject.name());
    }

    @Override
    public TestGroupTypeIdInput getDefaultInstanceForInputObject() {
        return TestGroupTypeIdInput.forNumber(0);
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
