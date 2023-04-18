package gov.hhs.aspr.gcm.translation.protobuf.plugins.reports;

import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.translatorSpecs.ReportLabelTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.translatorSpecs.ReportPeriodTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.translatorSpecs.SimpleReportLabelTranslatorSpec;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.SimpleReportLabel;

public class ReportsTranslator {

    private ReportsTranslator() {
    }

    public static Translator.Builder builder() {
        Translator.Builder builder = Translator.builder()
                .setTranslatorId(ReportsTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslatorSpec(new ReportLabelTranslatorSpec());
                    translatorContext.addTranslatorSpec(new ReportPeriodTranslatorSpec());
                    translatorContext.addTranslatorSpec(new SimpleReportLabelTranslatorSpec());

                    translatorContext.addMarkerInterface(SimpleReportLabel.class, ReportLabel.class);
                });

        return builder;
    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
