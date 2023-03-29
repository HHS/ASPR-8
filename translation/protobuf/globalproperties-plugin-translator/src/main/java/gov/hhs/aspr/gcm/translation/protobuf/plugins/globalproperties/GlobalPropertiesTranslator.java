package gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties;

import gov.hhs.aspr.gcm.translation.protobuf.core.Translator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translatorSpecs.GlobalPropertiesPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translatorSpecs.GlobalPropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translatorSpecs.GlobalPropertyReportPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translatorSpecs.TestGlobalPropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslatorId;
import plugins.globalproperties.GlobalPropertiesPluginData;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.input.GlobalPropertiesPluginDataInput;

public class GlobalPropertiesTranslator {

    private GlobalPropertiesTranslator() {
    }

    public static Translator.Builder builder() {
        return Translator.builder()
                .setTranslatorId(GlobalPropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(ReportsTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslatorSpec(new GlobalPropertiesPluginDataTranslatorSpec());
                    translatorContext.addTranslatorSpec(new GlobalPropertyIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new TestGlobalPropertyIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new GlobalPropertyReportPluginDataTranslatorSpec());
                });
    }

    public static Translator getTranslatorRW(String inputFileName, String outputFileName) {
        return builder()
                .addInputFile(inputFileName, GlobalPropertiesPluginDataInput.getDefaultInstance())
                .addOutputFile(outputFileName, GlobalPropertiesPluginData.class)
                .build();
    }

    public static Translator getTranslatorR(String inputFileName) {
        return builder()
                .addInputFile(inputFileName, GlobalPropertiesPluginDataInput.getDefaultInstance())
                .build();
    }

    public static Translator getTranslatorW(String outputFileName) {
        return builder()
                .addOutputFile(outputFileName, GlobalPropertiesPluginData.class)
                .build();
    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
