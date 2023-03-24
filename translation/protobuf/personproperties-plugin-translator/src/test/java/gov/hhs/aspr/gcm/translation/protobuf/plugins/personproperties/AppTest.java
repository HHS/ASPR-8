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

import gov.hhs.aspr.gcm.translation.protobuf.core.Translator;
import gov.hhs.aspr.gcm.translation.protobuf.core.TranslatorController;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.input.PersonPropertiesPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.reports.input.PersonPropertyInteractionReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.reports.input.PersonPropertyReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslator;
import nucleus.PluginData;
import plugins.people.support.PersonId;
import plugins.personproperties.PersonPropertiesPluginData;
import plugins.personproperties.reports.PersonPropertyInteractionReportPluginData;
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

                String personPropertyInteractionReportPluginDataInputFileName = "personPropertyInteractionReportPluginDataInput.json";
                String personPropertyInteractionReportPluginDataOutputFileName = "personPropertyInteractionReportPluginDataOutput.json";

                Translator personPropertiesTranslator = PersonPropertiesTranslator
                                .builder()
                                .addInputFile(inputFilePath.resolve(pluginDataInputFileName).toString(),
                                                PersonPropertiesPluginDataInput.getDefaultInstance())
                                .addOutputFile(outputFilePath.resolve(pluginDataOutputFileName).toString(),
                                                PersonPropertiesPluginData.class)
                                .addInputFile(inputFilePath.resolve(personPropertyReportPluginDataInputFileName)
                                                .toString(),
                                                PersonPropertyReportPluginDataInput.getDefaultInstance())
                                .addOutputFile(outputFilePath.resolve(personPropertyReportPluginDataOutputFileName)
                                                .toString(),
                                                PersonPropertyReportPluginData.class)
                                .addInputFile(inputFilePath
                                                .resolve(personPropertyInteractionReportPluginDataInputFileName)
                                                .toString(),
                                                PersonPropertyInteractionReportPluginDataInput.getDefaultInstance())
                                .addOutputFile(outputFilePath
                                                .resolve(personPropertyInteractionReportPluginDataOutputFileName)
                                                .toString(),
                                                PersonPropertyInteractionReportPluginData.class)
                                .build();

                TranslatorController translatorController = TranslatorController.builder()
                                .addTranslator(personPropertiesTranslator)
                                .addTranslator(PropertiesTranslator.getTranslator())
                                .addTranslator(PeopleTranslator.getTranslator())
                                .addTranslator(ReportsTranslator.getTranslator())
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
                                if (propertyId.getPropertyDefinition().getDefaultValue().isEmpty()
                                                || randomGenerator.nextBoolean()) {
                                        Object expectedPropertyValue = propertyId
                                                        .getRandomPropertyValue(randomGenerator);
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
                // Person Property Report

                PersonPropertyReportPluginData personPropertyReportPluginData = (PersonPropertyReportPluginData) pluginDatas
                                .get(1);

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

                PersonPropertyReportPluginData expectedPluginData = personPropertyReportPluginDataBuilder.build();

                assertEquals(reportLabel, personPropertyReportPluginData.getReportLabel());
                assertEquals(reportPeriod, personPropertyReportPluginData.getReportPeriod());

                assertEquals(expectedPluginData.getDefaultInclusionPolicy(),
                                personPropertyReportPluginData.getDefaultInclusionPolicy());

                assertEquals(expectedPluginData.getIncludedProperties(),
                                personPropertyReportPluginData.getIncludedProperties());
                assertEquals(expectedPluginData.getExcludedProperties(),
                                personPropertyReportPluginData.getExcludedProperties());

                // Person Property Interaction Report
                PersonPropertyInteractionReportPluginData personPropertyInteractionReportPluginData = (PersonPropertyInteractionReportPluginData) pluginDatas
                                .get(2);

                PersonPropertyInteractionReportPluginData.Builder personPropertyInteractionReportPluginDataBuilder = PersonPropertyInteractionReportPluginData
                                .builder();

                personPropertyInteractionReportPluginDataBuilder.setReportLabel(reportLabel)
                                .setReportPeriod(reportPeriod);

                for (PersonPropertyId personPropertyId : expectedPersonPropertyIds) {
                        if (randomGenerator.nextBoolean()) {
                                personPropertyInteractionReportPluginDataBuilder.addPersonPropertyId(personPropertyId);
                        }
                }

                PersonPropertyInteractionReportPluginData exPersonPropertyInteractionReportPluginData = personPropertyInteractionReportPluginDataBuilder
                                .build();

                assertEquals(exPersonPropertyInteractionReportPluginData.getReportLabel(),
                                personPropertyInteractionReportPluginData.getReportLabel());
                assertEquals(exPersonPropertyInteractionReportPluginData.getReportPeriod(),
                                personPropertyInteractionReportPluginData.getReportPeriod());
                assertEquals(exPersonPropertyInteractionReportPluginData.getPersonPropertyIds(),
                                personPropertyInteractionReportPluginData.getPersonPropertyIds());

                translatorController.writeOutput();
        }
}
