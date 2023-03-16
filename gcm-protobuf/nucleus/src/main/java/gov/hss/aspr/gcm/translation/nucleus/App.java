package gov.hss.aspr.gcm.translation.nucleus;

import java.util.List;

import gov.hhs.aspr.gcm.translation.core.TranslatorController;
import gov.hhs.aspr.gcm.translation.nucleus.input.SimulationTimeInput;
import nucleus.SimulationTime;

public class App {

    public static void main(String[] args) {
        String inputFileName = "./nucleus/src/main/resources/json/simulationTimeInput.json";
        String outputFileName = "./nucleus/src/main/resources/json/output/simulationTimeOutput.json";

        TranslatorController translatorController = TranslatorController.builder()
                .addTranslator(NucleusTranslator.builder()
                        .addInputFile(inputFileName, SimulationTimeInput.getDefaultInstance())
                        .addOutputFile(outputFileName, SimulationTime.class).build())
                .build()
                .init();

        List<Object> objects = translatorController.readInput().getObjects();

        SimulationTime simTime = (SimulationTime) objects.get(0);


        System.out.println(simTime);

        translatorController.writeOutput();
    }

}
