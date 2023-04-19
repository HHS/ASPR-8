package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.EnumSet;
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
import gov.hhs.aspr.translation.protobuf.core.testsupport.TestResourceHelper;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;
import plugins.reports.support.SimpleReportLabel;
import plugins.resources.ResourcesPluginData;
import plugins.resources.reports.PersonResourceReportPluginData;
import plugins.resources.reports.ResourcePropertyReportPluginData;
import plugins.resources.reports.ResourceReportPluginData;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.ResourcesTestPluginFactory;
import plugins.resources.testsupport.TestResourceId;
import plugins.resources.testsupport.TestResourcePropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.TimeTrackingPolicy;
import util.random.RandomGeneratorProvider;

public class AppTest {
    Path basePath = TestResourceHelper.getResourceDir(this.getClass());
    Path filePath = TestResourceHelper.makeTestOutputDir(basePath);

    @Test
    public void testResourcesTranslator() {
        String fileName = "pluginData.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);

        TranslatorController translatorController = TranslatorController.builder()
                .setTranslatorCoreBuilder(ProtobufTranslatorCore.builder())
                .addTranslator(ResourcesTranslator.getTranslator())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(RegionsTranslator.getTranslator())
                .addInputFilePath(filePath.resolve(fileName), ResourcesPluginDataInput.class)
                .addOutputFilePath(filePath.resolve(fileName), ResourcesPluginData.class)
                .build();

        long seed = 524805676405822016L;

        ResourcesPluginData expectedPluginData = ResourcesTestPluginFactory.getStandardResourcesPluginData(seed);

        translatorController.writeOutput(expectedPluginData);
        translatorController.readInput();

        ResourcesPluginData actualPluginData = translatorController.getObject(ResourcesPluginData.class);

        Set<TestResourceId> expectedResourceIds = EnumSet.allOf(TestResourceId.class);
        assertFalse(expectedResourceIds.isEmpty());

        Set<ResourceId> actualResourceIds = actualPluginData.getResourceIds();
        assertEquals(expectedResourceIds, actualResourceIds);

        for (TestResourceId resourceId : TestResourceId.values()) {
            TimeTrackingPolicy expectedPolicy = resourceId.getTimeTrackingPolicy();
            TimeTrackingPolicy actualPolicy = actualPluginData.getPersonResourceTimeTrackingPolicy(resourceId);
            assertEquals(expectedPolicy, actualPolicy);
        }

        Set<TestResourcePropertyId> expectedResourcePropertyIds = EnumSet.allOf(TestResourcePropertyId.class);
        assertFalse(expectedResourcePropertyIds.isEmpty());

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
        for (TestResourcePropertyId resourcePropertyId : TestResourcePropertyId.values()) {
            TestResourceId expectedResourceId = resourcePropertyId.getTestResourceId();
            PropertyDefinition expectedPropertyDefinition = resourcePropertyId.getPropertyDefinition();
            Object expectedPropertyValue = resourcePropertyId.getRandomPropertyValue(randomGenerator);

            assertTrue(actualPluginData.getResourcePropertyIds(expectedResourceId).contains(resourcePropertyId));

            PropertyDefinition actualPropertyDefinition = actualPluginData
                    .getResourcePropertyDefinition(expectedResourceId, resourcePropertyId);
            assertEquals(expectedPropertyDefinition, actualPropertyDefinition);

            Object actualPropertyValue = actualPluginData.getResourcePropertyValue(expectedResourceId,
                    resourcePropertyId);
            assertEquals(expectedPropertyValue, actualPropertyValue);
        }

        // TODO: fix equals contract
        // assertEquals(expectedPluginData, actualPluginData);
    }

    @Test
    public void testPersonResourceReportTranslatorSpec() {
        String fileName = "personResourceReport.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);

        TranslatorController translatorController = TranslatorController.builder()
                .setTranslatorCoreBuilder(ProtobufTranslatorCore.builder())
                .addTranslator(ResourcesTranslator.getTranslatorWithReport())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(RegionsTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .addInputFilePath(filePath.resolve(fileName),
                        PersonResourceReportPluginDataInput.class)
                .addOutputFilePath(filePath.resolve(fileName),
                        PersonResourceReportPluginData.class)
                .build();

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

        translatorController.writeOutput(expectedPluginData);
        translatorController.readInput();

        PersonResourceReportPluginData actualPluginData = translatorController
                .getObject(PersonResourceReportPluginData.class);

        assertEquals(expectedPluginData, actualPluginData);

    }

    @Test
    public void testResourcePropertyReportTranslatorSpec() {
        String fileName = "resourcePropertyReport.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);

        TranslatorController translatorController = TranslatorController.builder()
                .setTranslatorCoreBuilder(ProtobufTranslatorCore.builder())
                .addTranslator(ResourcesTranslator.getTranslatorWithReport())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(RegionsTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .addInputFilePath(filePath.resolve(fileName),
                        ResourcePropertyReportPluginDataInput.class)
                .addOutputFilePath(filePath.resolve(fileName),
                        ResourcePropertyReportPluginData.class)
                .build();

        ReportLabel reportLabel = new SimpleReportLabel("resource property report label");

        ResourcePropertyReportPluginData.Builder builder = ResourcePropertyReportPluginData.builder();

        builder.setReportLabel(reportLabel);

        ResourcePropertyReportPluginData expectedPluginData = builder.build();

        translatorController.writeOutput(expectedPluginData);
        translatorController.readInput();

        ResourcePropertyReportPluginData actualPluginData = translatorController
                .getObject(ResourcePropertyReportPluginData.class);

        assertEquals(expectedPluginData, actualPluginData);
    }

    @Test
    public void testResourceReportTranslatorSpec() {
        String fileName = "resourceReport.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);

        TranslatorController translatorController = TranslatorController.builder()
                .setTranslatorCoreBuilder(ProtobufTranslatorCore.builder())
                .addTranslator(ResourcesTranslator.getTranslatorWithReport())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(RegionsTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .addInputFilePath(filePath.resolve(fileName),
                        ResourceReportPluginDataInput.class)
                .addOutputFilePath(filePath.resolve(fileName),
                        ResourceReportPluginData.class)
                .build();

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

        translatorController.writeOutput(expectedPluginData);
        translatorController.readInput();

        ResourceReportPluginData actualPluginData = translatorController.getObject(ResourceReportPluginData.class);

        assertEquals(expectedPluginData, actualPluginData);
    }

}
