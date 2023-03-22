package gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translatorSpecs;

import gov.hhs.aspr.gcm.translation.plugins.globalproperties.input.TestGlobalPropertyIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;
import plugins.globalproperties.testsupport.TestGlobalPropertyId;

public class TestGlobalPropertyIdTranslatorSpec
        extends AbstractTranslatorSpec<TestGlobalPropertyIdInput, TestGlobalPropertyId> {

    @Override
    protected TestGlobalPropertyId convertInputObject(TestGlobalPropertyIdInput inputObject) {
        return TestGlobalPropertyId.valueOf(inputObject.name());
    }

    @Override
    protected TestGlobalPropertyIdInput convertAppObject(TestGlobalPropertyId simObject) {
        return TestGlobalPropertyIdInput.valueOf(simObject.name());
    }

    @Override
    public TestGlobalPropertyIdInput getDefaultInstanceForInputObject() {
        return TestGlobalPropertyIdInput.forNumber(0);
    }

    @Override
    public Class<TestGlobalPropertyId> getAppObjectClass() {
        return TestGlobalPropertyId.class;
    }

    @Override
    public Class<TestGlobalPropertyIdInput> getInputObjectClass() {
        return TestGlobalPropertyIdInput.class;
    }

}
