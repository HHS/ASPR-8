package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.core.TranslatorController;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.RegionsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.PersonResourceReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourcePropertyReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourceReportPluginDataInput;
import nucleus.PluginData;
import plugins.people.support.PersonId;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;
import plugins.reports.support.SimpleReportLabel;
import plugins.resources.ResourcesPluginData;
import plugins.resources.reports.PersonResourceReportPluginData;
import plugins.resources.reports.ResourcePropertyReportPluginData;
import plugins.resources.reports.ResourceReportPluginData;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.TestResourceId;
import plugins.resources.testsupport.TestResourcePropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.TimeTrackingPolicy;
import util.random.RandomGeneratorProvider;

public class AppTest {

    @Test
    public void testResourcesTranslator() {
        Path basePath = Path.of("").toAbsolutePath();

        if (!basePath.endsWith("resources-plugin-translator")) {
            basePath = basePath.resolve("resources-plugin-translator");
        }

        Path inputFilePath = basePath.resolve("src/main/resources/json");
        Path outputFilePath = basePath.resolve("src/main/resources/json/output");

        outputFilePath.toFile().mkdir();

        String inputFileName = "input.json";
        String outputFileName = "output.json";

        TranslatorController translatorController = TranslatorController.builder()
                .addTranslator(ResourcesTranslator.getTranslatorRW(inputFilePath.resolve(inputFileName).toString(),
                        outputFilePath.resolve(outputFileName).toString()))
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(RegionsTranslator.getTranslatorModule())
                .build();

        List<PluginData> pluginDatas = translatorController.readInput().getPluginDatas();

        ResourcesPluginData resourcesPluginData = (ResourcesPluginData) pluginDatas.get(0);

        long seed = 524805676405822016L;
        List<PersonId> people = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            people.add(new PersonId(i));
        }

        Set<TestResourceId> expectedResourceIds = EnumSet.allOf(TestResourceId.class);
        assertFalse(expectedResourceIds.isEmpty());

        Set<ResourceId> actualResourceIds = resourcesPluginData.getResourceIds();
        assertEquals(expectedResourceIds, actualResourceIds);

        for (TestResourceId resourceId : TestResourceId.values()) {
            TimeTrackingPolicy expectedPolicy = resourceId.getTimeTrackingPolicy();
            TimeTrackingPolicy actualPolicy = resourcesPluginData.getPersonResourceTimeTrackingPolicy(resourceId);
            assertEquals(expectedPolicy, actualPolicy);
        }

        Set<TestResourcePropertyId> expectedResourcePropertyIds = EnumSet.allOf(TestResourcePropertyId.class);
        assertFalse(expectedResourcePropertyIds.isEmpty());

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
        for (TestResourcePropertyId resourcePropertyId : TestResourcePropertyId.values()) {
            TestResourceId expectedResourceId = resourcePropertyId.getTestResourceId();
            PropertyDefinition expectedPropertyDefinition = resourcePropertyId.getPropertyDefinition();
            Object expectedPropertyValue = resourcePropertyId.getRandomPropertyValue(randomGenerator);

            assertTrue(resourcesPluginData.getResourcePropertyIds(expectedResourceId).contains(resourcePropertyId));

            PropertyDefinition actualPropertyDefinition = resourcesPluginData
                    .getResourcePropertyDefinition(expectedResourceId, resourcePropertyId);
            assertEquals(expectedPropertyDefinition, actualPropertyDefinition);

            Object actualPropertyValue = resourcesPluginData.getResourcePropertyValue(expectedResourceId,
                    resourcePropertyId);
            assertEquals(expectedPropertyValue, actualPropertyValue);
        }

