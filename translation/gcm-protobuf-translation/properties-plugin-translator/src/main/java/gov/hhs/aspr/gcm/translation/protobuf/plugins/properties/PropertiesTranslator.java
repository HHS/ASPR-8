package gov.hhs.aspr.gcm.translation.protobuf.plugins.properties;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.translationSpecs.PropertyDefinitionTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.translationSpecs.TimeTrackingPolicyTranslationSpec;
import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;

/**
 * Translator for the Properties Plugin.
 * <li>Using this Translator will add
 * all the necessary TanslationSpecs needed to read and write
 * PropertiesPlugin (PropertyDefiniton, TimeTrackingPolicy)
 */
public class PropertiesTranslator {

    private PropertiesTranslator() {
    }

    private static Translator.Builder builder() {
        Translator.Builder builder = Translator.builder()
                .setTranslatorId(PropertiesTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

                    translationEngineBuilder
                            .addTranslationSpec(new PropertyDefinitionTranslationSpec())
                            .addTranslationSpec(new TimeTrackingPolicyTranslationSpec());
                });

        return builder;
    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
