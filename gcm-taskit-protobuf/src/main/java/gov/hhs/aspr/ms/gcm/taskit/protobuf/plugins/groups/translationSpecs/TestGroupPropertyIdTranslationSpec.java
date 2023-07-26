package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.testsupport.input.TestGroupPropertyIdInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import plugins.groups.testsupport.TestGroupPropertyId;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain TestGroupPropertyIdInput} and
 * {@linkplain TestGroupPropertyId}
 */
public class TestGroupPropertyIdTranslationSpec
        extends ProtobufTranslationSpec<TestGroupPropertyIdInput, TestGroupPropertyId> {

    @Override
    protected TestGroupPropertyId convertInputObject(TestGroupPropertyIdInput inputObject) {
        return TestGroupPropertyId.valueOf(inputObject.name());
    }

    @Override
    protected TestGroupPropertyIdInput convertAppObject(TestGroupPropertyId appObject) {
        return TestGroupPropertyIdInput.valueOf(appObject.name());
    }

    @Override
    public Class<TestGroupPropertyId> getAppObjectClass() {
        return TestGroupPropertyId.class;
    }

    @Override
    public Class<TestGroupPropertyIdInput> getInputObjectClass() {
        return TestGroupPropertyIdInput.class;
    }

}
