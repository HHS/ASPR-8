package gov.hhs.aspr.gcm.translation.plugins.resources.translatorSpecs;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.resources.input.TestResourcePropertyIdInput;
import plugins.resources.testsupport.TestResourcePropertyId;

public class TestResourcePropertyIdTranslatorSpec
        extends AObjectTranslatorSpec<TestResourcePropertyIdInput, TestResourcePropertyId> {

    @Override
    protected TestResourcePropertyId convertInputObject(TestResourcePropertyIdInput inputObject) {
        return TestResourcePropertyId.valueOf(inputObject.name());
    }

    @Override
    protected TestResourcePropertyIdInput convertAppObject(TestResourcePropertyId simObject) {
        return TestResourcePropertyIdInput.valueOf(simObject.name());
    }

    @Override
    public TestResourcePropertyIdInput getDefaultInstanceForInputObject() {
        return TestResourcePropertyIdInput.forNumber(0);
    }

    @Override
    public Class<TestResourcePropertyId> getAppObjectClass() {
        return TestResourcePropertyId.class;
    }

    @Override
    public Class<TestResourcePropertyIdInput> getInputObjectClass() {
        return TestResourcePropertyIdInput.class;
    }

}
