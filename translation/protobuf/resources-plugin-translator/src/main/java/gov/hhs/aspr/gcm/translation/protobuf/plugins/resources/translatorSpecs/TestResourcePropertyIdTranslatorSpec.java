package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.TestResourcePropertyIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractProtobufTranslatorSpec;
import plugins.resources.testsupport.TestResourcePropertyId;

public class TestResourcePropertyIdTranslatorSpec
        extends AbstractProtobufTranslatorSpec<TestResourcePropertyIdInput, TestResourcePropertyId> {

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
