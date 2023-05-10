package gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translationSpecs.GlobalPropertiesPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translationSpecs.GlobalPropertyIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translationSpecs.GlobalPropertyReportPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translationSpecs.TestGlobalPropertyIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslatorId;
import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;

public class GlobalPropertiesTranslator {

    private GlobalPropertiesTranslator() {
    }

    public static Translator.Builder builder(boolean withReport) {

        Translator.Builder builder = Translator.builder()
                .setTranslatorId(GlobalPropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

                    translationEngineBuilder.addTranslationSpec(new GlobalPropertiesPluginDataTranslationSpec());
                    translationEngineBuilder.addTranslationSpec(new GlobalPropertyIdTranslationSpec());
                    translationEngineBuilder.addTranslationSpec(new TestGlobalPropertyIdTranslationSpec());
                    if (withReport) {
                        translationEngineBuilder
                                .addTranslationSpec(new GlobalPropertyReportPluginDataTranslationSpec());
                    }
                });

        if (withReport) {
            builder.addDependency(ReportsTranslatorId.TRANSLATOR_ID);
        }

        return builder;
    }

    public static Translator getTranslatorWithReport() {
        return builder(true).build();
    }

    public static Translator getTranslator() {
        return builder(false).build();
    }
}
