package gov.hhs.aspr.gcm.translation.protobuf.plugins.properties;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.translationSpecs.PropertyDefinitionMapTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.translationSpecs.PropertyDefinitionTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.translationSpecs.PropertyValueMapTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.translationSpecs.TimeTrackingPolicyTranslationSpec;
import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;

public class PropertiesTranslator {

    private PropertiesTranslator() {
    }

    public static Translator.Builder builder() {
        Translator.Builder builder = Translator.builder()
                .setTranslatorId(PropertiesTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

                    translationEngineBuilder.addTranslationSpec(new PropertyDefinitionTranslationSpec());
                    translationEngineBuilder.addTranslationSpec(new TimeTrackingPolicyTranslationSpec());
                    translationEngineBuilder.addTranslationSpec(new PropertyValueMapTranslationSpec());
                    translationEngineBuilder.addTranslationSpec(new PropertyDefinitionMapTranslationSpec());
                });

        return builder;
    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
