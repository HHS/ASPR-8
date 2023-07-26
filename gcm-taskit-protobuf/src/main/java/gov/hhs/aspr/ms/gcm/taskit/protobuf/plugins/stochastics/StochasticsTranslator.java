package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.stochastics;

import java.util.ArrayList;
import java.util.List;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.stochastics.translationSpecs.RandomNumberGeneratorIdTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.stochastics.translationSpecs.StochasticsPluginDataTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.stochastics.translationSpecs.TestRandomGeneratorIdTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.stochastics.translationSpecs.WellStateTranslationSpec;
import gov.hhs.aspr.ms.taskit.core.TranslationSpec;
import gov.hhs.aspr.ms.taskit.core.Translator;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;

/**
 * Translator for the Stochastics Plugin.
 * <li>Using this Translator will add
 * all the necessary TanslationSpecs needed to read and write
 * StochasticsPlugin
 */
public class StochasticsTranslator {

    private StochasticsTranslator() {
    }

    protected static List<TranslationSpec<?, ?>> getTranslationSpecs() {
        List<TranslationSpec<?, ?>> list = new ArrayList<>();

        list.add(new StochasticsPluginDataTranslationSpec());
        list.add(new WellStateTranslationSpec());
        list.add(new RandomNumberGeneratorIdTranslationSpec());
        list.add(new TestRandomGeneratorIdTranslationSpec());

        return list;
    }

    private static Translator.Builder builder() {
        Translator.Builder builder = Translator.builder()
                .setTranslatorId(StochasticsTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

                    for (TranslationSpec<?, ?> translationSpec : getTranslationSpecs()) {
                        translationEngineBuilder.addTranslationSpec(translationSpec);
                    }
                });

        return builder;
    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
