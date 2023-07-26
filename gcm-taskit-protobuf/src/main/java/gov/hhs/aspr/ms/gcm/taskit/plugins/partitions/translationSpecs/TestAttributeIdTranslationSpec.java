package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.input.TestAttributeIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.partitions.testsupport.attributes.support.TestAttributeId;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain TestAttributeIdInput} and
 * {@linkplain TestAttributeId}
 */
public class TestAttributeIdTranslationSpec extends ProtobufTranslationSpec<TestAttributeIdInput, TestAttributeId> {

    @Override
    protected TestAttributeId convertInputObject(TestAttributeIdInput inputObject) {
        return TestAttributeId.valueOf(inputObject.name());
    }

    @Override
    protected TestAttributeIdInput convertAppObject(TestAttributeId appObject) {
        return TestAttributeIdInput.valueOf(appObject.name());
    }

    @Override
    public Class<TestAttributeId> getAppObjectClass() {
        return TestAttributeId.class;
    }

    @Override
    public Class<TestAttributeIdInput> getInputObjectClass() {
        return TestAttributeIdInput.class;
    }

}
