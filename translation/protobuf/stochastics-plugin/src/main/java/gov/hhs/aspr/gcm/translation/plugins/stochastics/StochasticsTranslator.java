package gov.hhs.aspr.gcm.translation.plugins.stochastics;

import gov.hhs.aspr.gcm.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.plugins.stochastics.input.StochasticsPluginDataInput;
import gov.hhs.aspr.gcm.translation.plugins.stochastics.translatorSpecs.RandomGeneratorIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.stochastics.translatorSpecs.StochasticsPluginDataTranslatorSpec;
import plugins.stochastics.StochasticsPluginData;

public class StochasticsTranslator {

    private StochasticsTranslator() {

    }

    public static Translator.Builder builder() {
        return Translator.builder()
                .setTranslatorId(StochasticsTranslatorId.PLUGIN_BUNDLE_ID)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslatorSpec(new StochasticsPluginDataTranslatorSpec());
                    translatorContext.addTranslatorSpec(new RandomGeneratorIdTranslatorSpec());
                });

    }

    public static Translator getTranslatorRW(String inputFileName, String outputFileName) {
        return builder()
                .addInputFile(inputFileName, StochasticsPluginDataInput.getDefaultInstance())
                .addOutputFile(outputFileName, StochasticsPluginData.class)
                .build();
    }

    public static Translator getTranslatorR(String inputFileName) {
        return builder()
                .addInputFile(inputFileName, StochasticsPluginDataInput.getDefaultInstance())
                .build();
    }

    public static Translator getTranslatorW(String outputFileName) {
        return builder()
                .addOutputFile(outputFileName, StochasticsPluginData.class)
                .build();
    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
