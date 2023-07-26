package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.RegionsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.ResourcesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourcePropertyIdInput;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import plugins.resources.support.ResourcePropertyId;
import plugins.resources.testsupport.TestResourcePropertyId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_ResourcePropertyIdTranslationSpec {

    @Test
    @UnitTestConstructor(target = ResourcePropertyIdTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new ResourcePropertyIdTranslationSpec());
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

        ResourcePropertyIdTranslationSpec translationSpec = new ResourcePropertyIdTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        ResourcePropertyId expectedAppValue = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;

        ResourcePropertyIdInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        ResourcePropertyId actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = ResourcePropertyIdTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        ResourcePropertyIdTranslationSpec translationSpec = new ResourcePropertyIdTranslationSpec();

        assertEquals(ResourcePropertyId.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = ResourcePropertyIdTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        ResourcePropertyIdTranslationSpec translationSpec = new ResourcePropertyIdTranslationSpec();

        assertEquals(ResourcePropertyIdInput.class, translationSpec.getInputObjectClass());
    }
}