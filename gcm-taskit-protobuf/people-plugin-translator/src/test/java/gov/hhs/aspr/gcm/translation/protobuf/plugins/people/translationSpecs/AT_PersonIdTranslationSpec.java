package gov.hhs.aspr.gcm.translation.protobuf.plugins.people.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.input.PersonIdInput;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import plugins.people.support.PersonId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_PersonIdTranslationSpec {

    @Test
    @UnitTestConstructor(target = PersonIdTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new PersonIdTranslationSpec());
    }

    @Test
    @UnitTestForCoverage
    public void testConvertObject() {
        TranslationController translationController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(PeopleTranslator.getTranslator())
                .build();

        ProtobufTranslationEngine protobufTranslationEngine = translationController
                .getTranslationEngine(ProtobufTranslationEngine.class);

        PersonIdTranslationSpec translationSpec = new PersonIdTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        PersonId expectedAppValue = new PersonId(0);

        PersonIdInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        PersonId actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = PersonIdTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        PersonIdTranslationSpec translationSpec = new PersonIdTranslationSpec();

        assertEquals(PersonId.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = PersonIdTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        PersonIdTranslationSpec translationSpec = new PersonIdTranslationSpec();

        assertEquals(PersonIdInput.class, translationSpec.getInputObjectClass());
    }
}
