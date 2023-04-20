package gov.hhs.aspr.gcm.translation.protobuf.plugins.properties;

import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorCore;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.translatorSpecs.PropertyDefinitionMapTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.translatorSpecs.PropertyDefinitionTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.translatorSpecs.PropertyValueMapTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.translatorSpecs.TimeTrackingPolicyTranslatorSpec;

public class PropertiesTranslator {

    private PropertiesTranslator() {
    }

    public static Translator.Builder builder() {
        Translator.Builder builder = Translator.builder()
                .setTranslatorId(PropertiesTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslatorCore.Builder coreBuilder = translatorContext
                            .getTranslatorCoreBuilder(ProtobufTranslatorCore.Builder.class);

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
