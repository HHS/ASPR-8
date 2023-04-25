package gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translationSpecs.GlobalPropertiesPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translationSpecs.GlobalPropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translationSpecs.GlobalPropertyReportPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translationSpecs.TestGlobalPropertyIdTranslatorSpec;
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
                    ProtobufTranslationEngine.Builder coreBuilder = translatorContext.getTranslatorCoreBuilder(ProtobufTranslationEngine.Builder.class);

                    coreBuilder.addTranslatorSpec(new GlobalPropertiesPluginDataTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new GlobalPropertyIdTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new TestGlobalPropertyIdTranslatorSpec());
                    if (withReport) {
                        coreBuilder.addTranslatorSpec(new GlobalPropertyReportPluginDataTranslatorSpec());
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