        translatorController.writeOutput();
    }

    @Test
    public void testPersonResourceReportTranslatorSpec() {
        Path basePath = Path.of("").toAbsolutePath();

        if (!basePath.endsWith("resources-plugin-translator")) {
            basePath = basePath.resolve("resources-plugin-translator");
        }

        Path inputFilePath = basePath.resolve("src/main/resources/json");
        Path outputFilePath = basePath.resolve("src/main/resources/json/output");

        outputFilePath.toFile().mkdir();

        String fileName = "personResourceReport.json";

        TranslatorController translatorController = TranslatorController.builder()
                .addTranslator(ResourcesTranslator.builder(true)
                        .addInputFile(inputFilePath.resolve(fileName).toString(),
                                PersonResourceReportPluginDataInput.getDefaultInstance())
                        .addOutputFile(outputFilePath.resolve(fileName).toString(),
                                PersonResourceReportPluginData.class)
                        .build())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(RegionsTranslator.getTranslatorModule())
                .addTranslator(ReportsTranslator.getTranslator())
                .build();

        List<PluginData> pluginDatas = translatorController.readInput().getPluginDatas();

        PersonResourceReportPluginData actualPluginData = (PersonResourceReportPluginData) pluginDatas.get(0);

        long seed = 524805676405822016L;
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

        ReportLabel reportLabel = new SimpleReportLabel("person resource report label");
        ReportPeriod reportPeriod = ReportPeriod.DAILY;

        PersonResourceReportPluginData.Builder builder = PersonResourceReportPluginData.builder();

        builder.setReportLabel(reportLabel).setReportPeriod(reportPeriod).setDefaultInclusion(false);

        Set<TestResourceId> expectedResourceIds = EnumSet.allOf(TestResourceId.class);
        assertFalse(expectedResourceIds.isEmpty());

        for (ResourceId resourceId : expectedResourceIds) {
            if (randomGenerator.nextBoolean()) {
                builder.includeResource(resourceId);
            } else {
                builder.excludeResource(resourceId);
            }
        }

        PersonResourceReportPluginData expectedPluginData = builder.build();

        assertEquals(expectedPluginData.getReportLabel(), actualPluginData.getReportLabel());
        assertEquals(expectedPluginData.getReportPeriod(), actualPluginData.getReportPeriod());
        assertEquals(expectedPluginData.getDefaultInclusionPolicy(), actualPluginData.getDefaultInclusionPolicy());
        assertEquals(expectedPluginData.getIncludedResourceIds(), actualPluginData.getIncludedResourceIds());
        assertEquals(expectedPluginData.getExcludedResourceIds(), actualPluginData.getExcludedResourceIds());

        translatorController.writeOutput();

    }

    @Test
    public void testResourcePropertyReportTranslatorSpec() {
        Path basePath = Path.of("").toAbsolutePath();

        if (!basePath.endsWith("resources-plugin-translator")) {
            basePath = basePath.resolve("resources-plugin-translator");
        }

        Path inputFilePath = basePath.resolve("src/main/resources/json");
        Path outputFilePath = basePath.resolve("src/main/resources/json/output");

        outputFilePath.toFile().mkdir();

        String fileName = "resourcePropertyReport.json";

        TranslatorController translatorController = TranslatorController.builder()
                .addTranslator(ResourcesTranslator.builder(true)
                        .addInputFile(inputFilePath.resolve(fileName).toString(),
                                ResourcePropertyReportPluginDataInput.getDefaultInstance())
                        .addOutputFile(outputFilePath.resolve(fileName).toString(),
                                ResourcePropertyReportPluginData.class)
                        .build())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(RegionsTranslator.getTranslatorModule())
                .addTranslator(ReportsTranslator.getTranslator())
                .build();

        List<PluginData> pluginDatas = translatorController.readInput().getPluginDatas();

        ResourcePropertyReportPluginData actualPluginData = (ResourcePropertyReportPluginData) pluginDatas.get(0);

        ReportLabel reportLabel = new SimpleReportLabel("resource property report label");

        ResourcePropertyReportPluginData.Builder builder = ResourcePropertyReportPluginData.builder();

        builder.setReportLabel(reportLabel);

        ResourcePropertyReportPluginData expectedPluginData = builder.build();

        assertEquals(expectedPluginData.getReportLabel(), actualPluginData.getReportLabel());
        translatorController.writeOutput();

    }

    @Test
    public void testResourceReportTranslatorSpec() {
        Path basePath = Path.of("").toAbsolutePath();

        if (!basePath.endsWith("resources-plugin-translator")) {
            basePath = basePath.resolve("resources-plugin-translator");
        }

        Path inputFilePath = basePath.resolve("src/main/resources/json");
        Path outputFilePath = basePath.resolve("src/main/resources/json/output");

        outputFilePath.toFile().mkdir();

        String fileName = "resourceReport.json";

        TranslatorController translatorController = TranslatorController.builder()
                .addTranslator(ResourcesTranslator.builder(true)
                        .addInputFile(inputFilePath.resolve(fileName).toString(),
                                ResourceReportPluginDataInput.getDefaultInstance())
                        .addOutputFile(outputFilePath.resolve(fileName).toString(),
                                ResourceReportPluginData.class)
                        .build())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(RegionsTranslator.getTranslatorModule())
                .addTranslator(ReportsTranslator.getTranslator())
                .build();

        List<PluginData> pluginDatas = translatorController.readInput().getPluginDatas();

        ResourceReportPluginData actualPluginData = (ResourceReportPluginData) pluginDatas.get(0);

        long seed = 524805676405822016L;
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

        ReportLabel reportLabel = new SimpleReportLabel("resource report label");
        ReportPeriod reportPeriod = ReportPeriod.DAILY;

        ResourceReportPluginData.Builder builder = ResourceReportPluginData.builder();

        builder.setReportLabel(reportLabel).setReportPeriod(reportPeriod).setDefaultInclusion(false);

        Set<TestResourceId> expectedResourceIds = EnumSet.allOf(TestResourceId.class);
        assertFalse(expectedResourceIds.isEmpty());

        for (ResourceId resourceId : expectedResourceIds) {
            if (randomGenerator.nextBoolean()) {
                builder.includeResource(resourceId);
            } else {
                builder.excludeResource(resourceId);
            }
        }

        ResourceReportPluginData expectedPluginData = builder.build();

        assertEquals(expectedPluginData.getReportLabel(), actualPluginData.getReportLabel());
        assertEquals(expectedPluginData.getReportPeriod(), actualPluginData.getReportPeriod());
        assertEquals(expectedPluginData.getDefaultInclusionPolicy(), actualPluginData.getDefaultInclusionPolicy());
        assertEquals(expectedPluginData.getIncludedResourceIds(), actualPluginData.getIncludedResourceIds());
        assertEquals(expectedPluginData.getExcludedResourceIds(), actualPluginData.getExcludedResourceIds());

        translatorController.writeOutput();

    }

}
