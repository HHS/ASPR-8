package gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorCore;
import gov.hhs.aspr.translation.core.TranslatorController;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.input.PersonPropertiesPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.input.PersonPropertyInteractionReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.input.PersonPropertyReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslator;
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
    Path basePath = getCurrentDir();
    Path inputFilePath = basePath.resolve("json");
    Path outputFilePath = makeOutputDir();

    private Path getCurrentDir() {
        try {
            return Path.of(this.getClass().getClassLoader().getResource("").toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private Path makeOutputDir() {
        Path path = basePath.resolve("json/output");

        path.toFile().mkdirs();

        return path;
    }

    @Test
    public void testPersonPropertiesTranslator() {
        String fileName = "pluginData.json";

        TranslatorController translatorController = TranslatorController.builder()
                .setTranslatorCoreBuilder(ProtobufTranslatorCore.builder())
                .addTranslator(PersonPropertiesTranslator.getTranslator())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .addReader(inputFilePath.resolve(fileName), PersonPropertiesPluginDataInput.class)
                .addWriter(outputFilePath.resolve(fileName), PersonPropertiesPluginData.class)
                .build();

        translatorController.readInput();

        PersonPropertiesPluginData personPropertiesPluginData = translatorController
                .getObject(PersonPropertiesPluginData.class);

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

        translatorController.writeOutput();
    }

    @Test
    public void testPersonPropertyReportTranslatorSpec() {
        String fileName = "propertyReport.json";

        TranslatorController translatorController = TranslatorController.builder()
                .setTranslatorCoreBuilder(ProtobufTranslatorCore.builder())
                .addTranslator(PersonPropertiesTranslator.builder(true).build())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .addReader(inputFilePath.resolve(fileName), PersonPropertyReportPluginDataInput.class)
                .addWriter(outputFilePath.resolve(fileName), PersonPropertyReportPluginData.class)
                .build();

        translatorController.readInput();

        PersonPropertyReportPluginData actualPluginData = translatorController
                .getObject(PersonPropertyReportPluginData.class);

        long seed = 4684903523797799712L;

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

        ReportLabel reportLabel = new SimpleReportLabel("property report label");
        ReportPeriod reportPeriod = ReportPeriod.DAILY;

        Set<TestPersonPropertyId> expectedPersonPropertyIds = EnumSet.allOf(TestPersonPropertyId.class);
        assertFalse(expectedPersonPropertyIds.isEmpty());

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

        assertEquals(expectedPluginData.getReportLabel(), actualPluginData.getReportLabel());
        assertEquals(expectedPluginData.getReportPeriod(), actualPluginData.getReportPeriod());

        assertEquals(expectedPluginData.getDefaultInclusionPolicy(),
                actualPluginData.getDefaultInclusionPolicy());

        assertEquals(expectedPluginData.getIncludedProperties(), actualPluginData.getIncludedProperties());
        assertEquals(expectedPluginData.getExcludedProperties(), actualPluginData.getExcludedProperties());

        translatorController.writeOutput();
    }

    @Test
    public void testPersonInteractionReportTranslatorSpec() {
        String fileName = "interactionReport.json";

        TranslatorController translatorController = TranslatorController.builder()
                .setTranslatorCoreBuilder(ProtobufTranslatorCore.builder())
                .addTranslator(PersonPropertiesTranslator.builder(true).build())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .addReader(inputFilePath.resolve(fileName), PersonPropertyInteractionReportPluginDataInput.class)
                .addWriter(outputFilePath.resolve(fileName), PersonPropertyInteractionReportPluginData.class)
                .build();

        translatorController.readInput();

        PersonPropertyInteractionReportPluginData actualPluginData = translatorController
                .getObject(PersonPropertyInteractionReportPluginData.class);

        long seed = 4684903523797799712L;
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

        ReportLabel reportLabel = new SimpleReportLabel("interaction report label");
        ReportPeriod reportPeriod = ReportPeriod.DAILY;

        PersonPropertyInteractionReportPluginData.Builder builder = PersonPropertyInteractionReportPluginData
                .builder();

        builder
                .setReportLabel(reportLabel)
                .setReportPeriod(reportPeriod);

        Set<TestPersonPropertyId> expectedPersonPropertyIds = EnumSet.allOf(TestPersonPropertyId.class);
        assertFalse(expectedPersonPropertyIds.isEmpty());

        for (PersonPropertyId personPropertyId : expectedPersonPropertyIds) {
            if (randomGenerator.nextBoolean()) {
                builder.addPersonPropertyId(personPropertyId);
            }
        }

        PersonPropertyInteractionReportPluginData expectedPluginData = builder.build();

        assertEquals(expectedPluginData.getReportLabel(), actualPluginData.getReportLabel());
        assertEquals(expectedPluginData.getReportPeriod(), actualPluginData.getReportPeriod());
        assertEquals(expectedPluginData.getPersonPropertyIds(), actualPluginData.getPersonPropertyIds());

        translatorController.writeOutput();
    }
}
