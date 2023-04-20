package gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translatorSpecs.GlobalPropertiesPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translatorSpecs.GlobalPropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translatorSpecs.GlobalPropertyReportPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translatorSpecs.TestGlobalPropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslatorId;
import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorCore;

public class GlobalPropertiesTranslator {

    private GlobalPropertiesTranslator() {
    }

    public static Translator.Builder builder(boolean withReport) {
        
        Translator.Builder builder = Translator.builder()
                .setTranslatorId(GlobalPropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslatorCore.Builder coreBuilder = translatorContext.getTranslatorCoreBuilder(ProtobufTranslatorCore.Builder.class);

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
