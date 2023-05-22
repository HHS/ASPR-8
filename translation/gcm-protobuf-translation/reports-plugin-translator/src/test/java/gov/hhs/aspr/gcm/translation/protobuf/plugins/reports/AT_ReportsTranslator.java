package gov.hhs.aspr.gcm.translation.protobuf.plugins.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.translationSpecs.ReportLabelTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.translationSpecs.ReportPeriodTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.translationSpecs.SimpleReportLabelTranslationSpec;
import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.SimpleReportLabel;
import util.annotations.UnitTestMethod;

public class AT_ReportsTranslator {

    @Test
    @UnitTestMethod(target = ReportsTranslator.class, name = "getTranslator", args = {})
    public void testGetTranslator() {
        Translator expectedTranslator = Translator.builder()
                .setTranslatorId(ReportsTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

                    translationEngineBuilder
                            .addTranslationSpec(new ReportLabelTranslationSpec())
                            .addTranslationSpec(new ReportPeriodTranslationSpec())
                            .addTranslationSpec(new SimpleReportLabelTranslationSpec());

                    translatorContext.addParentChildClassRelationship(SimpleReportLabel.class, ReportLabel.class);
                }).build();

        assertEquals(expectedTranslator, ReportsTranslator.getTranslator());
    }
}
