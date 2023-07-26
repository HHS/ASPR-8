package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.input.TestMaterialsProducerPropertyIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.materials.testsupport.TestMaterialsProducerPropertyId;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain TestMaterialsProducerPropertyIdInput} and
 * {@linkplain TestMaterialsProducerPropertyId}
 */
public class TestMaterialsProducerPropertyIdTranslationSpec
        extends ProtobufTranslationSpec<TestMaterialsProducerPropertyIdInput, TestMaterialsProducerPropertyId> {

    @Override
    protected TestMaterialsProducerPropertyId convertInputObject(TestMaterialsProducerPropertyIdInput inputObject) {
        return TestMaterialsProducerPropertyId.valueOf(inputObject.name());
    }

    @Override
    protected TestMaterialsProducerPropertyIdInput convertAppObject(TestMaterialsProducerPropertyId appObject) {
        return TestMaterialsProducerPropertyIdInput.valueOf(appObject.name());
    }

    @Override
    public Class<TestMaterialsProducerPropertyId> getAppObjectClass() {
        return TestMaterialsProducerPropertyId.class;
    }

    @Override
    public Class<TestMaterialsProducerPropertyIdInput> getInputObjectClass() {
        return TestMaterialsProducerPropertyIdInput.class;
    }

}
