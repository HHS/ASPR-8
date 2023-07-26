package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.testsupport.input.TestResourceIdInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import plugins.resources.testsupport.TestResourceId;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain TestResourceIdInput} and
 * {@linkplain TestResourceId}
 */
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
