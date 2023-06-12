package gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.input.GlobalPropertiesPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.input.GlobalPropertyReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import gov.hhs.aspr.translation.core.testsupport.TestResourceHelper;
import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.globalproperties.reports.GlobalPropertyReportPluginData;
import plugins.globalproperties.testsupport.GlobalPropertiesTestPluginFactory;
import plugins.globalproperties.testsupport.TestGlobalPropertyId;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.SimpleReportLabel;
import util.annotations.UnitTestForCoverage;
import util.random.RandomGeneratorProvider;

public class IT_GlobalPropertiesTranslator {
    Path basePath = TestResourceHelper.getResourceDir(this.getClass());
    Path filePath = TestResourceHelper.makeTestOutputDir(basePath);

    @Test
    @UnitTestForCoverage
    public void testGlobalPropertiesPluginDataIntegration() {
        String fileName = "pluginData.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);

        TranslationController translatorController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(
                        GlobalPropertiesTranslator.getTranslator())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .addInputFilePath(filePath.resolve(fileName), GlobalPropertiesPluginDataInput.class)
                .addOutputFilePath(filePath.resolve(fileName), GlobalPropertiesPluginData.class)
                .build();

        GlobalPropertiesPluginData expectedPluginData = GlobalPropertiesTestPluginFactory
                .getStandardGlobalPropertiesPluginData(8368397106493368066L);

        translatorController.writeOutput(expectedPluginData);

        translatorController.readInput();

        GlobalPropertiesPluginData actualPluginData = translatorController
                .getFirstObject(GlobalPropertiesPluginData.class);

        assertEquals(expectedPluginData, actualPluginData);

    }

    @Test
    @UnitTestForCoverage
    public void testGlobalPropertyReportPluginDataIntegration() {
        String fileName = "propertyReport.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);

        TranslationController translatorController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(GlobalPropertiesTranslator.getTranslator())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .addInputFilePath(filePath.resolve(fileName), GlobalPropertyReportPluginDataInput.class)
                .addOutputFilePath(filePath.resolve(fileName), GlobalPropertyReportPluginData.class)
                .build();

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

        translatorController.writeOutput(expectedPluginData);

        translatorController.readInput();

        GlobalPropertyReportPluginData actualPluginData = translatorController
                .getFirstObject(GlobalPropertyReportPluginData.class);

        assertEquals(expectedPluginData, actualPluginData);
    }
}
