package gov.hhs.aspr.gcm.translation.protobuf.plugins.people.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.input.PersonRangeInput;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import plugins.people.support.PersonRange;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_PersonRangeTranslationSpec {

    @Test
    @UnitTestConstructor(target = PersonRangeTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new PersonRangeTranslationSpec());
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

        PersonRangeTranslationSpec translationSpec = new PersonRangeTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        PersonRange expectedAppValue = new PersonRange(1 * 15, (2 * 15) + 1);

        PersonRangeInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        PersonRange actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = PersonRangeTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        PersonRangeTranslationSpec translationSpec = new PersonRangeTranslationSpec();

        assertEquals(PersonRange.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = PersonRangeTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        PersonRangeTranslationSpec translationSpec = new PersonRangeTranslationSpec();

        assertEquals(PersonRangeInput.class, translationSpec.getInputObjectClass());
    }
}
