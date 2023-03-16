package gov.hhs.aspr.gcm.translation.plugins.globalproperties;

import gov.hhs.aspr.gcm.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.plugins.properties.PropertiesTranslatorId;
import plugins.globalproperties.GlobalPropertiesPluginData;
import gov.hhs.aspr.gcm.translation.plugins.globalproperties.input.GlobalPropertiesPluginDataInput;
import gov.hhs.aspr.gcm.translation.plugins.globalproperties.translatorSpecs.GlobalPropertiesPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.globalproperties.translatorSpecs.GlobalPropertyIdTranslatorSpec;

public class GlobalPropertiesTranslator {

    private GlobalPropertiesTranslator() {
    }

    public static Translator.Builder builder() {
        return Translator.builder()
                .setTranslatorId(GlobalPropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslatorSpec(new GlobalPropertiesPluginDataTranslatorSpec());
                    translatorContext.addTranslatorSpec(new GlobalPropertyIdTranslatorSpec());
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
