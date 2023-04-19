package gov.hss.aspr.gcm.translation.protobuf.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.nucleus.input.SimulationStateInput;
import gov.hhs.aspr.translation.core.TranslatorController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorCore;
import gov.hhs.aspr.translation.protobuf.core.testsupport.TestResourceHelper;
import gov.hss.aspr.gcm.translation.protobuf.nucleus.simObjects.ExamplePlanData;
import gov.hss.aspr.gcm.translation.protobuf.nucleus.simObjects.translatorSpecs.ExamplePlanDataTranslatorSpec;
import nucleus.PlanQueueData;
import nucleus.Planner;
import nucleus.SimulationState;
import util.random.RandomGeneratorProvider;

public class AppTest {
    Path basePath = TestResourceHelper.getResourceDir(this.getClass());
    Path filePath = TestResourceHelper.makeTestOutputDir(basePath);

    @Test
    public void testSimulationStateTranslator() {
        String fileName = "simulationState.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);

        TranslatorController translatorController = TranslatorController.builder()
                .setTranslatorCoreBuilder(ProtobufTranslatorCore.builder()
                        .addTranslatorSpec(new ExamplePlanDataTranslatorSpec()))
                .addTranslator(NucleusTranslator.getTranslator())
                .addInputFilePath(filePath.resolve(fileName), SimulationStateInput.class)
                .addOutputFilePath(filePath.resolve(fileName), SimulationState.class)
                .build();

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

        builder.setStartTime(startTime).setPlanningQueueArrivalId(planningQueueArrivalId)
                .setBaseDate(LocalDate.of(2023, 4, 12));

        SimulationState exptectedSimulationState = builder.build();

        translatorController.writeOutput(exptectedSimulationState);

        translatorController.readInput();

        SimulationState actualSimulationState = translatorController.getObject(SimulationState.class);

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

        
        // TODO: fix equals contract for Simulation State
    }

}
