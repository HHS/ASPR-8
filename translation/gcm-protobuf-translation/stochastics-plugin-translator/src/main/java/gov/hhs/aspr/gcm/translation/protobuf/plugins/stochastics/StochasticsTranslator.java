package gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.translationSpecs.RandomGeneratorIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.translationSpecs.StochasticsPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.translationSpecs.TestRandomGeneratorIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.translationSpecs.WellStateTranslationSpec;
import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;

public class StochasticsTranslator {

    private StochasticsTranslator() {
    }

    public static Translator.Builder builder() {
        Translator.Builder builder = Translator.builder()
                .setTranslatorId(StochasticsTranslatorId.PLUGIN_BUNDLE_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder coreBuilder = translatorContext.getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

                    coreBuilder.addTranslatorSpec(new StochasticsPluginDataTranslationSpec());
                    coreBuilder.addTranslatorSpec(new WellStateTranslationSpec());
                    coreBuilder.addTranslatorSpec(new RandomGeneratorIdTranslationSpec());
                    coreBuilder.addTranslatorSpec(new TestRandomGeneratorIdTranslationSpec());
                });

        return builder;
    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
