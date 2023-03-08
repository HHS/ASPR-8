package plugins.stochastics;

import gov.hhs.aspr8.gcm.gcmprotobuf.core.PluginBundle;
import plugins.stochastics.translators.StochasticsPluginDataTranslator;

public class StochasticsPluginBundle {
    public static PluginBundle getPluginBundle(String inputFileName, String outputFileName) {
        return PluginBundle.builder()
                .setPluginBundleId(StochasticsPluginBundleId.PLUGIN_BUNDLE_ID)
                .setInputFileName(inputFileName)
                .setOutputFileName(outputFileName)
                .setInputObjectType(StochasticsPluginDataInput.getDefaultInstance())
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslator(new StochasticsPluginDataTranslator());
                })
                .build();
    }
}
