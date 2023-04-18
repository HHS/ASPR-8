package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.RegionsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.PersonResourceReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourcePropertyReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourceReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourcesPluginDataInput;
import gov.hhs.aspr.translation.core.TranslatorController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorCore;
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
    public void testResourcesTranslator() {
        String fileName = "pluginData.json";

        TranslatorController translatorController = TranslatorController.builder()
                .setTranslatorCoreBuilder(ProtobufTranslatorCore.builder())
                .addTranslator(ResourcesTranslator.getTranslator())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(RegionsTranslator.getTranslator())
                .addInputFilePath(inputFilePath.resolve(fileName), ResourcesPluginDataInput.class)
                .addOutputFilePath(outputFilePath.resolve(fileName), ResourcesPluginData.class)
                .build();

        translatorController.readInput();

        ResourcesPluginData resourcesPluginData = translatorController.getObject(ResourcesPluginData.class);
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
        String fileName = "personResourceReport.json";

        TranslatorController translatorController = TranslatorController.builder()
                .setTranslatorCoreBuilder(ProtobufTranslatorCore.builder())
                .addTranslator(ResourcesTranslator.getTranslatorWithReport())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(RegionsTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .addInputFilePath(inputFilePath.resolve(fileName),
                        PersonResourceReportPluginDataInput.class)
                .addOutputFilePath(outputFilePath.resolve(fileName),
                        PersonResourceReportPluginData.class)
                .build();

        translatorController.readInput();

        PersonResourceReportPluginData actualPluginData = translatorController
                .getObject(PersonResourceReportPluginData.class);
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
        String fileName = "resourcePropertyReport.json";

        TranslatorController translatorController = TranslatorController.builder()
                .setTranslatorCoreBuilder(ProtobufTranslatorCore.builder())
                .addTranslator(ResourcesTranslator.getTranslatorWithReport())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(RegionsTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .addInputFilePath(inputFilePath.resolve(fileName),
                        ResourcePropertyReportPluginDataInput.class)
                .addOutputFilePath(outputFilePath.resolve(fileName),
                        ResourcePropertyReportPluginData.class)
                .build();

        translatorController.readInput();

        ResourcePropertyReportPluginData actualPluginData = translatorController
                .getObject(ResourcePropertyReportPluginData.class);
        ReportLabel reportLabel = new SimpleReportLabel("resource property report label");

        ResourcePropertyReportPluginData.Builder builder = ResourcePropertyReportPluginData.builder();

        builder.setReportLabel(reportLabel);

        ResourcePropertyReportPluginData expectedPluginData = builder.build();

        assertEquals(expectedPluginData.getReportLabel(), actualPluginData.getReportLabel());
        translatorController.writeOutput();

    }

    @Test
    public void testResourceReportTranslatorSpec() {
        String fileName = "resourceReport.json";

        TranslatorController translatorController = TranslatorController.builder()
                .setTranslatorCoreBuilder(ProtobufTranslatorCore.builder())
                .addTranslator(ResourcesTranslator.getTranslatorWithReport())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(RegionsTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .addInputFilePath(inputFilePath.resolve(fileName),
                        ResourceReportPluginDataInput.class)
                .addOutputFilePath(outputFilePath.resolve(fileName),
                        ResourceReportPluginData.class)
                .build();

        translatorController.readInput();

        ResourceReportPluginData actualPluginData = translatorController.getObject(ResourceReportPluginData.class);
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
