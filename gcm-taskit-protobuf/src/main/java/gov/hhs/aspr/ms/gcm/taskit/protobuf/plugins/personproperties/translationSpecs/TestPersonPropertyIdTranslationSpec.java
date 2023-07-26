package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.personproperties.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.personproperties.testsupport.input.TestPersonPropertyIdInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.testsupport.TestPersonPropertyId;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain TestPersonPropertyIdInput} and
 * {@linkplain TestPersonPropertyId}
 */
public class TestPersonPropertyIdTranslationSpec
        extends ProtobufTranslationSpec<TestPersonPropertyIdInput, TestPersonPropertyId> {

    @Override
    protected TestPersonPropertyId convertInputObject(TestPersonPropertyIdInput inputObject) {
        return TestPersonPropertyId.valueOf(inputObject.name());
    }

    @Override
    protected TestPersonPropertyIdInput convertAppObject(TestPersonPropertyId appObject) {
        return TestPersonPropertyIdInput.valueOf(appObject.name());
    }

    @Override
    public Class<TestPersonPropertyId> getAppObjectClass() {
        return TestPersonPropertyId.class;
    }

    @Override
    public Class<TestPersonPropertyIdInput> getInputObjectClass() {
        return TestPersonPropertyIdInput.class;
    }

}
