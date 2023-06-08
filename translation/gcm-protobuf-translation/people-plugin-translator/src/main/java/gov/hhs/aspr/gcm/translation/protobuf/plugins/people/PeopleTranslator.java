package gov.hhs.aspr.gcm.translation.protobuf.plugins.people;

import gov.hhs.aspr.translation.core.TranslationSpec;
import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;

import java.util.ArrayList;
import java.util.List;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.input.PersonIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.translationSpecs.PeoplePluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.translationSpecs.PersonIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.translationSpecs.PersonRangeTranslationSpec;

/**
 * Translator for the People Plugin.
 * <li>Using this Translator will add
 * all the necessary TanslationSpecs needed to read and write
 * PeoplePluginData
 */
public class PeopleTranslator {

    private PeopleTranslator() {
    }

    protected static List<TranslationSpec<?, ?>> getTranslationSpecs() {
        List<TranslationSpec<?, ?>> list = new ArrayList<>();

        list.add(new PeoplePluginDataTranslationSpec());
        list.add(new PersonIdTranslationSpec());
        list.add(new PersonRangeTranslationSpec());

        return list;
    }

    private static Translator.Builder builder() {
        return Translator.builder()
                .setTranslatorId(PeopleTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

                    translationEngineBuilder
                            .addTranslationSpec(new PeoplePluginDataTranslationSpec())
                            .addTranslationSpec(new PersonIdTranslationSpec())
                            .addTranslationSpec(new PersonRangeTranslationSpec());

                    translationEngineBuilder
                            .addFieldToIncludeDefaultValue(PersonIdInput.getDescriptor().findFieldByName("id"));
                });

    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
