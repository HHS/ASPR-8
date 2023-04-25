package gov.hhs.aspr.gcm.translation.protobuf.plugins.people;

import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.input.PersonIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.translationSpecs.PeoplePluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.translationSpecs.PersonIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.translationSpecs.PersonRangeTranslationSpec;

public class PeopleTranslator {

    private PeopleTranslator() {
    }

    public static Translator.Builder builder() {
        return Translator.builder()
                .setTranslatorId(PeopleTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

                    translationEngineBuilder.addTranslatorSpec(new PeoplePluginDataTranslationSpec());
                    translationEngineBuilder.addTranslatorSpec(new PersonIdTranslationSpec());
                    translationEngineBuilder.addTranslatorSpec(new PersonRangeTranslationSpec());

                    translationEngineBuilder.addFieldToIncludeDefaultValue(PersonIdInput.getDescriptor().findFieldByName("id"));
                });

    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
