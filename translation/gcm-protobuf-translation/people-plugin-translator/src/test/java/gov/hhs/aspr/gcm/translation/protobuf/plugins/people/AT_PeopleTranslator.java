package gov.hhs.aspr.gcm.translation.protobuf.plugins.people;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.input.PersonIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.translationSpecs.PeoplePluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.translationSpecs.PersonIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.translationSpecs.PersonRangeTranslationSpec;
import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import util.annotations.UnitTestMethod;

public class AT_PeopleTranslator {

    @Test
    @UnitTestMethod(target = PeopleTranslator.class, name = "getTranslator", args = {})
    public void testGetTranslator() {

        Translator expectedTranslator = Translator.builder()
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
                }).build();

        assertEquals(expectedTranslator, PeopleTranslator.getTranslator());
    }
}
