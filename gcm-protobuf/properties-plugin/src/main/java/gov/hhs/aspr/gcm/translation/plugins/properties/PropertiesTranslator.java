package gov.hhs.aspr.gcm.translation.plugins.properties;

import com.google.protobuf.Message;

import gov.hhs.aspr.gcm.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.plugins.properties.translatorSpecs.PropertyDefinitionTranslator;
import gov.hhs.aspr.gcm.translation.plugins.properties.translatorSpecs.TimeTrackingPolicyTranslator;

public class PropertiesTranslator {

    private PropertiesTranslator() {

    }

    private static Translator.Builder getBaseTranslator() {
        return Translator.builder()
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslatorSpec(new PropertyDefinitionTranslator());
                    translatorContext.addTranslatorSpec(new TimeTrackingPolicyTranslator());
                })
                .setInputIsPluginData(false)
                .setOutputIsPluginData(false)
                .setTranslatorId(PropertiesTranslatorId.TRANSLATOR_ID);

    }

    public static Translator getTranslator(String inputFileName, String outputFileName, Message inputType) {

        return getBaseTranslator()
                .setInputObjectType(inputType)
                .setInputFileName(inputFileName)
                .setOutputFileName(outputFileName)
                .build();

    }

    public static Translator getTranslator(Message inputType) {
        return getBaseTranslator()
                .setInputObjectType(inputType)
                .build();

    }

    public static Translator getTranslator() {
        return getBaseTranslator().build();

    }
}
