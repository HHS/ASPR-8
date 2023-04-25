package gov.hhs.aspr.gcm.translation.protobuf.plugins.people;

import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.input.PersonIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.translationSpecs.PeoplePluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.translationSpecs.PersonIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.translationSpecs.PersonRangeTranslatorSpec;

public class PeopleTranslator {

    private PeopleTranslator() {
    }

    public static Translator.Builder builder() {
        return Translator.builder()
                .setTranslatorId(PeopleTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder coreBuilder = translatorContext
                            .getTranslatorCoreBuilder(ProtobufTranslationEngine.Builder.class);

                    coreBuilder.addTranslatorSpec(new PeoplePluginDataTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new PersonIdTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new PersonRangeTranslatorSpec());

                    coreBuilder.addFieldToIncludeDefaultValue(PersonIdInput.getDescriptor().findFieldByName("id"));
                });

    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
