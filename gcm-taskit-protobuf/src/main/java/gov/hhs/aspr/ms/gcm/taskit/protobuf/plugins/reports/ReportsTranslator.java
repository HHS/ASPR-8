package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports;

import java.util.ArrayList;
import java.util.List;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.translationSpecs.ReportLabelTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.translationSpecs.ReportPeriodTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.translationSpecs.SimpleReportLabelTranslationSpec;
import gov.hhs.aspr.ms.taskit.core.TranslationSpec;
import gov.hhs.aspr.ms.taskit.core.Translator;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.SimpleReportLabel;

/**
 * Translator for the Reports Plugin.
 * <li>Using this Translator will add
 * all the necessary TanslationSpecs needed to read and write
 * ReportsPlugin
 */
public class ReportsTranslator {

    private ReportsTranslator() {
    }

    protected static List<TranslationSpec<?, ?>> getTranslationSpecs() {
        List<TranslationSpec<?, ?>> list = new ArrayList<>();

        list.add(new ReportLabelTranslationSpec());
        list.add(new ReportPeriodTranslationSpec());
        list.add(new SimpleReportLabelTranslationSpec());

        return list;
    }

    private static Translator.Builder builder() {
        Translator.Builder builder = Translator.builder()
                .setTranslatorId(ReportsTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

                    for (TranslationSpec<?, ?> translationSpec : getTranslationSpecs()) {
                        translationEngineBuilder.addTranslationSpec(translationSpec);
                    }

                    translatorContext.addParentChildClassRelationship(SimpleReportLabel.class, ReportLabel.class);
                });

        return builder;
    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
