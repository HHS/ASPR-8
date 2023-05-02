package gov.hhs.aspr.gcm.translation.protobuf.plugins.regions;

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
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.RegionPropertyReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.RegionTransferReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.RegionsPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import gov.hhs.aspr.translation.core.testsupport.TestResourceHelper;
import plugins.people.support.PersonId;
import plugins.regions.RegionsPluginData;
import plugins.regions.reports.RegionPropertyReportPluginData;
import plugins.regions.reports.RegionTransferReportPluginData;
import plugins.regions.support.RegionPropertyId;
import plugins.regions.testsupport.RegionsTestPluginFactory;
import plugins.regions.testsupport.TestRegionPropertyId;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;
import plugins.reports.support.SimpleReportLabel;
import plugins.util.properties.TimeTrackingPolicy;
import util.random.RandomGeneratorProvider;

public class AppTest {
    Path basePath = TestResourceHelper.getResourceDir(this.getClass());
    Path filePath = TestResourceHelper.makeTestOutputDir(basePath);

    @Test
    public void testRegionsTranslator() {
        String fileName = "pluginData.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);

        TranslationController translatorController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(RegionsTranslator.getTranslator())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addInputFilePath(filePath.resolve(fileName), RegionsPluginDataInput.class)
                .addOutputFilePath(filePath.resolve(fileName), RegionsPluginData.class)
                .build();

        long seed = 524805676405822016L;
        int initialPopulation = 100;
        List<PersonId> people = new ArrayList<>();

        for (int i = 0; i < initialPopulation; i++) {
            people.add(new PersonId(i));
        }

        RegionsPluginData expectedPluginData = RegionsTestPluginFactory.getStandardRegionsPluginData(people,
                TimeTrackingPolicy.TRACK_TIME, seed);

        translatorController.writeOutput(expectedPluginData);
        translatorController.readInput();

        RegionsPluginData actualPluginData = translatorController.getFirstObject(RegionsPluginData.class);

        assertEquals(expectedPluginData, actualPluginData);
    }

    @Test
    public void testRegionPropertyReportTranslatorSpec() {
        String fileName = "propertyReport.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);

        TranslationController translatorController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(RegionsTranslator.getTranslatorWithReport())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .addInputFilePath(filePath.resolve(fileName),
                        RegionPropertyReportPluginDataInput.class)
                .addOutputFilePath(filePath.resolve(fileName),
                        RegionPropertyReportPluginData.class)
                .build();

        long seed = 524805676405822016L;
        ReportLabel reportLabel = new SimpleReportLabel("region property report label");

        RegionPropertyReportPluginData.Builder builder = RegionPropertyReportPluginData.builder()
                .setReportLabel(reportLabel)
                .setDefaultInclusion(false);

        Set<TestRegionPropertyId> expectedRegionPropertyIds = EnumSet.allOf(TestRegionPropertyId.class);
        assertFalse(expectedRegionPropertyIds.isEmpty());

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

        for (RegionPropertyId regionPropertyId : TestRegionPropertyId.values()) {
            if (randomGenerator.nextBoolean()) {
                builder.includeRegionProperty(regionPropertyId);
            } else {
                builder.excludeRegionProperty(regionPropertyId);
            }
        }

        RegionPropertyReportPluginData expectedPluginData = builder.build();

        translatorController.writeOutput(expectedPluginData);
        translatorController.readInput();

        RegionPropertyReportPluginData actualPluginData = translatorController
                .getFirstObject(RegionPropertyReportPluginData.class);

        assertEquals(expectedPluginData, actualPluginData);
    }

    @Test
    public void testRegionTransferReportTranslatorSpec() {
        String fileName = "transferReport.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);

        TranslationController translatorController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(RegionsTranslator.getTranslatorWithReport())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .addInputFilePath(filePath.resolve(fileName),
                        RegionTransferReportPluginDataInput.class)
                .addOutputFilePath(filePath.resolve(fileName),
                        RegionTransferReportPluginData.class)
                .build();

        ReportLabel reportLabel = new SimpleReportLabel("region transfer report label");
        ReportPeriod reportPeriod = ReportPeriod.DAILY;

        RegionTransferReportPluginData.Builder builder = RegionTransferReportPluginData.builder();

        builder.setReportLabel(reportLabel).setReportPeriod(reportPeriod);

        RegionTransferReportPluginData expectedPluginData = builder.build();

        translatorController.writeOutput(expectedPluginData);
        translatorController.readInput();

        RegionTransferReportPluginData actualPluginData = translatorController
                .getFirstObject(RegionTransferReportPluginData.class);

        assertEquals(expectedPluginData, actualPluginData);

    }
}
