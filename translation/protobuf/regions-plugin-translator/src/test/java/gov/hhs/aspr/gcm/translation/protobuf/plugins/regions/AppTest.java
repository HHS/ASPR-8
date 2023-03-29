package gov.hhs.aspr.gcm.translation.protobuf.plugins.regions;

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
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.RegionPropertyReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.RegionTransferReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslator;
import nucleus.PluginData;
import plugins.people.support.PersonId;
import plugins.regions.RegionsPluginData;
import plugins.regions.reports.RegionPropertyReportPluginData;
import plugins.regions.reports.RegionTransferReportPluginData;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.regions.testsupport.TestRegionId;
import plugins.regions.testsupport.TestRegionPropertyId;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;
import plugins.reports.support.SimpleReportLabel;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.TimeTrackingPolicy;
import util.random.RandomGeneratorProvider;

public class AppTest {

    @Test
    public void testRegionsTranslator() {
        Path basePath = Path.of("").toAbsolutePath();

        if (!basePath.endsWith("regions-plugin-translator")) {
            basePath = basePath.resolve("regions-plugin-translator");
        }

        Path inputFilePath = basePath.resolve("src/main/resources/json");
        Path outputFilePath = basePath.resolve("src/main/resources/json/output");

        outputFilePath.toFile().mkdir();

        String inputFileName = "input.json";
        String outputFileName = "output.json";

        TranslatorController translatorController = TranslatorController.builder()
                .addTranslator(RegionsTranslator.getTranslatorRW(inputFilePath.resolve(inputFileName).toString(),
                        outputFilePath.resolve(outputFileName).toString()))
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .build();

        List<PluginData> pluginDatas = translatorController.readInput().getPluginDatas();

        RegionsPluginData regionsPluginData = (RegionsPluginData) pluginDatas.get(0);

        long seed = 524805676405822016L;
        int initialPopulation = 100;
        List<PersonId> people = new ArrayList<>();

        for (int i = 0; i < initialPopulation; i++) {
            people.add(new PersonId(i));
        }

        Set<TestRegionId> expectedRegionIds = EnumSet.allOf(TestRegionId.class);
        assertFalse(expectedRegionIds.isEmpty());

        Set<RegionId> actualRegionIds = regionsPluginData.getRegionIds();
        assertEquals(expectedRegionIds, actualRegionIds);

        Set<TestRegionPropertyId> expectedRegionPropertyIds = EnumSet.allOf(TestRegionPropertyId.class);
        assertFalse(expectedRegionPropertyIds.isEmpty());

        Set<RegionPropertyId> actualRegionPropertyIds = regionsPluginData.getRegionPropertyIds();
        assertEquals(expectedRegionPropertyIds, actualRegionPropertyIds);

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

        for (TestRegionPropertyId expectedRegionPropertyId : TestRegionPropertyId.values()) {
            PropertyDefinition expectedPropertyDefinition = expectedRegionPropertyId.getPropertyDefinition();
            PropertyDefinition actualPropertyDefinition = regionsPluginData
                    .getRegionPropertyDefinition(expectedRegionPropertyId);
            assertEquals(expectedPropertyDefinition, actualPropertyDefinition);

            if (expectedPropertyDefinition.getDefaultValue().isEmpty()) {
                for (TestRegionId regionId : TestRegionId.values()) {
                    Object expectedPropertyValue = expectedRegionPropertyId.getRandomPropertyValue(randomGenerator);
                    regionsPluginData.getRegionPropertyValues(regionId);
                    assertTrue(
                            regionsPluginData.getRegionPropertyValues(regionId).containsKey(expectedRegionPropertyId));
                    assertEquals(expectedPropertyValue,
                            regionsPluginData.getRegionPropertyValues(regionId).get(expectedRegionPropertyId));
                }
            }

        }
        assertEquals(TimeTrackingPolicy.TRACK_TIME, regionsPluginData.getPersonRegionArrivalTrackingPolicy());

        assertEquals(initialPopulation, regionsPluginData.getPersonCount());

        TestRegionId regionId = TestRegionId.REGION_1;
        for (PersonId personId : people) {
            assertTrue(regionsPluginData.getPersonRegion(personId).isPresent());
            assertEquals(regionId, regionsPluginData.getPersonRegion(personId).get());
            regionId = regionId.next();
        }

        translatorController.writeOutput();
    }

