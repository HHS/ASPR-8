package gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.RegionsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.TestRegionPropertyIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import plugins.regions.testsupport.TestRegionPropertyId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_TestRegionPropertyIdTranslationSpec {

    @Test
    @UnitTestConstructor(target = TestRegionPropertyIdTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new TestRegionPropertyIdTranslationSpec());
    }

    @Test
    @UnitTestForCoverage
    public void testConvertObject() {
        TranslationController translationController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(RegionsTranslator.getTranslator())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .build();

        ProtobufTranslationEngine protobufTranslationEngine = translationController
                .getTranslationEngine(ProtobufTranslationEngine.class);

        TestRegionPropertyIdTranslationSpec translationSpec = new TestRegionPropertyIdTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        TestRegionPropertyId expectedAppValue = TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE;

        TestRegionPropertyIdInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        TestRegionPropertyId actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = TestRegionPropertyIdTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        TestRegionPropertyIdTranslationSpec translationSpec = new TestRegionPropertyIdTranslationSpec();

        assertEquals(TestRegionPropertyId.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = TestRegionPropertyIdTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        TestRegionPropertyIdTranslationSpec translationSpec = new TestRegionPropertyIdTranslationSpec();

        assertEquals(TestRegionPropertyIdInput.class, translationSpec.getInputObjectClass());
    }
}
