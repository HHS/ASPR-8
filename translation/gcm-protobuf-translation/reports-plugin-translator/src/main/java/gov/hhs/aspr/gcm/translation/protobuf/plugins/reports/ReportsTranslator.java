package gov.hhs.aspr.gcm.translation.protobuf.plugins.reports;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.translationSpecs.ReportLabelTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.translationSpecs.ReportPeriodTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.translationSpecs.SimpleReportLabelTranslationSpec;
import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.SimpleReportLabel;

public class ReportsTranslator {

    private ReportsTranslator() {
    }

    public static Translator.Builder builder() {
        Translator.Builder builder = Translator.builder()
                .setTranslatorId(ReportsTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

                    translationEngineBuilder.addTranslatorSpec(new ReportLabelTranslationSpec());
                    translationEngineBuilder.addTranslatorSpec(new ReportPeriodTranslationSpec());
                    translationEngineBuilder.addTranslatorSpec(new SimpleReportLabelTranslationSpec());

                    translatorContext.addParentChildClassRelationship(SimpleReportLabel.class, ReportLabel.class);
                });

        return builder;
    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
