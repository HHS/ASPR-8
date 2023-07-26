package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.globalproperties.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.globalproperties.GlobalPropertiesTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.globalproperties.support.input.GlobalPropertyDimensionInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.ms.taskit.core.TranslationController;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import plugins.globalproperties.support.GlobalPropertyDimension;
import plugins.globalproperties.testsupport.TestGlobalPropertyId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_GlobalPropertyDimensionTranslationSpec {

    @Test
    @UnitTestConstructor(target = GlobalPropertyDimensionTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new GlobalPropertyDimensionTranslationSpec());
    }

    @Test
    @UnitTestForCoverage
    public void testConvertObject() {
        TranslationController translationController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(GlobalPropertiesTranslator.getTranslator())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .build();

        ProtobufTranslationEngine protobufTranslationEngine = translationController
                .getTranslationEngine(ProtobufTranslationEngine.class);

        GlobalPropertyDimensionTranslationSpec translationSpec = new GlobalPropertyDimensionTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        GlobalPropertyDimension expectedAppValue = GlobalPropertyDimension
                .builder()
                .setAssignmentTime(0)
                .setGlobalPropertyId(TestGlobalPropertyId.GLOBAL_PROPERTY_3_DOUBLE_MUTABLE)
                .addValue(10.0)
                .addValue(1250.2)
                .addValue(15000.5)
                .build();

        GlobalPropertyDimensionInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        GlobalPropertyDimension actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = GlobalPropertyDimensionTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        GlobalPropertyDimensionTranslationSpec translationSpec = new GlobalPropertyDimensionTranslationSpec();

        assertEquals(GlobalPropertyDimension.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = GlobalPropertyDimensionTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        GlobalPropertyDimensionTranslationSpec translationSpec = new GlobalPropertyDimensionTranslationSpec();

        assertEquals(GlobalPropertyDimensionInput.class, translationSpec.getInputObjectClass());
    }

}
