package common;

import com.google.protobuf.Message;

import base.PluginBundle;
import common.translators.PropertyDefinitionTranslator;

public class PropertiesPluginBundle {
    public static PluginBundle getPluginBundle(String inputFileName, String outputFileName, Message inputType) {
        return PluginBundle.builder()
                .setInputFileName(inputFileName)
                .setOutputFileName(outputFileName)
                .setInputObjectType(inputType)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslator(new PropertyDefinitionTranslator());
                })
                .setInputIsPluginData(false)
                .setOutputIsPluginData(false)
                .build();
    }
}
