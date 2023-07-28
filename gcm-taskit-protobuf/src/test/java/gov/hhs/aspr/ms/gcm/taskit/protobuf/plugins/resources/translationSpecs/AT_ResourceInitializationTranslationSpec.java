package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.RegionsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.ResourcesTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceInitializationInput;
import gov.hhs.aspr.ms.taskit.core.TranslationController;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourceInitialization;
import gov.hhs.aspr.ms.gcm.plugins.resources.testsupport.TestResourceId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_ResourceInitializationTranslationSpec {

    @Test
    @UnitTestConstructor(target = ResourceInitializationTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new ResourceInitializationTranslationSpec());
    }

    @Test
    @UnitTestForCoverage
    public void testConvertObject() {
        TranslationController translationController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(ResourcesTranslator.getTranslator())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(RegionsTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .build();

        ProtobufTranslationEngine protobufTranslationEngine = translationController
                .getTranslationEngine(ProtobufTranslationEngine.class);

        ResourceInitializationTranslationSpec translationSpec = new ResourceInitializationTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        ResourceInitialization expectedAppValue = new ResourceInitialization(TestResourceId.RESOURCE_1, 100L);

        ResourceInitializationInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        ResourceInitialization actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = ResourceInitializationTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        ResourceInitializationTranslationSpec translationSpec = new ResourceInitializationTranslationSpec();

        assertEquals(ResourceInitialization.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = ResourceInitializationTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        ResourceInitializationTranslationSpec translationSpec = new ResourceInitializationTranslationSpec();

        assertEquals(ResourceInitializationInput.class, translationSpec.getInputObjectClass());
    }
}
