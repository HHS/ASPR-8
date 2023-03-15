package gov.hhs.aspr.gcm.translation.plugins.properties;

import com.google.protobuf.Message;

import gov.hhs.aspr.gcm.translation.core.PluginBundle;
import gov.hhs.aspr.gcm.translation.plugins.properties.translators.PropertyDefinitionTranslator;
import gov.hhs.aspr.gcm.translation.plugins.properties.translators.TimeTrackingPolicyTranslator;

public class PropertiesPluginBundle {

    private static PluginBundle.Builder addMessageInput(PluginBundle.Builder builder, Message inputType) {
        builder.setInputObjectType(inputType);

        return builder;
    }

    private static PluginBundle.Builder setConstants(PluginBundle.Builder builder) {
        builder.setInitializer((translatorContext) -> {
            translatorContext.addTranslator(new PropertyDefinitionTranslator());
            translatorContext.addTranslator(new TimeTrackingPolicyTranslator());
        })
                .setInputIsPluginData(false)
                .setOutputIsPluginData(false)
                .setPluginBundleId(PropertiesPluginBundleId.PLUGIN_BUNDLE_ID);

        return builder;
    }

    public static PluginBundle getPluginBundle(String inputFileName, String outputFileName, Message inputType) {

        return addMessageInput(setConstants(PluginBundle.builder()), inputType)
                .setInputFileName(inputFileName)
                .setOutputFileName(outputFileName)
                .build();

    }

    public static PluginBundle getPluginBundle(Message inputType) {
        return addMessageInput(setConstants(PluginBundle.builder()), inputType)
                .build();

    }

    public static PluginBundle getPluginBundle() {
        return setConstants(PluginBundle.builder()).build();

    }
}
