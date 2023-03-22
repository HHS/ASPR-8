package gov.hhs.aspr.gcm.translation.plugins.groups.translatorSpecs;

import gov.hhs.aspr.gcm.translation.core.AbstractTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.groups.input.TestGroupPropertyIdInput;
import plugins.groups.testsupport.TestGroupPropertyId;

public class TestGroupPropertyIdTranslatorSpec
        extends AbstractTranslatorSpec<TestGroupPropertyIdInput, TestGroupPropertyId> {

    @Override
    protected TestGroupPropertyId convertInputObject(TestGroupPropertyIdInput inputObject) {
        return TestGroupPropertyId.valueOf(inputObject.name());
    }

    @Override
    protected TestGroupPropertyIdInput convertAppObject(TestGroupPropertyId simObject) {
        return TestGroupPropertyIdInput.valueOf(simObject.name());
    }

    @Override
    public TestGroupPropertyIdInput getDefaultInstanceForInputObject() {
        return TestGroupPropertyIdInput.forNumber(0);
    }

    @Override
    public Class<TestGroupPropertyId> getAppObjectClass() {
        return TestGroupPropertyId.class;
    }

    @Override
    public Class<TestGroupPropertyIdInput> getInputObjectClass() {
        return TestGroupPropertyIdInput.class;
    }

}
