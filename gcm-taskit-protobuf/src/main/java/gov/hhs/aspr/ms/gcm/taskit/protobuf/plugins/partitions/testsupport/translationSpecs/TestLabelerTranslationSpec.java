package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.testsupport.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.testsupport.input.TestLabelerInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.testsupport.TestLabeler;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain TestLabelerInput} and
 * {@linkplain TestLabeler}
 */
public class TestLabelerTranslationSpec extends ProtobufTranslationSpec<TestLabelerInput, TestLabeler> {

    @Override
    protected TestLabeler convertInputObject(TestLabelerInput inputObject) {
        return new TestLabeler(inputObject.getId());
    }

    @Override
    protected TestLabelerInput convertAppObject(TestLabeler appObject) {
        return TestLabelerInput.newBuilder().setId(appObject.getId().toString()).build();
    }

    @Override
    public Class<TestLabeler> getAppObjectClass() {
        return TestLabeler.class;
    }

    @Override
    public Class<TestLabelerInput> getInputObjectClass() {
        return TestLabelerInput.class;
    }

}
