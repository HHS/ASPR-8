package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.support.input.ReportLabelInput;
import gov.hhs.aspr.ms.taskit.core.TranslationController;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.SimpleReportLabel;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_ReportLabelTranslationSpec {

    @Test
    @UnitTestConstructor(target = ReportLabelTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new ReportLabelTranslationSpec());
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

        ReportLabelTranslationSpec translationSpec = new ReportLabelTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        ReportLabel expectedAppValue = new SimpleReportLabel("report label");

        ReportLabelInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        ReportLabel actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = ReportLabelTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        ReportLabelTranslationSpec translationSpec = new ReportLabelTranslationSpec();

        assertEquals(ReportLabel.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = ReportLabelTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        ReportLabelTranslationSpec translationSpec = new ReportLabelTranslationSpec();

        assertEquals(ReportLabelInput.class, translationSpec.getInputObjectClass());
    }
}
