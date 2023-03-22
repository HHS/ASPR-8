package gov.hss.aspr.gcm.translation.protobuf.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.nucleus.input.SimulationTimeInput;
import gov.hhs.aspr.gcm.translation.protobuf.core.TranslatorController;
import nucleus.SimulationTime;

public class AppTest {

    @Test
    public void testSimulationTimeTranslator(String[] args) {
        String inputFileName = "./nucleus/src/main/resources/json/simulationTimeInput.json";
        String outputFileName = "./nucleus/src/main/resources/json/output/simulationTimeOutput.json";

        TranslatorController translatorController = TranslatorController.builder()
                .addTranslator(NucleusTranslator.builder()
                        .addInputFile(inputFileName, SimulationTimeInput.getDefaultInstance())
                        .addOutputFile(outputFileName, SimulationTime.class).build())
                .build();

        List<Object> objects = translatorController.readInput().getObjects();

        SimulationTime actualSimulationTime = (SimulationTime) objects.get(0);

        SimulationTime exptectedSimulationTime = SimulationTime.builder().setBaseDate(LocalDate.of(2023, 3, 15))
                .setStartTime(0.0).build();

        assertEquals(exptectedSimulationTime, actualSimulationTime);

        translatorController.writeOutput();
    }

}
