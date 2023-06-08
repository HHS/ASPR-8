package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import gov.hhs.aspr.translation.core.testsupport.TestResourceHelper;
import plugins.people.support.PersonId;
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
import util.annotations.UnitTestForCoverage;
import util.random.RandomGeneratorProvider;

public class IT_ResourcesTranslator {
    Path basePath = TestResourceHelper.getResourceDir(this.getClass());
    Path filePath = TestResourceHelper.makeTestOutputDir(basePath);

    @Test
    @UnitTestForCoverage
    public void testResourcesTranslator() {
        String fileName = "pluginData.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);

        TranslationController translatorController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(ResourcesTranslator.getTranslator())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(RegionsTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .addInputFilePath(filePath.resolve(fileName), ResourcesPluginDataInput.class)
                .addOutputFilePath(filePath.resolve(fileName), ResourcesPluginData.class)
                .build();

        long seed = 524805676405822016L;
        int initialPopulation = 100;
        List<PersonId> people = new ArrayList<>();

        for (int i = 0; i < initialPopulation; i++) {
            people.add(new PersonId(i));
        }
        
        ResourcesPluginData expectedPluginData = ResourcesTestPluginFactory
                .getStandardResourcesPluginData(people, seed);

        translatorController.writeOutput(expectedPluginData);
        translatorController.readInput();

        ResourcesPluginData actualPluginData = translatorController.getFirstObject(ResourcesPluginData.class);

        assertEquals(expectedPluginData, actualPluginData);
    }

    @Test
    @UnitTestForCoverage
    public void testPersonResourceReportTranslatorSpec() {
        String fileName = "personResourceReport.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);

        TranslationController translatorController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(ResourcesTranslator.getTranslator())
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
                .getFirstObject(PersonResourceReportPluginData.class);

        assertEquals(expectedPluginData, actualPluginData);

    }

    @Test
    @UnitTestForCoverage
    public void testResourcePropertyReportTranslatorSpec() {
        String fileName = "resourcePropertyReport.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);

        TranslationController translatorController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(ResourcesTranslator.getTranslator())
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
                .getFirstObject(ResourcePropertyReportPluginData.class);

        assertEquals(expectedPluginData, actualPluginData);
    }

    @Test
    @UnitTestForCoverage
    public void testResourceReportTranslatorSpec() {
        String fileName = "resourceReport.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);

        TranslationController translatorController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(ResourcesTranslator.getTranslator())
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

        ResourceReportPluginData actualPluginData = translatorController
                .getFirstObject(ResourceReportPluginData.class);

        assertEquals(expectedPluginData, actualPluginData);
    }

}
