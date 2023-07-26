package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.MaterialsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.MaterialIdInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.RegionsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.ResourcesTranslator;
import gov.hhs.aspr.ms.taskit.core.TranslationController;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import plugins.materials.support.MaterialId;
import plugins.materials.testsupport.TestMaterialId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_MaterialIdTranslationSpec {

    @Test
    @UnitTestConstructor(target = MaterialIdTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new MaterialIdTranslationSpec());
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

        MaterialIdTranslationSpec translationSpec = new MaterialIdTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        MaterialId expectedAppValue = TestMaterialId.MATERIAL_1;

        MaterialIdInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        MaterialId actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = MaterialIdTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        MaterialIdTranslationSpec translationSpec = new MaterialIdTranslationSpec();

        assertEquals(MaterialId.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = MaterialIdTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        MaterialIdTranslationSpec translationSpec = new MaterialIdTranslationSpec();

        assertEquals(MaterialIdInput.class, translationSpec.getInputObjectClass());
    }
}
