package gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.translationSpecs.RandomNumberGeneratorIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.translationSpecs.StochasticsPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.translationSpecs.TestRandomGeneratorIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.translationSpecs.WellStateTranslationSpec;
import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import util.annotations.UnitTestMethod;

public class AT_StochasticsTranslator {

    @Test
    @UnitTestMethod(target = StochasticsTranslator.class, name = "getTranslator", args = {})
    public void testGetTranslator() {
        Translator expectedTranslator = Translator.builder()
                .setTranslatorId(StochasticsTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

                    translationEngineBuilder
                            .addTranslationSpec(new StochasticsPluginDataTranslationSpec())
                            .addTranslationSpec(new WellStateTranslationSpec())
                            .addTranslationSpec(new RandomNumberGeneratorIdTranslationSpec())
                            .addTranslationSpec(new TestRandomGeneratorIdTranslationSpec());
                }).build();

        assertEquals(expectedTranslator, StochasticsTranslator.getTranslator());
    }
}
