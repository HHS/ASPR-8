package gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.input.StochasticsPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.core.Translator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.translatorSpecs.RandomGeneratorIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.translatorSpecs.StochasticsPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.translatorSpecs.TestRandomGeneratorIdTranslatorSpec;
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
                    translatorContext.addTranslatorSpec(new TestRandomGeneratorIdTranslatorSpec());
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
