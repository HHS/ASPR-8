package gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.time.LocalDate;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.input.SimulationStateInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport.ExamplePlanData;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.core.testsupport.TestResourceHelper;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import nucleus.PlanQueueData;
import nucleus.Planner;
import nucleus.SimulationState;
import util.annotations.UnitTestForCoverage;
import util.random.RandomGeneratorProvider;

public class IT_NucleusTranslator {
    Path basePath = TestResourceHelper.getResourceDir(this.getClass());
    Path filePath = TestResourceHelper.makeTestOutputDir(basePath);

    @Test
    @UnitTestForCoverage
    public void testSimulationStateTranslator() {
        String fileName = "simulationState.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);

        TranslationController translatorController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
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

        SimulationState actualSimulationState = translatorController.getFirstObject(SimulationState.class);

        assertEquals(exptectedSimulationState, actualSimulationState);
    }

}
