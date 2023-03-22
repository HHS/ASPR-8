package gov.hhs.aspr.gcm.translation.protobuf.plugins.properties;

import gov.hhs.aspr.gcm.translation.protobuf.core.Translator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.translatorSpecs.PropertyDefinitionTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.translatorSpecs.TimeTrackingPolicyTranslatorSpec;

public class PropertiesTranslator {

    private PropertiesTranslator() {

    }

    public static Translator.Builder builder() {
        return Translator.builder()
                .setTranslatorId(PropertiesTranslatorId.TRANSLATOR_ID)
                .setInputIsPluginData(false)
                .setOutputIsPluginData(false)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslatorSpec(new PropertyDefinitionTranslatorSpec());
                    translatorContext.addTranslatorSpec(new TimeTrackingPolicyTranslatorSpec());
                });

    }

    public static Translator getTranslator() {
        return builder().build();

    }
}
