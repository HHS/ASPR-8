package gov.hhs.aspr.gcm.translation.plugins.properties;

import gov.hhs.aspr.gcm.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.plugins.properties.translatorSpecs.PropertyDefinitionTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.properties.translatorSpecs.TimeTrackingPolicyTranslatorSpec;

public class PropertiesTranslator {

    private PropertiesTranslator() {

    }

    public static Translator.Builder getBaseTranslatorBuilder() {
        return Translator.builder()
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslatorSpec(new PropertyDefinitionTranslatorSpec());
                    translatorContext.addTranslatorSpec(new TimeTrackingPolicyTranslatorSpec());
                })
                .setInputIsPluginData(false)
                .setOutputIsPluginData(false)
                .setTranslatorId(PropertiesTranslatorId.TRANSLATOR_ID);

    }

    public static Translator getTranslator() {
        return getBaseTranslatorBuilder().build();

    }
}
