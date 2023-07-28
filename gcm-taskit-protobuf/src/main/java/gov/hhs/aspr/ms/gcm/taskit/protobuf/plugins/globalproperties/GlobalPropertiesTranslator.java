package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.globalproperties;

import java.util.ArrayList;
import java.util.List;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.globalproperties.translationSpecs.GlobalPropertiesPluginDataTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.globalproperties.translationSpecs.GlobalPropertyDimensionTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.globalproperties.translationSpecs.GlobalPropertyIdTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.globalproperties.translationSpecs.GlobalPropertyReportPluginDataTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.globalproperties.translationSpecs.TestGlobalPropertyIdTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.PropertiesTranslatorId;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.ReportsTranslatorId;
import gov.hhs.aspr.ms.taskit.core.TranslationSpec;
import gov.hhs.aspr.ms.taskit.core.Translator;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;

/**
 * Translator for the GlobalProperties Plugin.
 * <li>Using this Translator will add
 * all the necessary TanslationSpecs needed to read and write
 * GlobalPropertiesPluginData
 */
public class GlobalPropertiesTranslator {

    private GlobalPropertiesTranslator() {
    }

    protected static List<TranslationSpec<?, ?>> getTranslationSpecs() {
        List<TranslationSpec<?, ?>> list = new ArrayList<>();

        list.add(new GlobalPropertiesPluginDataTranslationSpec());
        list.add(new GlobalPropertyIdTranslationSpec());
        list.add(new GlobalPropertyDimensionTranslationSpec());
        list.add(new TestGlobalPropertyIdTranslationSpec());
        list.add(new GlobalPropertyReportPluginDataTranslationSpec());

        return list;
    }

    /**
     * Returns a Translator Builder that already includes the necessary
     * TranslationSpecs needed to read and write GlobalPropertiesPluginData and its
     * respective reports - GlobalPropertyReportPluginData
     */
    private static Translator.Builder builder() {

        Translator.Builder builder = Translator.builder()
                .setTranslatorId(GlobalPropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(ReportsTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

                    for (TranslationSpec<?, ?> translationSpec : getTranslationSpecs()) {
                        translationEngineBuilder.addTranslationSpec(translationSpec);
                    }
                });

        return builder;
    }

    /**
     * Returns a GlobalPropertiesTranslator that includes TranslationSpecs for the
     * GlobalPropertiesPluginData.
     * <li>Equivalent to calling builder(false).build()
     */
    public static Translator getTranslator() {
        return builder().build();
    }
}
