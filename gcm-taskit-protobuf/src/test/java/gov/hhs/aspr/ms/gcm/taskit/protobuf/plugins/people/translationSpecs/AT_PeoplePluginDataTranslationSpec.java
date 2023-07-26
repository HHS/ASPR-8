package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput;
import gov.hhs.aspr.ms.taskit.core.TranslationController;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import plugins.people.datamanagers.PeoplePluginData;
import plugins.people.support.PersonRange;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

public class AT_PeoplePluginDataTranslationSpec {

    @Test
    @UnitTestConstructor(target = PeoplePluginDataTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new PeoplePluginDataTranslationSpec());
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

        PeoplePluginDataTranslationSpec translationSpec = new PeoplePluginDataTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        PeoplePluginData.Builder builder = PeoplePluginData.builder();
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6573670690105604419L);

        int numRanges = randomGenerator.nextInt(15);

        for (int i = 0; i < numRanges; i++) {
            PersonRange personRange = new PersonRange(i * 15, (i * 15) + 1);
            builder.addPersonRange(personRange);
        }

        PeoplePluginData expectedAppValue = builder.build();

        PeoplePluginDataInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        PeoplePluginData actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
        assertEquals(expectedAppValue.toString(), actualAppValue.toString());

        inputValue = inputValue.toBuilder().clearPersonCount().build();

        actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
        assertEquals(expectedAppValue.toString(), actualAppValue.toString());
    }

    @Test
    @UnitTestMethod(target = PeoplePluginDataTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        PeoplePluginDataTranslationSpec translationSpec = new PeoplePluginDataTranslationSpec();

        assertEquals(PeoplePluginData.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = PeoplePluginDataTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        PeoplePluginDataTranslationSpec translationSpec = new PeoplePluginDataTranslationSpec();

        assertEquals(PeoplePluginDataInput.class, translationSpec.getInputObjectClass());
    }
}
