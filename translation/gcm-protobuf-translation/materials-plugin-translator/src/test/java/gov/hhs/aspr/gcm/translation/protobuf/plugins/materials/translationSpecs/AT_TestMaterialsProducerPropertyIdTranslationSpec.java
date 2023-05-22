package gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.MaterialsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.TestMaterialsProducerPropertyIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.RegionsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.ResourcesTranslator;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import plugins.materials.testsupport.TestMaterialsProducerPropertyId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_TestMaterialsProducerPropertyIdTranslationSpec {
    
    @Test
    @UnitTestConstructor(target = TestMaterialsProducerPropertyIdTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new TestMaterialsProducerPropertyIdTranslationSpec());
    }

    @Test
    @UnitTestForCoverage
    public void testConvertObject() {
        TranslationController translationController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(MaterialsTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(ResourcesTranslator.getTranslator())
                .addTranslator(RegionsTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .build();

        ProtobufTranslationEngine protobufTranslationEngine = translationController
                .getTranslationEngine(ProtobufTranslationEngine.class);

        TestMaterialsProducerPropertyIdTranslationSpec translationSpec = new TestMaterialsProducerPropertyIdTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        TestMaterialsProducerPropertyId expectedAppValue = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;

        TestMaterialsProducerPropertyIdInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        TestMaterialsProducerPropertyId actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = TestMaterialsProducerPropertyIdTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        TestMaterialsProducerPropertyIdTranslationSpec translationSpec = new TestMaterialsProducerPropertyIdTranslationSpec();

        assertEquals(TestMaterialsProducerPropertyId.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = TestMaterialsProducerPropertyIdTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        TestMaterialsProducerPropertyIdTranslationSpec translationSpec = new TestMaterialsProducerPropertyIdTranslationSpec();

        assertEquals(TestMaterialsProducerPropertyIdInput.class, translationSpec.getInputObjectClass());
    }
}
