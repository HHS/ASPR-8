package gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.PersonPropertiesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.input.PersonPropertiesPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import plugins.people.support.PersonId;
import plugins.personproperties.PersonPropertiesPluginData;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

public class AT_PersonPropertiesPluginDataTranslationSpec {

    @Test
    @UnitTestConstructor(target = PersonPropertiesPluginDataTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new PersonPropertiesPluginDataTranslationSpec());
    }

    @Test
    @UnitTestForCoverage
    public void testConvertObject() {
        TranslationController translationController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(PersonPropertiesTranslator.getTranslator())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .build();

        ProtobufTranslationEngine protobufTranslationEngine = translationController
                .getTranslationEngine(ProtobufTranslationEngine.class);

        PersonPropertiesPluginDataTranslationSpec translationSpec = new PersonPropertiesPluginDataTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        long seed = 4684903523797799712L;
        int initialPoptulation = 100;

        List<PersonId> people = new ArrayList<>();
        for (int i = 0; i < initialPoptulation; i++) {
            people.add(new PersonId(i));
        }

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			builder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition(), 0.0, testPersonPropertyId.isTimeTracked());
		}
        
		for (PersonId personId : people) {

			for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {

				boolean doesNotHaveDefaultValue = testPersonPropertyId.getPropertyDefinition().getDefaultValue().isEmpty();
				if (doesNotHaveDefaultValue || randomGenerator.nextBoolean()) {
					Object randomPropertyValue = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
					builder.setPersonPropertyValue(personId, testPersonPropertyId, randomPropertyValue);
				}

                if(testPersonPropertyId.isTimeTracked()) {
                    builder.setPersonPropertyTime(personId, testPersonPropertyId, 5.0);
                }
			}
		}

        PersonPropertiesPluginData expectedAppValue = builder.build();

        PersonPropertiesPluginDataInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        PersonPropertiesPluginData actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = PersonPropertiesPluginDataTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        PersonPropertiesPluginDataTranslationSpec translationSpec = new PersonPropertiesPluginDataTranslationSpec();

        assertEquals(PersonPropertiesPluginData.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = PersonPropertiesPluginDataTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        PersonPropertiesPluginDataTranslationSpec translationSpec = new PersonPropertiesPluginDataTranslationSpec();

        assertEquals(PersonPropertiesPluginDataInput.class, translationSpec.getInputObjectClass());
    }
}
