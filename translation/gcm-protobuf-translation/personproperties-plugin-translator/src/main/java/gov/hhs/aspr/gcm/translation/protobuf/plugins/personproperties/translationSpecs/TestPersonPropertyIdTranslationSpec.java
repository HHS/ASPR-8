package gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.input.TestPersonPropertyIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.personproperties.testsupport.TestPersonPropertyId;

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
