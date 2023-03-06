package plugins.globalproperties;

import base.PluginBundle;
import plugins.globalproperties.translators.GlobalPropertiesPluginDataTranslator;

public class GlobalPropertiesPluginBundle {
    public static PluginBundle getPluginBundle(String inputFileName, String outputFileName) {
        return PluginBundle.builder()
                .setInputFileName(inputFileName)
                .setOutputFileName(outputFileName)
                .setInputObjectType(GlobalPropertiesPluginDataInput.getDefaultInstance())
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslator(new GlobalPropertiesPluginDataTranslator());
                })
                .build();
    }
}