    @Test
    public void testRegionPropertyReportTranslatorSpec() {
        Path basePath = Path.of("").toAbsolutePath();

        if (!basePath.endsWith("regions-plugin-translator")) {
            basePath = basePath.resolve("regions-plugin-translator");
        }

        Path inputFilePath = basePath.resolve("src/main/resources/json");
        Path outputFilePath = basePath.resolve("src/main/resources/json/output");

        outputFilePath.toFile().mkdir();

        String fileName = "propertyReport.json";

        TranslatorController translatorController = TranslatorController.builder()
                .addTranslator(RegionsTranslator
                        .builder(true)
                        .addInputFile(inputFilePath.resolve(fileName).toString(),
                                RegionPropertyReportPluginDataInput.getDefaultInstance())
                        .addOutputFile(outputFilePath.resolve(fileName).toString(),
                                RegionPropertyReportPluginData.class)
                        .build())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .build();

        List<PluginData> pluginDatas = translatorController.readInput().getPluginDatas();

        RegionPropertyReportPluginData actualPluginData = (RegionPropertyReportPluginData) pluginDatas.get(0);

        long seed = 524805676405822016L;

        Set<TestRegionPropertyId> expectedRegionPropertyIds = EnumSet.allOf(TestRegionPropertyId.class);
        assertFalse(expectedRegionPropertyIds.isEmpty());

        ReportLabel reportLabel = new SimpleReportLabel("region property report label");

        RegionPropertyReportPluginData.Builder builder = RegionPropertyReportPluginData.builder();

        builder
                .setReportLabel(reportLabel)
                .setDefaultInclusion(false);

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

        for (RegionPropertyId regionPropertyId : TestRegionPropertyId.values()) {
            if (randomGenerator.nextBoolean()) {
                builder.includeRegionProperty(regionPropertyId);
            } else {
                builder.excludeRegionProperty(regionPropertyId);
            }
        }

        RegionPropertyReportPluginData expectedPluginData = builder.build();

        assertEquals(expectedPluginData.getReportLabel(), actualPluginData.getReportLabel());
        assertEquals(expectedPluginData.getDefaultInclusionPolicy(), actualPluginData.getDefaultInclusionPolicy());
        assertEquals(expectedPluginData.getIncludedProperties(), actualPluginData.getIncludedProperties());
        assertEquals(expectedPluginData.getExcludedProperties(), actualPluginData.getExcludedProperties());

        translatorController.writeOutput();

    }

    @Test
    public void testRegionTransferReportTranslatorSpec() {
        Path basePath = Path.of("").toAbsolutePath();

        if (!basePath.endsWith("regions-plugin-translator")) {
            basePath = basePath.resolve("regions-plugin-translator");
        }

        Path inputFilePath = basePath.resolve("src/main/resources/json");
        Path outputFilePath = basePath.resolve("src/main/resources/json/output");

        outputFilePath.toFile().mkdir();

        String fileName = "transferReport.json";

        TranslatorController translatorController = TranslatorController.builder()
                .addTranslator(RegionsTranslator
                        .builder(true)
                        .addInputFile(inputFilePath.resolve(fileName).toString(),
                                RegionTransferReportPluginDataInput.getDefaultInstance())
                        .addOutputFile(outputFilePath.resolve(fileName).toString(),
                                RegionTransferReportPluginData.class)
                        .build())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .build();

        List<PluginData> pluginDatas = translatorController.readInput().getPluginDatas();

        RegionTransferReportPluginData actualPluginData = (RegionTransferReportPluginData) pluginDatas.get(0);

        ReportLabel reportLabel = new SimpleReportLabel("region transfer report label");
        ReportPeriod reportPeriod = ReportPeriod.DAILY;

        RegionTransferReportPluginData.Builder builder = RegionTransferReportPluginData.builder();

        builder.setReportLabel(reportLabel).setReportPeriod(reportPeriod);

        RegionTransferReportPluginData expectedPluginData = builder.build();

        assertEquals(expectedPluginData.getReportLabel(), actualPluginData.getReportLabel());
        assertEquals(expectedPluginData.getReportPeriod(), actualPluginData.getReportPeriod());

        translatorController.writeOutput();

    }
}
