package gov.hhs.aspr.gcm.gcmprotobuf.globalproperties;

import gov.hhs.aspr.gcm.gcmprotobuf.core.PluginBundle;
import gov.hhs.aspr.gcm.gcmprotobuf.globalproperties.translators.GlobalPropertiesPluginDataTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.globalproperties.translators.GlobalPropertyIdTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.properties.PropertiesPluginBundleId;
import plugins.globalproperties.input.GlobalPropertiesPluginDataInput;

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
                    translatorContext.addTranslator(new GlobalPropertyIdTranslator());
                })
                .build();
    }
}
