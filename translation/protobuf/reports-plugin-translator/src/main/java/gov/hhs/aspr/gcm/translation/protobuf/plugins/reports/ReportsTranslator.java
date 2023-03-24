package gov.hhs.aspr.gcm.translation.protobuf.plugins.reports;

import gov.hhs.aspr.gcm.translation.protobuf.core.Translator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.translatorSpecs.ReportLabelTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.translatorSpecs.ReportPeriodTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.translatorSpecs.SimpleReportLabelTranslatorSpec;

public class ReportsTranslator {

    private ReportsTranslator() {

    }

    public static Translator.Builder builder() {
        return Translator.builder()
                .setTranslatorId(ReportsTranslatorId.TRANSLATOR_ID)
                .setInputIsPluginData(false)
                .setOutputIsPluginData(false)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslatorSpec(new ReportLabelTranslatorSpec());
                    translatorContext.addTranslatorSpec(new ReportPeriodTranslatorSpec());
                    translatorContext.addTranslatorSpec(new SimpleReportLabelTranslatorSpec());
                });

    }

    public static Translator getTranslator() {
        return builder().build();

    }
}
