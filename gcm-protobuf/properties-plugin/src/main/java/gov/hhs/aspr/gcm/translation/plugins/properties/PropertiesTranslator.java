package gov.hhs.aspr.gcm.translation.plugins.properties;

import gov.hhs.aspr.gcm.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.plugins.properties.translatorSpecs.PropertyDefinitionTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.properties.translatorSpecs.TimeTrackingPolicyTranslatorSpec;

public class PropertiesTranslator {

    private PropertiesTranslator() {

    }

    public static Translator.Builder getBaseTranslatorBuilder() {
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
        return getBaseTranslatorBuilder().build();

    }
}
