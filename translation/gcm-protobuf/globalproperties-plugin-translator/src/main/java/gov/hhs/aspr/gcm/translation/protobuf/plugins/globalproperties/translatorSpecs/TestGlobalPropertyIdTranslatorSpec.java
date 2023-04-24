package gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.input.TestGlobalPropertyIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorSpec;
import plugins.globalproperties.testsupport.TestGlobalPropertyId;

public class TestGlobalPropertyIdTranslatorSpec
        extends ProtobufTranslatorSpec<TestGlobalPropertyIdInput, TestGlobalPropertyId> {

    @Override
    protected TestGlobalPropertyId convertInputObject(TestGlobalPropertyIdInput inputObject) {
        return TestGlobalPropertyId.valueOf(inputObject.name());
    }

    @Override
    protected TestGlobalPropertyIdInput convertAppObject(TestGlobalPropertyId simObject) {
        return TestGlobalPropertyIdInput.valueOf(simObject.name());
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
