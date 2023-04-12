package gov.hss.aspr.gcm.translation.protobuf.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.core.TranslatorController;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.input.SimulationStateInput;
import gov.hss.aspr.gcm.translation.protobuf.nucleus.simObjects.ExamplePlanData;
import gov.hss.aspr.gcm.translation.protobuf.nucleus.simObjects.translatorSpecs.ExamplePlanDataTranslatorSpec;
import nucleus.PlanQueueData;
import nucleus.Planner;
import nucleus.SimulationState;
import util.random.RandomGeneratorProvider;

public class AppTest {

    @Test
    public void testSimulationStateTranslator() {
        Path basePath = Path.of("").toAbsolutePath();

        if (!basePath.endsWith("nucleus-translator")) {
            basePath = basePath.resolve("nucleus-translator");
        }

        Path inputFilePath = basePath.resolve("src/main/resources/json");
        Path outputFilePath = basePath.resolve("src/main/resources/json/output");

        outputFilePath.toFile().mkdir();

        String fileName = "simulationState.json";

        TranslatorController translatorController = TranslatorController.builder()
                .addTranslator(NucleusTranslator.builder()
                        .addInputFile(inputFilePath.resolve(fileName).toString(),
                                SimulationStateInput.getDefaultInstance())
                        .addOutputFile(outputFilePath.resolve(fileName).toString(), SimulationState.class).build())
                .addTranslatorSpec(new ExamplePlanDataTranslatorSpec())
                .build();

        List<Object> objects = translatorController.readInput().getObjects();

        SimulationState actualSimulationState = (SimulationState) objects.get(0);

        // assertEquals(exptectedSimulationState.getBaseDate(),
        // actualSimulationState.getBaseDate());
        // assertEquals(exptectedSimulationState.getStartTime(),
        // actualSimulationState.getStartTime());

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6625494580697137579L);

        long arrivalId = randomGenerator.nextLong();
        SimulationState.Builder builder = SimulationState.builder();

        for (int i = 0; i < 10; i++) {
            ExamplePlanData examplePlanData = new ExamplePlanData(i * 15);
            Planner planner = Planner.DATA_MANAGER;
            double time = i + 10.0;
            Object key = "key" + i;
            int plannerId = 0;
            arrivalId += 1;

            PlanQueueData.Builder planQueueBuilder = PlanQueueData.builder();

            planQueueBuilder
                    .setArrivalId(arrivalId)
                    .setKey(key)
                    .setPlanData(examplePlanData)
                    .setPlanner(planner)
                    .setPlannerId(plannerId)
                    .setTime(time);

            builder.addPlanQueueData(planQueueBuilder.build());
        }
        double startTime = 5;
        long planningQueueArrivalId = arrivalId + 1;

        builder.setStartTime(startTime).setPlanningQueueArrivalId(planningQueueArrivalId);

        SimulationState exptectedSimulationState = builder.build();

        assertEquals(exptectedSimulationState.getBaseDate(), actualSimulationState.getBaseDate());
        assertEquals(exptectedSimulationState.getStartTime(), actualSimulationState.getStartTime());
        assertEquals(exptectedSimulationState.getPlanningQueueArrivalId(),
                actualSimulationState.getPlanningQueueArrivalId());

        List<PlanQueueData> expectedPlanQueueDatas = exptectedSimulationState.getPlanQueueDatas();
        List<PlanQueueData> actualPlanQueueDatas = actualSimulationState.getPlanQueueDatas();

        assertEquals(expectedPlanQueueDatas.size(), actualPlanQueueDatas.size());

        for (int i = 0; i < expectedPlanQueueDatas.size(); i++) {
            PlanQueueData expecetdPlanQueueData = expectedPlanQueueDatas.get(i);
            PlanQueueData actualPlanQueueData = actualPlanQueueDatas.get(i);

            assertEquals(expecetdPlanQueueData.getArrivalId(), actualPlanQueueData.getArrivalId());
            assertEquals(expecetdPlanQueueData.getKey(), actualPlanQueueData.getKey());
            assertEquals(expecetdPlanQueueData.getPlanner(), actualPlanQueueData.getPlanner());
            assertEquals(expecetdPlanQueueData.getPlannerId(), actualPlanQueueData.getPlannerId());
            assertEquals(expecetdPlanQueueData.getTime(), actualPlanQueueData.getTime());
            assertEquals(expecetdPlanQueueData.isActive(), actualPlanQueueData.isActive());

            assertEquals(expecetdPlanQueueData.getPlanData(), actualPlanQueueData.getPlanData());
        }

        translatorController.writeOutput();
    }

}
