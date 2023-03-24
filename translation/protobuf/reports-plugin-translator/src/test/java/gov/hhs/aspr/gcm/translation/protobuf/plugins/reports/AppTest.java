package gov.hhs.aspr.gcm.translation.protobuf.plugins.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.core.TranslatorController;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportLabelInput;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.SimpleReportLabel;

public class AppTest {

    @Test
    public void testPropertyValueMapTranslator() {

        Path basePath = Path.of("").toAbsolutePath();

        if (!basePath.endsWith("reports-plugin-translator")) {
            basePath = basePath.resolve("reports-plugin-translator");
        }

        Path inputFilePath = basePath.resolve("src/main/resources/json");
        Path outputFilePath = basePath.resolve("src/main/resources/json/output");
        
        outputFilePath.toFile().mkdir();

        String reportLabelInputFileName = "reportLabelInput.json";
        String reportLabelOutputFileName = "reportLabelOutput.json";

        TranslatorController translatorController = TranslatorController.builder()
                .addTranslator(ReportsTranslator.builder()
                        .addInputFile(inputFilePath.resolve(reportLabelInputFileName).toString(), ReportLabelInput.getDefaultInstance())
                        .addOutputFile(outputFilePath.resolve(reportLabelOutputFileName).toString(), ReportLabel.class)
                        .build())
                .build();

        List<Object> objects = translatorController.readInput().getObjects();

        ReportLabel label = (ReportLabel) objects.get(0);


        assertEquals(new SimpleReportLabel("report label"), label);

        translatorController.writeOutput();

    }
}
