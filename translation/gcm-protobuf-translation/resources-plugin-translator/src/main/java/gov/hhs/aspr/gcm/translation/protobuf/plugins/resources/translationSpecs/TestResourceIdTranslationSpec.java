package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.TestResourceIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.resources.testsupport.TestResourceId;

public class TestResourceIdTranslationSpec extends ProtobufTranslationSpec<TestResourceIdInput, TestResourceId> {

    @Override
    protected TestResourceId convertInputObject(TestResourceIdInput inputObject) {
        return TestResourceId.valueOf(inputObject.name());
    }

    @Override
    protected TestResourceIdInput convertAppObject(TestResourceId appObject) {
        return TestResourceIdInput.valueOf(appObject.name());
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
