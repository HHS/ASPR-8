package gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.translationSpecs.RandomNumberGeneratorIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.translationSpecs.StochasticsPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.translationSpecs.TestRandomGeneratorIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.translationSpecs.WellStateTranslationSpec;
import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;

/**
 * Translator for the Stochastics Plugin.
 * <li>Using this Translator will add
 * all the necessary TanslationSpecs needed to read and write
 * StochasticsPlugin
 */
public class StochasticsTranslator {

    private StochasticsTranslator() {
    }

    private static Translator.Builder builder() {
        Translator.Builder builder = Translator.builder()
                .setTranslatorId(StochasticsTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

                    translationEngineBuilder
                            .addTranslationSpec(new StochasticsPluginDataTranslationSpec())
                            .addTranslationSpec(new WellStateTranslationSpec())
                            .addTranslationSpec(new RandomNumberGeneratorIdTranslationSpec())
                            .addTranslationSpec(new TestRandomGeneratorIdTranslationSpec());
                });

        return builder;
    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
