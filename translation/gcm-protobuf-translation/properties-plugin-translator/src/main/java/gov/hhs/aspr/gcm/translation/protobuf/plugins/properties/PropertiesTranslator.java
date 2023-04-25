package gov.hhs.aspr.gcm.translation.protobuf.plugins.properties;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.translationSpecs.PropertyDefinitionMapTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.translationSpecs.PropertyDefinitionTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.translationSpecs.PropertyValueMapTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.translationSpecs.TimeTrackingPolicyTranslatorSpec;
import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;

public class PropertiesTranslator {

    private PropertiesTranslator() {
    }

    public static Translator.Builder builder() {
        Translator.Builder builder = Translator.builder()
                .setTranslatorId(PropertiesTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder coreBuilder = translatorContext
                            .getTranslatorCoreBuilder(ProtobufTranslationEngine.Builder.class);

                    coreBuilder.addTranslatorSpec(new PropertyDefinitionTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new TimeTrackingPolicyTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new PropertyValueMapTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new PropertyDefinitionMapTranslatorSpec());
                });

        return builder;
    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
