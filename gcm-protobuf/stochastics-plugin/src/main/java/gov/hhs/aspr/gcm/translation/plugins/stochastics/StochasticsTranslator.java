package gov.hhs.aspr.gcm.translation.plugins.stochastics;

import gov.hhs.aspr.gcm.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.plugins.stochastics.input.StochasticsPluginDataInput;
import gov.hhs.aspr.gcm.translation.plugins.stochastics.translatorSpecs.RandomGeneratorIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.stochastics.translatorSpecs.StochasticsPluginDataTranslatorSpec;
import plugins.stochastics.StochasticsPluginData;

public class StochasticsTranslator {

    private StochasticsTranslator() {

    }

    public static Translator.Builder getBaseTranslatorBuilder() {
        return Translator.builder()
                .setTranslatorId(StochasticsTranslatorId.PLUGIN_BUNDLE_ID)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslatorSpec(new StochasticsPluginDataTranslatorSpec());
                    translatorContext.addTranslatorSpec(new RandomGeneratorIdTranslatorSpec());
                });

    }

    public static Translator getTranslator(String inputFileName, String outputFileName) {
        return getBaseTranslatorBuilder()
                .addInputFile(inputFileName, StochasticsPluginDataInput.getDefaultInstance())
                .addOutputFile(outputFileName, StochasticsPluginData.class)
                .build();
    }

    public static Translator getTranslator() {
        return getBaseTranslatorBuilder().build();
    }
}
