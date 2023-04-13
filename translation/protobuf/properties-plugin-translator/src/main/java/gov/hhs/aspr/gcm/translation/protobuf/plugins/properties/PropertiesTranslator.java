package gov.hhs.aspr.gcm.translation.protobuf.plugins.properties;

import gov.hhs.aspr.gcm.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.translatorSpecs.PropertyDefinitionMapTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.translatorSpecs.PropertyDefinitionTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.translatorSpecs.PropertyValueMapTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.translatorSpecs.TimeTrackingPolicyTranslatorSpec;

public class PropertiesTranslator {

    private PropertiesTranslator() {

    }

    public static Translator.Builder builder() {
        return Translator.builder()
                .setTranslatorId(PropertiesTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslatorSpec(new PropertyDefinitionTranslatorSpec());
                    translatorContext.addTranslatorSpec(new TimeTrackingPolicyTranslatorSpec());
                    translatorContext.addTranslatorSpec(new PropertyValueMapTranslatorSpec());
                    translatorContext.addTranslatorSpec(new PropertyDefinitionMapTranslatorSpec());
                });

    }

    public static Translator getTranslator() {
        return builder().build();

    }
}
