package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.TestResourceIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;
import plugins.resources.testsupport.TestResourceId;

public class TestResourceIdTranslatorSpec extends AbstractTranslatorSpec<TestResourceIdInput, TestResourceId> {

    @Override
    protected TestResourceId convertInputObject(TestResourceIdInput inputObject) {
        return TestResourceId.valueOf(inputObject.name());
    }

    @Override
    protected TestResourceIdInput convertAppObject(TestResourceId simObject) {
        return TestResourceIdInput.valueOf(simObject.name());
    }

    @Override
    public TestResourceIdInput getDefaultInstanceForInputObject() {
        return TestResourceIdInput.forNumber(0);
    }

    @Override
    public Class<TestResourceId> getAppObjectClass() {
        return TestResourceId.class;
    }

    @Override
    public Class<TestResourceIdInput> getInputObjectClass() {
        return TestResourceIdInput.class;
    }

}
