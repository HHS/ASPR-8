package gov.hhs.aspr.gcm.translation.plugins.properties;

import com.google.protobuf.Message;

import gov.hhs.aspr.gcm.translation.core.TranslatorModule;
import gov.hhs.aspr.gcm.translation.plugins.properties.translators.PropertyDefinitionTranslator;
import gov.hhs.aspr.gcm.translation.plugins.properties.translators.TimeTrackingPolicyTranslator;

public class PropertiesTranslatorModule {

    private PropertiesTranslatorModule() {

    }

    private static TranslatorModule.Builder getBaseModule() {
        return TranslatorModule.builder()
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslator(new PropertyDefinitionTranslator());
                    translatorContext.addTranslator(new TimeTrackingPolicyTranslator());
                })
                .setInputIsPluginData(false)
                .setOutputIsPluginData(false)
                .setPluginBundleId(PropertiesTranslatorModuleId.TRANSLATOR_MODULE_ID);

    }

    public static TranslatorModule getTranslatorModule(String inputFileName, String outputFileName, Message inputType) {

        return getBaseModule()
                .setInputObjectType(inputType)
                .setInputFileName(inputFileName)
                .setOutputFileName(outputFileName)
                .build();

    }

    public static TranslatorModule getTranslatorModule(Message inputType) {
        return getBaseModule()
                .setInputObjectType(inputType)
                .build();

    }

    public static TranslatorModule getTranslatorModule() {
        return getBaseModule().build();

    }
}
