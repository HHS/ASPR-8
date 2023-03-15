package gov.hhs.aspr.gcm.translation.plugins.stochastics;

import gov.hhs.aspr.gcm.translation.core.TranslatorModule;
import gov.hhs.aspr.gcm.translation.plugins.stochastics.translators.RandomGeneratorIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.stochastics.translators.StochasticsPluginDataTranslator;
import gov.hhs.aspr.gcm.translation.plugins.stochastics.input.StochasticsPluginDataInput;

public class StochasticsTranslatorModule {

    private StochasticsTranslatorModule() {

    }

    private static TranslatorModule.Builder getBaseModule() {
        return TranslatorModule.builder()
                .setPluginBundleId(StochasticsPluginBundleId.PLUGIN_BUNDLE_ID)
                .setInputObjectType(StochasticsPluginDataInput.getDefaultInstance())
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslator(new StochasticsPluginDataTranslator());
                    translatorContext.addTranslator(new RandomGeneratorIdTranslator());
                });

    }

    public static TranslatorModule getTranslatorModule(String inputFileName, String outputFileName) {
        return getBaseModule()
                .setInputFileName(inputFileName)
                .setOutputFileName(outputFileName)
                .build();
    }

    public static TranslatorModule getTranslatorModule() {
        return getBaseModule().build();
    }
}
