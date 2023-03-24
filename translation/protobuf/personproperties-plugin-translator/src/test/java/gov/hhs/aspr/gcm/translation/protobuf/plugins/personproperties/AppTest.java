package gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.core.Translator;
import gov.hhs.aspr.gcm.translation.protobuf.core.TranslatorController;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.input.PersonPropertiesPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.reports.input.PersonPropertyReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translatorSpecs.TestPersonPropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslator;
import nucleus.PluginData;
import plugins.people.support.PersonId;
import plugins.personproperties.PersonPropertiesPluginData;
import plugins.personproperties.reports.PersonPropertyReportPluginData;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.support.PersonPropertyInitialization;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;
import plugins.reports.support.SimpleReportLabel;
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

        String pluginDataInputFileName = "pluginDataInput.json";
        String pluginDataOutputFileName = "pluginDataOutput.json";

        String personPropertyReportPluginDataInputFileName = "personPropertyReportPluginDataInput.json";
        String personPropertyReportPluginDataOutputFileName = "personPropertyReportPluginDataOutput.json";

        Translator personPropertiesTranslator = PersonPropertiesTranslator
                .builder()
                .addInputFile(inputFilePath.resolve(pluginDataInputFileName).toString(),
                        PersonPropertiesPluginDataInput.getDefaultInstance())
                .addOutputFile(outputFilePath.resolve(pluginDataOutputFileName).toString(),
                        PersonPropertiesPluginData.class)
                .addOutputFile(outputFilePath.resolve(personPropertyReportPluginDataOutputFileName).toString(),
                        PersonPropertyReportPluginData.class)
                .build();

        TranslatorController translatorController = TranslatorController.builder()
                .addTranslator(personPropertiesTranslator)
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
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

        ReportLabel reportLabel = new SimpleReportLabel("report label");
        ReportPeriod reportPeriod = ReportPeriod.DAILY;

        PersonPropertyReportPluginData.Builder personPropertyReportPluginDataBuilder = //
                PersonPropertyReportPluginData.builder()//
                        .setReportPeriod(reportPeriod)//
                        .setReportLabel(reportLabel);//

        for (PersonPropertyId personPropertyId : expectedPersonPropertyIds) {
            if (randomGenerator.nextBoolean()) {
                personPropertyReportPluginDataBuilder.includePersonProperty(personPropertyId);
            } else {
                personPropertyReportPluginDataBuilder.excludePersonProperty(personPropertyId);
            }
        }

        translatorController.writePluginDataOutput(personPropertyReportPluginDataBuilder.build());
    }
}
