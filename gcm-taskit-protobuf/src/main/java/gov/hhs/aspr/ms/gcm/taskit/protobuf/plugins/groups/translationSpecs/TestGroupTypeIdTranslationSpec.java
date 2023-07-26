package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.testsupport.input.TestGroupTypeIdInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import plugins.groups.testsupport.TestGroupTypeId;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain TestGroupTypeIdInput} and
 * {@linkplain TestGroupTypeId}
 */
public class TestGroupTypeIdTranslationSpec extends ProtobufTranslationSpec<TestGroupTypeIdInput, TestGroupTypeId> {

    @Override
    protected TestGroupTypeId convertInputObject(TestGroupTypeIdInput inputObject) {
        return TestGroupTypeId.valueOf(inputObject.name());
    }

    @Override
    protected TestGroupTypeIdInput convertAppObject(TestGroupTypeId appObject) {
        return TestGroupTypeIdInput.valueOf(appObject.name());
    }

    @Override
    public Class<TestGroupTypeId> getAppObjectClass() {
        return TestGroupTypeId.class;
    }

    @Override
    public Class<TestGroupTypeIdInput> getInputObjectClass() {
        return TestGroupTypeIdInput.class;
    }

}
