package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.support.input.ReportPeriodInput;
import gov.hhs.aspr.ms.taskit.core.TranslationController;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import plugins.reports.support.ReportPeriod;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_ReportPeriodTranslationSpec {

    @Test
    @UnitTestConstructor(target = ReportPeriodTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new ReportPeriodTranslationSpec());
    }

    @Test
    @UnitTestForCoverage
    public void testConvertObject() {
        TranslationController translationController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(ReportsTranslator.getTranslator())
                .build();

        ProtobufTranslationEngine protobufTranslationEngine = translationController
                .getTranslationEngine(ProtobufTranslationEngine.class);

        ReportPeriodTranslationSpec translationSpec = new ReportPeriodTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        ReportPeriod expectedAppValue = ReportPeriod.DAILY;

        ReportPeriodInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        ReportPeriod actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = ReportPeriodTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        ReportPeriodTranslationSpec translationSpec = new ReportPeriodTranslationSpec();

        assertEquals(ReportPeriod.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = ReportPeriodTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        ReportPeriodTranslationSpec translationSpec = new ReportPeriodTranslationSpec();

        assertEquals(ReportPeriodInput.class, translationSpec.getInputObjectClass());
    }
}
