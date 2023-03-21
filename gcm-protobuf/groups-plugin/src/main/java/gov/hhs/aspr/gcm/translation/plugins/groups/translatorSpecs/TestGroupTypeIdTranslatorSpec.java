package gov.hhs.aspr.gcm.translation.plugins.groups.translatorSpecs;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.groups.input.TestGroupTypeIdInput;
import plugins.groups.testsupport.TestGroupTypeId;

public class TestGroupTypeIdTranslatorSpec extends AObjectTranslatorSpec<TestGroupTypeIdInput, TestGroupTypeId> {

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
