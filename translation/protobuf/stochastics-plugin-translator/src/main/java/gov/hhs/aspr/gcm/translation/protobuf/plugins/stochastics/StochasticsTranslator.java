package gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics;

import gov.hhs.aspr.gcm.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.translatorSpecs.RandomGeneratorIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.translatorSpecs.StochasticsPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.translatorSpecs.TestRandomGeneratorIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.translatorSpecs.WellStateTranslatorSpec;

public class StochasticsTranslator {

    private StochasticsTranslator() {

    }

    public static Translator.Builder builder() {
        return Translator.builder()
                .setTranslatorId(StochasticsTranslatorId.PLUGIN_BUNDLE_ID)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslatorSpec(new StochasticsPluginDataTranslatorSpec());
                    translatorContext.addTranslatorSpec(new WellStateTranslatorSpec());
                    translatorContext.addTranslatorSpec(new RandomGeneratorIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new TestRandomGeneratorIdTranslatorSpec());
                });

    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
