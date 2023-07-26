package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.support.input.SimpleReportLabelInput;
import gov.hhs.aspr.ms.taskit.core.TranslationController;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import plugins.reports.support.SimpleReportLabel;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_SimpleReportLabelTranslationSpec {

    @Test
    @UnitTestConstructor(target = SimpleReportLabelTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new SimpleReportLabelTranslationSpec());
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

        SimpleReportLabelTranslationSpec translationSpec = new SimpleReportLabelTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        SimpleReportLabel expectedAppValue = new SimpleReportLabel("report label");

        SimpleReportLabelInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        SimpleReportLabel actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = SimpleReportLabelTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        SimpleReportLabelTranslationSpec translationSpec = new SimpleReportLabelTranslationSpec();

        assertEquals(SimpleReportLabel.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = SimpleReportLabelTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        SimpleReportLabelTranslationSpec translationSpec = new SimpleReportLabelTranslationSpec();

        assertEquals(SimpleReportLabelInput.class, translationSpec.getInputObjectClass());
    }
}
