package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.RegionsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.support.input.RegionFilterInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.ms.taskit.core.TranslationController;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionFilter;
import gov.hhs.aspr.ms.gcm.plugins.regions.testsupport.TestRegionId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_RegionFilterTranslationSpec {

    @Test
    @UnitTestConstructor(target = RegionFilterTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new RegionFilterTranslationSpec());
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

        RegionFilterTranslationSpec translationSpec = new RegionFilterTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        RegionFilter expectedAppValue = new RegionFilter(TestRegionId.values());

        RegionFilterInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        RegionFilter actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = RegionFilterTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        RegionFilterTranslationSpec translationSpec = new RegionFilterTranslationSpec();

        assertEquals(RegionFilter.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = RegionFilterTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        RegionFilterTranslationSpec translationSpec = new RegionFilterTranslationSpec();

        assertEquals(RegionFilterInput.class, translationSpec.getInputObjectClass());
    }
}
