package gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.core.TranslatorController;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translatorSpecs.TestPersonPropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslator;
import nucleus.PluginData;
import plugins.people.support.PersonId;
import plugins.personproperties.PersonPropertiesPluginData;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.support.PersonPropertyInitialization;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import plugins.util.properties.PropertyDefinition;
import util.random.RandomGeneratorProvider;

public class AppTest {

    @Test
    public void testPersonPropertiesTranslator() {
        Path basePath = Path.of("").toAbsolutePath();

        if (!basePath.endsWith("personproperties-plugin-translator")) {
            basePath = basePath.resolve("personproperties-plugin-translator");
        }

        Path inputFilePath = basePath.resolve("src/main/resources/json");
        Path outputFilePath = basePath.resolve("src/main/resources/json/output");
        
        outputFilePath.toFile().mkdir();

        String inputFileName = "input.json";
        String outputFileName = "output.json";

        TranslatorController translatorController = TranslatorController.builder()
                .addTranslator(
                        PersonPropertiesTranslator.getTranslatorRW(inputFilePath.resolve(inputFileName).toString(), outputFilePath.resolve(outputFileName).toString()))
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslatorSpec(new TestPersonPropertyIdTranslatorSpec())
                .build();

        List<PluginData> pluginDatas = translatorController.readInput().getPluginDatas();

        PersonPropertiesPluginData personPropertiesPluginData = (PersonPropertiesPluginData) pluginDatas.get(0);

        long seed = 4684903523797799712L;
        int initialPoptulation = 100;

        List<PersonId> people = new ArrayList<>();
        for (int i = 0; i < initialPoptulation; i++) {
            people.add(new PersonId(i));
        }

        Set<TestPersonPropertyId> expectedPersonPropertyIds = EnumSet.allOf(TestPersonPropertyId.class);
        assertFalse(expectedPersonPropertyIds.isEmpty());

        Set<PersonPropertyId> actualPersonPropertyIds = personPropertiesPluginData.getPersonPropertyIds();
        assertEquals(expectedPersonPropertyIds, actualPersonPropertyIds);

        for (TestPersonPropertyId expecetedPropertyId : expectedPersonPropertyIds) {
            PropertyDefinition expectedPropertyDefinition = expecetedPropertyId.getPropertyDefinition();
            PropertyDefinition actualPropertyDefinition = personPropertiesPluginData
                    .getPersonPropertyDefinition(expecetedPropertyId);
            assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
        }

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
        for (PersonId personId : people) {
            List<Pair<TestPersonPropertyId, Object>> expectedValues = new ArrayList<>();
            for (TestPersonPropertyId propertyId : TestPersonPropertyId.values()) {
                if (propertyId.getPropertyDefinition().getDefaultValue().isEmpty() || randomGenerator.nextBoolean()) {
                    Object expectedPropertyValue = propertyId.getRandomPropertyValue(randomGenerator);
                    expectedValues.add(new Pair<>(propertyId, expectedPropertyValue));
                }
            }
            List<PersonPropertyInitialization> propInitList = personPropertiesPluginData
                    .getPropertyValues(personId.getValue());

            assertEquals(expectedValues.size(), propInitList.size());
            for (int i = 0; i < propInitList.size(); i++) {
                TestPersonPropertyId expectedPersonPropertyId = expectedValues.get(i).getFirst();
                Object expectedValue = expectedValues.get(i).getSecond();

                PersonPropertyId actualPropertyId = propInitList.get(i).getPersonPropertyId();
                Object actualValue = propInitList.get(i).getValue();

                assertEquals(expectedPersonPropertyId, actualPropertyId);
                assertEquals(expectedValue, actualValue);

            }

        }

        translatorController.writeOutput();
    }
}
