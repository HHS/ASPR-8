package gov.hhs.aspr8.gcm.gcmprotobuf.globalproperties;

import gov.hhs.aspr8.gcm.gcmprotobuf.core.PluginBundle;
import gov.hhs.aspr8.gcm.gcmprotobuf.globalproperties.translators.GlobalPropertiesPluginDataTranslator;
import gov.hhs.aspr8.gcm.gcmprotobuf.properties.PropertiesPluginBundleId;
import plugins.globalproperties.GlobalPropertiesPluginDataInput;

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
