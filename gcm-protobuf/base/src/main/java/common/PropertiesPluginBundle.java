package common;

import com.google.protobuf.Message;

import base.PluginBundle;
import common.translators.PropertyDefinitionMapTranslator;
import common.translators.PropertyDefinitionTranslator;
import common.translators.PropertyValueMapTranslator;

public class PropertiesPluginBundle {

    private static PluginBundle.Builder addMessageInput(PluginBundle.Builder builder, Message inputType) {
        builder.setInputObjectType(inputType);

        return builder;
    }

    private static PluginBundle.Builder addConstants(PluginBundle.Builder builder) {
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
        PluginBundle.Builder builder = PluginBundle.builder()
                .setInputFileName(inputFileName)
                .setOutputFileName(outputFileName);

        addMessageInput(builder, inputType);
        addConstants(builder);

        return builder.build();

    }

    public static PluginBundle getPluginBundle(Message inputType) {
        PluginBundle.Builder builder = PluginBundle.builder();

        addMessageInput(builder, inputType);
        addConstants(builder);

        return builder.build();

    }

    public static PluginBundle getPluginBundle() {
        return addConstants(PluginBundle.builder()).build();

    }
}
