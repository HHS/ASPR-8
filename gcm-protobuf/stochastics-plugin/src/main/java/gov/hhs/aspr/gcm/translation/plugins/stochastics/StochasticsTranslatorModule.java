package gov.hhs.aspr.gcm.translation.plugins.stochastics;

import gov.hhs.aspr.gcm.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.plugins.stochastics.translators.RandomGeneratorIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.stochastics.translators.StochasticsPluginDataTranslator;
import gov.hhs.aspr.gcm.translation.plugins.stochastics.input.StochasticsPluginDataInput;

public class StochasticsTranslatorModule {

    private StochasticsTranslatorModule() {

    }

    private static Translator.Builder getBaseModule() {
        return Translator.builder()
                .setPluginBundleId(StochasticsPluginBundleId.PLUGIN_BUNDLE_ID)
                .setInputObjectType(StochasticsPluginDataInput.getDefaultInstance())
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslator(new StochasticsPluginDataTranslator());
                    translatorContext.addTranslator(new RandomGeneratorIdTranslator());
                });

    }

    public static Translator getTranslatorModule(String inputFileName, String outputFileName) {
        return getBaseModule()
                .setInputFileName(inputFileName)
                .setOutputFileName(outputFileName)
                .build();
    }

    public static Translator getTranslatorModule() {
        return getBaseModule().build();
    }
}
