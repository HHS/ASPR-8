package gov.hhs.aspr.gcm.translation.plugins.stochastics;

import gov.hhs.aspr.gcm.translation.core.TranslatorModule;
import gov.hhs.aspr.gcm.translation.plugins.stochastics.translators.RandomGeneratorIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.stochastics.translators.StochasticsPluginDataTranslator;
import gov.hhs.aspr.gcm.translation.plugins.stochastics.input.StochasticsPluginDataInput;

public class StochasticsPluginBundle {
    public static TranslatorModule getPluginBundle(String inputFileName, String outputFileName) {
        return TranslatorModule.builder()
                .setPluginBundleId(StochasticsPluginBundleId.PLUGIN_BUNDLE_ID)
                .setInputFileName(inputFileName)
                .setOutputFileName(outputFileName)
                .setInputObjectType(StochasticsPluginDataInput.getDefaultInstance())
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslator(new StochasticsPluginDataTranslator());
                    translatorContext.addTranslator(new RandomGeneratorIdTranslator());
                })
                .build();
    }
}
