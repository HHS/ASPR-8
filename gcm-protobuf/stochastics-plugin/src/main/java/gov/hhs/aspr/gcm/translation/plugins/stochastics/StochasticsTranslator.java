package gov.hhs.aspr.gcm.translation.plugins.stochastics;

import gov.hhs.aspr.gcm.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.plugins.stochastics.input.StochasticsPluginDataInput;
import gov.hhs.aspr.gcm.translation.plugins.stochastics.translatorSpecs.RandomGeneratorIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.stochastics.translatorSpecs.StochasticsPluginDataTranslator;

public class StochasticsTranslator {

    private StochasticsTranslator() {

    }

    private static Translator.Builder getBaseTranslator() {
        return Translator.builder()
                .setPluginBundleId(StochasticsPluginBundleId.PLUGIN_BUNDLE_ID)
                .setInputObjectType(StochasticsPluginDataInput.getDefaultInstance())
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslatorSpec(new StochasticsPluginDataTranslator());
                    translatorContext.addTranslatorSpec(new RandomGeneratorIdTranslator());
                });

    }

    public static Translator getTranslator(String inputFileName, String outputFileName) {
        return getBaseTranslator()
                .setInputFileName(inputFileName)
                .setOutputFileName(outputFileName)
                .build();
    }

    public static Translator getTranslator() {
        return getBaseTranslator().build();
    }
}
