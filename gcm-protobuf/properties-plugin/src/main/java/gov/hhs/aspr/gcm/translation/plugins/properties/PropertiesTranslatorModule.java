package gov.hhs.aspr.gcm.translation.plugins.properties;

import com.google.protobuf.Message;

import gov.hhs.aspr.gcm.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.plugins.properties.translators.PropertyDefinitionTranslator;
import gov.hhs.aspr.gcm.translation.plugins.properties.translators.TimeTrackingPolicyTranslator;

public class PropertiesTranslatorModule {

    private PropertiesTranslatorModule() {

    }

    private static Translator.Builder getBaseModule() {
        return Translator.builder()
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslator(new PropertyDefinitionTranslator());
                    translatorContext.addTranslator(new TimeTrackingPolicyTranslator());
                })
                .setInputIsPluginData(false)
                .setOutputIsPluginData(false)
                .setPluginBundleId(PropertiesTranslatorModuleId.TRANSLATOR_ID);

    }

    public static Translator getTranslatorModule(String inputFileName, String outputFileName, Message inputType) {

        return getBaseModule()
                .setInputObjectType(inputType)
                .setInputFileName(inputFileName)
                .setOutputFileName(outputFileName)
                .build();

    }

    public static Translator getTranslatorModule(Message inputType) {
        return getBaseModule()
                .setInputObjectType(inputType)
                .build();

    }

    public static Translator getTranslatorModule() {
        return getBaseModule().build();

    }
}
