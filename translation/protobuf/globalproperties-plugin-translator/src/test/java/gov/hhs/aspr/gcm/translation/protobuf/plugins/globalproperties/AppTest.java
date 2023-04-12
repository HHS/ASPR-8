package gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.core.TranslatorController;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.input.GlobalPropertiesPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.input.GlobalPropertyReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslator;
import nucleus.PluginData;
import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.globalproperties.reports.GlobalPropertyReportPluginData;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.globalproperties.testsupport.TestGlobalPropertyId;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.SimpleReportLabel;
import plugins.util.properties.PropertyDefinition;
import util.random.RandomGeneratorProvider;

public class AppTest {

    @Test
    public void testGlobalPropertiesPluginDataTranslatorSpec() {

        Path basePath = Path.of("").toAbsolutePath();

        if (!basePath.endsWith("globalproperties-plugin-translator")) {
            basePath = basePath.resolve("globalproperties-plugin-translator");
        }
        Path inputFilePath = basePath.resolve("src/main/resources/json");
        Path outputFilePath = basePath.resolve("src/main/resources/json/output");

        outputFilePath.toFile().mkdir();

        String fileName = "pluginData.json";

        // .toString(),
        //                         outputFilePath.resolve(fileName).toString()

        TranslatorController translatorController = TranslatorController.builder()
                .addTranslator(
                        GlobalPropertiesTranslator.getTranslator())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addReader(inputFilePath.resolve(fileName), GlobalPropertiesPluginDataInput.class)
                .build();

        List<PluginData> pluginDatas = translatorController.readInput().getPluginDatas();

        GlobalPropertiesPluginData actualPluginData = (GlobalPropertiesPluginData) pluginDatas.get(0);

        Set<TestGlobalPropertyId> expectedPropertyIds = EnumSet.allOf(TestGlobalPropertyId.class);

        Set<GlobalPropertyId> actualGlobalPropertyIds = actualPluginData.getGlobalPropertyIds();
        assertEquals(expectedPropertyIds, actualGlobalPropertyIds);

        for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
            PropertyDefinition expectedPropertyDefinition = testGlobalPropertyId.getPropertyDefinition();
            PropertyDefinition actualPropertyDefinition = actualPluginData
                    .getGlobalPropertyDefinition(testGlobalPropertyId);

            assertEquals(expectedPropertyDefinition, actualPropertyDefinition);

        }

        // translatorController.writeOutput();
    }

    @Test
    public void testGlobalPropertyReportTranslatorSpec() {

        Path basePath = Path.of("").toAbsolutePath();

        if (!basePath.endsWith("globalproperties-plugin-translator")) {
            basePath = basePath.resolve("globalproperties-plugin-translator");
        }
        Path inputFilePath = basePath.resolve("src/main/resources/json");
        Path outputFilePath = basePath.resolve("src/main/resources/json/output");

        outputFilePath.toFile().mkdir();

        String fileName = "propertyReport.json";

        // .addInputFile(inputFilePath.resolve(fileName).toString(),
        //                         GlobalPropertyReportPluginDataInput.getDefaultInstance())
        //                 .addOutputFile(outputFilePath.resolve(fileName).toString(),
        //                         GlobalPropertyReportPluginData.class)

        TranslatorController translatorController = TranslatorController.builder()
                .addTranslator(GlobalPropertiesTranslator.builder(true)
                        .build())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .addReader(inputFilePath.resolve(fileName), GlobalPropertyReportPluginDataInput.class)
                .build();

        List<PluginData> pluginDatas = translatorController.readInput().getPluginDatas();

        GlobalPropertyReportPluginData actualPluginData = (GlobalPropertyReportPluginData) pluginDatas.get(0);

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(524805676405822016L);
        GlobalPropertyReportPluginData.Builder builder = GlobalPropertyReportPluginData.builder();

        ReportLabel reportLabel = new SimpleReportLabel("report label");

        builder.setDefaultInclusion(false).setReportLabel(reportLabel);

        for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
            if (randomGenerator.nextBoolean()) {
                builder.includeGlobalProperty(testGlobalPropertyId);
            } else {
                builder.excludeGlobalProperty(testGlobalPropertyId);
            }
        }

        GlobalPropertyReportPluginData expectedPluginData = builder.build();

        assertEquals(expectedPluginData.getReportLabel(), actualPluginData.getReportLabel());
        assertEquals(expectedPluginData.getDefaultInclusionPolicy(), actualPluginData.getDefaultInclusionPolicy());
        assertEquals(expectedPluginData.getIncludedProperties(), actualPluginData.getIncludedProperties());
        assertEquals(expectedPluginData.getExcludedProperties(), actualPluginData.getExcludedProperties());

        translatorController.writeOutput();
    }
}
