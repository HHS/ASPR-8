package gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics;

import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.translatorSpecs.RandomGeneratorIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.translatorSpecs.StochasticsPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.translatorSpecs.TestRandomGeneratorIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.translatorSpecs.WellStateTranslatorSpec;

public class StochasticsTranslator {

    private StochasticsTranslator() {
    }

    public static Translator.Builder builder() {
        Translator.Builder builder = Translator.builder()
                .setTranslatorId(StochasticsTranslatorId.PLUGIN_BUNDLE_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder coreBuilder = translatorContext.getTranslatorCoreBuilder(ProtobufTranslationEngine.Builder.class);

                    coreBuilder.addTranslatorSpec(new StochasticsPluginDataTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new WellStateTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new RandomGeneratorIdTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new TestRandomGeneratorIdTranslatorSpec());
                });

        return builder;
    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
