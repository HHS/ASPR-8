package gov.hhs.aspr.gcm.gcmprotobuf.properties;

import com.google.protobuf.Message;

import gov.hhs.aspr.gcm.gcmprotobuf.core.PluginBundle;
import gov.hhs.aspr.gcm.gcmprotobuf.properties.translators.PropertyDefinitionMapTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.properties.translators.PropertyDefinitionTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.properties.translators.PropertyValueMapTranslator;

public class PropertiesPluginBundle {

    private static PluginBundle.Builder addMessageInput(PluginBundle.Builder builder, Message inputType) {
        builder.setInputObjectType(inputType);

        return builder;
    }

    private static PluginBundle.Builder setConstants(PluginBundle.Builder builder) {
        builder.setInitializer((translatorContext) -> {
            translatorContext.addTranslator(new PropertyDefinitionTranslator());
            translatorContext.addTranslator(new PropertyDefinitionMapTranslator());
            translatorContext.addTranslator(new PropertyValueMapTranslator());
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
