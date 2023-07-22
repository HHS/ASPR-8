package gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.RegionsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.TestRegionIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import plugins.regions.testsupport.TestRegionId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_TestRegionIdTranslationSpec {

    @Test
    @UnitTestConstructor(target = TestRegionIdTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new TestRegionIdTranslationSpec());
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

        TestRegionIdTranslationSpec translationSpec = new TestRegionIdTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        TestRegionId expectedAppValue = TestRegionId.REGION_1;

        TestRegionIdInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        TestRegionId actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = TestRegionIdTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        TestRegionIdTranslationSpec translationSpec = new TestRegionIdTranslationSpec();

        assertEquals(TestRegionId.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = TestRegionIdTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        TestRegionIdTranslationSpec translationSpec = new TestRegionIdTranslationSpec();

        assertEquals(TestRegionIdInput.class, translationSpec.getInputObjectClass());
    }
}
