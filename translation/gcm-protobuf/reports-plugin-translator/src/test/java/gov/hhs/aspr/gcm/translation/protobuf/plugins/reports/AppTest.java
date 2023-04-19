package gov.hhs.aspr.gcm.translation.protobuf.plugins.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportLabelInput;
import gov.hhs.aspr.translation.core.TranslatorController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorCore;
import gov.hhs.aspr.translation.protobuf.core.testsupport.TestResourceHelper;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.SimpleReportLabel;

public class AppTest {
    Path basePath = TestResourceHelper.getResourceDir(this.getClass());
    Path filePath = TestResourceHelper.makeTestOutputDir(basePath);

    @Test
    public void testReportLabelTranslatorSpec() {
        String fileName = "reportLabel.json";
        
        TestResourceHelper.createTestOutputFile(filePath, fileName);

        TranslatorController translatorController = TranslatorController.builder()
                .setTranslatorCoreBuilder(ProtobufTranslatorCore.builder())
                .addTranslator(ReportsTranslator.getTranslator())
                .addInputFilePath(filePath.resolve(fileName), ReportLabelInput.class)
                .addOutputFilePath(filePath.resolve(fileName), ReportLabel.class)
                .build();
        
        ReportLabel expecetdReportLabel = new SimpleReportLabel("report label");

        translatorController.writeOutput(expecetdReportLabel);

        translatorController.readInput();

        ReportLabel actualReportLabel = translatorController.getObject(ReportLabel.class);

        assertEquals(expecetdReportLabel, actualReportLabel);

    }
}
