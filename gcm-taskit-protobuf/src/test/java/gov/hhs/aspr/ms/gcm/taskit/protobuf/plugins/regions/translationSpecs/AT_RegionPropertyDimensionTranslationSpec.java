package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.RegionsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.support.input.RegionPropertyDimensionInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.ms.taskit.core.TranslationController;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionPropertyDimension;
import gov.hhs.aspr.ms.gcm.plugins.regions.testsupport.TestRegionId;
import gov.hhs.aspr.ms.gcm.plugins.regions.testsupport.TestRegionPropertyId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_RegionPropertyDimensionTranslationSpec {

    @Test
    @UnitTestConstructor(target = RegionPropertyDimensionTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new RegionPropertyDimensionTranslationSpec());
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

        RegionPropertyDimensionTranslationSpec translationSpec = new RegionPropertyDimensionTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        RegionPropertyDimension expectedAppValue = RegionPropertyDimension
                .builder()
                .setRegionId(TestRegionId.REGION_1)
                .setRegionPropertyId(TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE)
                .addValue(10.0)
                .addValue(1250.2)
                .addValue(15000.5)
                .build();

        RegionPropertyDimensionInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        RegionPropertyDimension actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = RegionPropertyDimensionTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        RegionPropertyDimensionTranslationSpec translationSpec = new RegionPropertyDimensionTranslationSpec();

        assertEquals(RegionPropertyDimension.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = RegionPropertyDimensionTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        RegionPropertyDimensionTranslationSpec translationSpec = new RegionPropertyDimensionTranslationSpec();

        assertEquals(RegionPropertyDimensionInput.class, translationSpec.getInputObjectClass());
    }
}
