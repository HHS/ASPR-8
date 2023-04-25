package gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.input.TestGlobalPropertyIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.globalproperties.testsupport.TestGlobalPropertyId;

public class TestGlobalPropertyIdTranslatorSpec
        extends ProtobufTranslationSpec<TestGlobalPropertyIdInput, TestGlobalPropertyId> {

    @Override
    protected TestGlobalPropertyId convertInputObject(TestGlobalPropertyIdInput inputObject) {
        return TestGlobalPropertyId.valueOf(inputObject.name());
    }

    @Override
    protected TestGlobalPropertyIdInput convertAppObject(TestGlobalPropertyId appObject) {
        return TestGlobalPropertyIdInput.valueOf(appObject.name());
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
