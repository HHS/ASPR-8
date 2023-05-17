package gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translationSpecs.GlobalPropertiesPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translationSpecs.GlobalPropertyIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translationSpecs.GlobalPropertyReportPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translationSpecs.TestGlobalPropertyIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslatorId;
import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;

/**
 * Translator for the GlobalProperties Plugin.
 * <li>Using this Translator will add
 * all the necessary TanslationSpecs needed to read and write
 * GlobalPropertiesPluginData
 */
public class GlobalPropertiesTranslator {

    private GlobalPropertiesTranslator() {
    }

    /**
     * Returns a Translator Builder that already includes the necessary
     * TranslationSpecs needed to read and write GlobalPropertiesPluginData
     * 
     * <li>the parameter withReport controls whether to add TranslationSpecs for the
     * GlobalPropertyReportPluginData and also add a dependency on the
     * ReportsTranslator
     */
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

    /**
     * Returns a GlobalPropertiesTranslator that includes TranslationSpecs for the
     * GlobalPropertiesPluginData and GlobalPropertyReportPluginData and has a
     * dependency on the ReportsTranslator.
     * <li>Equivalent to calling builder(true).build()
     */
    public static Translator getTranslatorWithReport() {
        return builder(true).build();
    }

    /**
     * Returns a GlobalPropertiesTranslator that includes TranslationSpecs for the
     * GlobalPropertiesPluginData.
     * <li>Equivalent to calling builder(false).build()
     */
    public static Translator getTranslator() {
        return builder(false).build();
    }
}
