package plugins.globalproperties;

import base.PluginBundle;
import plugins.globalproperties.translators.GlobalPropertiesPluginDataTranslator;
import plugins.properties.PropertiesPluginBundleId;

public class GlobalPropertiesPluginBundle {
    public static PluginBundle getPluginBundle(String inputFileName, String outputFileName) {
        return PluginBundle.builder()
                .setPluginBundleId(GlobalPropertiesPluginBundleId.PLUGIN_BUNDLE_ID)
                .addDependency(PropertiesPluginBundleId.PLUGIN_BUNDLE_ID)
                .setInputFileName(inputFileName)
                .setOutputFileName(outputFileName)
                .setInputObjectType(GlobalPropertiesPluginDataInput.getDefaultInstance())
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslator(new GlobalPropertiesPluginDataTranslator());
                })
                .build();
    }
}
