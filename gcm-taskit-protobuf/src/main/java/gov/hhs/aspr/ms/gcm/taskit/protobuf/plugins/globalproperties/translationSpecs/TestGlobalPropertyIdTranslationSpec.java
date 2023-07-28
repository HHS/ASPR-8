package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.globalproperties.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.globalproperties.testsupport.input.TestGlobalPropertyIdInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.testsupport.TestGlobalPropertyId;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain TestGlobalPropertyIdInput} and
 * {@linkplain TestGlobalPropertyId}
 */
public class TestGlobalPropertyIdTranslationSpec
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
