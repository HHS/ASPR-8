package gov.hhs.aspr.gcm.translation.plugins.personproperties.translatorSpecs;

import gov.hhs.aspr.gcm.translation.core.AbstractTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.personproperties.input.TestPersonPropertyIdInput;
import plugins.personproperties.testsupport.TestPersonPropertyId;

public class TestPersonPropertyIdTranslatorSpec
        extends AbstractTranslatorSpec<TestPersonPropertyIdInput, TestPersonPropertyId> {

    @Override
    protected TestPersonPropertyId convertInputObject(TestPersonPropertyIdInput inputObject) {
        return TestPersonPropertyId.valueOf(inputObject.name());
    }

    @Override
    protected TestPersonPropertyIdInput convertAppObject(TestPersonPropertyId simObject) {
        return TestPersonPropertyIdInput.valueOf(simObject.name());
    }

    @Override
    public TestPersonPropertyIdInput getDefaultInstanceForInputObject() {
        return TestPersonPropertyIdInput.forNumber(0);
    }

    @Override
    public Class<TestPersonPropertyId> getAppObjectClass() {
        return TestPersonPropertyId.class;
    }

    @Override
    public Class<TestPersonPropertyIdInput> getInputObjectClass() {
        return TestPersonPropertyIdInput.class;
    }

}
