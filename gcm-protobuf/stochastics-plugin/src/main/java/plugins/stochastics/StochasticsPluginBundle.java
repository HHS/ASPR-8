package plugins.stochastics;

import base.PluginBundle;
import plugins.stochastics.translators.StochasticsPluginDataTranslator;

public class StochasticsPluginBundle {
    public static PluginBundle getPluginBundle(String inputFileName, String outputFileName) {
        return PluginBundle.builder()
                .setInputFileName(inputFileName)
                .setOutputFileName(outputFileName)
                .setInputObjectType(StochasticsPluginDataInput.getDefaultInstance())
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslator(new StochasticsPluginDataTranslator());
                })
                .build();
    }
}
