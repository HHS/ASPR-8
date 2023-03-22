package gov.hss.aspr.gcm.translation.protobuf.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.nucleus.input.SimulationTimeInput;
import gov.hhs.aspr.gcm.translation.protobuf.core.TranslatorController;
import nucleus.SimulationTime;

public class AppTest {

    @Test
    public void testSimulationTimeTranslator() {
        Path basePath = Path.of("").toAbsolutePath();

        if (!basePath.endsWith("nucleus")) {
            basePath = basePath.resolve("nucleus");
        }

        Path inputFilePath = basePath.resolve("src/main/resources/json/simulationTimeInput.json");
        Path outputFilePath = basePath.resolve("src/main/resources/json/output/simulationTimeOutput.json");


        System.out.println("Working Directory = " + System.getProperty("user.dir"));

        TranslatorController translatorController = TranslatorController.builder()
                .addTranslator(NucleusTranslator.builder()
                        .addInputFile(inputFilePath.toString(), SimulationTimeInput.getDefaultInstance())
                        .addOutputFile(outputFilePath.toString(), SimulationTime.class).build())
                .build();

        List<Object> objects = translatorController.readInput().getObjects();

        SimulationTime actualSimulationTime = (SimulationTime) objects.get(0);

        SimulationTime exptectedSimulationTime = SimulationTime.builder().setBaseDate(LocalDate.of(2023, 3, 15))
                .setStartTime(0.0).build();

        assertEquals(exptectedSimulationTime.getBaseDate(), actualSimulationTime.getBaseDate());
        assertEquals(exptectedSimulationTime.getStartTime(), actualSimulationTime.getStartTime());

        translatorController.writeOutput();
    }

}
