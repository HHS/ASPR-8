package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.TestResourcePropertyIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.resources.testsupport.TestResourcePropertyId;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain TestResourcePropertyIdInput} and
 * {@linkplain TestResourcePropertyId}
 */
public class TestResourcePropertyIdTranslationSpec
        extends ProtobufTranslationSpec<TestResourcePropertyIdInput, TestResourcePropertyId> {

    @Override
    protected TestResourcePropertyId convertInputObject(TestResourcePropertyIdInput inputObject) {
        return TestResourcePropertyId.valueOf(inputObject.name());
    }

    @Override
    protected TestResourcePropertyIdInput convertAppObject(TestResourcePropertyId appObject) {
        return TestResourcePropertyIdInput.valueOf(appObject.name());
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
