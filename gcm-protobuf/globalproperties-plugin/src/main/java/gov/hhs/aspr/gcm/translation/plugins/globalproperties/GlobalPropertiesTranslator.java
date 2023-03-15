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

    private static Translator.Builder getBaseTranslatorBuilder() {
        return Translator.builder()
                .setTranslatorId(GlobalPropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslatorSpec(new GlobalPropertiesPluginDataTranslatorSpec());
                    translatorContext.addTranslatorSpec(new GlobalPropertyIdTranslatorSpec());
                });
    }

    public static Translator getTranslator(String inputFileName, String outputFileName) {
        return getBaseTranslatorBuilder()
                .addInputFile(inputFileName, GlobalPropertiesPluginDataInput.getDefaultInstance())
                .addOutputFile(outputFileName, GlobalPropertiesPluginData.class)
                .build();
    }

    public static Translator getTranslator() {
        return getBaseTranslatorBuilder().build();
    }
}
