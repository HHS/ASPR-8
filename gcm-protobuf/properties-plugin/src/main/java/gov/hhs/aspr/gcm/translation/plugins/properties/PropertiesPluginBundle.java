package gov.hhs.aspr.gcm.translation.plugins.properties;

import com.google.protobuf.Message;

import gov.hhs.aspr.gcm.translation.core.TranslatorModule;
import gov.hhs.aspr.gcm.translation.plugins.properties.translators.PropertyDefinitionTranslator;
import gov.hhs.aspr.gcm.translation.plugins.properties.translators.TimeTrackingPolicyTranslator;

public class PropertiesPluginBundle {

    private static TranslatorModule.Builder addMessageInput(TranslatorModule.Builder builder, Message inputType) {
        builder.setInputObjectType(inputType);

        return builder;
    }

    private static TranslatorModule.Builder setConstants(TranslatorModule.Builder builder) {
        builder.setInitializer((translatorContext) -> {
            translatorContext.addTranslator(new PropertyDefinitionTranslator());
            translatorContext.addTranslator(new TimeTrackingPolicyTranslator());
        })
                .setInputIsPluginData(false)
                .setOutputIsPluginData(false)
                .setPluginBundleId(PropertiesPluginBundleId.PLUGIN_BUNDLE_ID);

        return builder;
    }

    public static TranslatorModule getPluginBundle(String inputFileName, String outputFileName, Message inputType) {

        return addMessageInput(setConstants(TranslatorModule.builder()), inputType)
                .setInputFileName(inputFileName)
                .setOutputFileName(outputFileName)
                .build();

    }

    public static TranslatorModule getPluginBundle(Message inputType) {
        return addMessageInput(setConstants(TranslatorModule.builder()), inputType)
                .build();

    }

    public static TranslatorModule getPluginBundle() {
        return setConstants(TranslatorModule.builder()).build();

    }
}
