package gov.hhs.aspr.gcm.translation.protobuf.plugins.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.translationSpecs.PropertyDefinitionTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.translationSpecs.TimeTrackingPolicyTranslationSpec;
import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import util.annotations.UnitTestMethod;

public class AT_PropertiesTranslator {

    @Test
    @UnitTestMethod(target = PropertiesTranslator.class, name = "getTranslator", args = {})
    public void testGetTranslator() {
        Translator expectedTranslator = Translator.builder()
                .setTranslatorId(PropertiesTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

                    translationEngineBuilder
                            .addTranslationSpec(new PropertyDefinitionTranslationSpec())
                            .addTranslationSpec(new TimeTrackingPolicyTranslationSpec());
                }).build();

        assertEquals(expectedTranslator, PropertiesTranslator.getTranslator());
    }
}
