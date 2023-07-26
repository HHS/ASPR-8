package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.MaterialsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.testsupport.input.TestMaterialsProducerIdInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.RegionsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.ResourcesTranslator;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import plugins.materials.testsupport.TestMaterialsProducerId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_TestMaterialsProducerIdTranslationSpec {

    @Test
    @UnitTestConstructor(target = TestMaterialsProducerIdTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new TestMaterialsProducerIdTranslationSpec());
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

        TestMaterialsProducerIdTranslationSpec translationSpec = new TestMaterialsProducerIdTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        TestMaterialsProducerId expectedAppValue = TestMaterialsProducerId.MATERIALS_PRODUCER_1;

        TestMaterialsProducerIdInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        TestMaterialsProducerId actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = TestMaterialsProducerIdTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        TestMaterialsProducerIdTranslationSpec translationSpec = new TestMaterialsProducerIdTranslationSpec();

        assertEquals(TestMaterialsProducerId.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = TestMaterialsProducerIdTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        TestMaterialsProducerIdTranslationSpec translationSpec = new TestMaterialsProducerIdTranslationSpec();

        assertEquals(TestMaterialsProducerIdInput.class, translationSpec.getInputObjectClass());
    }
}
