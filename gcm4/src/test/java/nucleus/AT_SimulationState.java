package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import util.annotations.UnitTag;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

public class AT_SimulationState {

    class TestPlanData1 implements PlanData {

    }

    @Test
    @UnitTestMethod(target = SimulationState.Builder.class, name = "build", args = {})
    public void testBuild() {
        assertNotNull(SimulationState.builder().build());

        // preconditions
        ContractException contractException = assertThrows(ContractException.class, () -> {

            SimulationState.builder()
                    .addPlanQueueData(
                            PlanQueueData.builder().setPlanner(Planner.ACTOR).setPlanData(new TestPlanData1()).build())
                    .build();
        });

        assertEquals(NucleusError.PLANNING_QUEUE_ARRIVAL_INVALID, contractException.getErrorType());

        contractException = assertThrows(ContractException.class, () -> {

            SimulationState.builder()
                    .addPlanQueueData(
                            PlanQueueData.builder().setPlanner(Planner.ACTOR).setPlanData(new TestPlanData1())
                                    .setTime(1).build())
                    .setPlanningQueueArrivalId(1)
                    .setStartTime(5)
                    .build();
        });

        assertEquals(NucleusError.PLANNING_QUEUE_TIME, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = SimulationState.Builder.class, name = "setPlanningQueueArrivalId", args = { long.class })
    public void testSetPlanningQueueArrivalId() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(594478369345316212L);

        for (int i = 0; i < 10; i++) {
            long arrivalId = randomGenerator.nextLong();
            SimulationState simulationState = SimulationState.builder()
                    .setPlanningQueueArrivalId(arrivalId)
                    .build();

            assertEquals(arrivalId, simulationState.getPlanningQueueArrivalId());
        }
    }

    @Test
    @UnitTestMethod(target = SimulationState.Builder.class, name = "setStartTime", args = { double.class })
    public void testSetStartTime() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6278569817385464648L);

        for (int i = 0; i < 10; i++) {
            double startTime = randomGenerator.nextDouble() * 10;
            SimulationState simulationState = SimulationState.builder()
                    .setStartTime(startTime)
                    .build();

            assertEquals(startTime, simulationState.getStartTime());
        }
    }

    @Test
    @UnitTestMethod(target = SimulationState.Builder.class, name = "setBaseDate", args = { LocalDate.class })
    public void testSetBaseDate() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(196036555746621355L);

        for (int i = 0; i < 10; i++) {
            int month = randomGenerator.nextInt(12) + 1;
            int day;
            int year = 2023 + randomGenerator.nextInt(2);

            if (month == 2) {
                day = randomGenerator.nextInt(28) + 1;
            } else if (month == 9 || month == 4 || month == 6 || month == 11) {
                day = randomGenerator.nextInt(30) + 1;
            } else {
                day = randomGenerator.nextInt(31) + 1;
            }

            LocalDate localDate = LocalDate.of(year, month, day);
            SimulationState simulationState = SimulationState.builder()
                    .setBaseDate(localDate)
                    .build();

            assertEquals(localDate, simulationState.getBaseDate());
        }

        // precondition:
        // LocalDate is null
        ContractException contractException = assertThrows(ContractException.class, () -> {
            SimulationState.builder().setBaseDate(null);
        });

        assertEquals(NucleusError.NULL_BASE_DATE, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = SimulationState.Builder.class, name = "addPlanQueueData", args = { PlanQueueData.class })
    public void testAddPlanQueueData() {
        for (int i = 0; i < 10; i++) {

            SimulationState.Builder simulationStateBuilder = SimulationState.builder();
            List<PlanQueueData> expectedPlanQueueDatas = new ArrayList<>();
            for (int j = 0; j < i; j++) {
                PlanQueueData planQueueData = PlanQueueData.builder()
                        .setPlanData(new TestPlanData1())
                        .setTime(i)
                        .setPlanner(Planner.ACTOR)
                        .build();

                simulationStateBuilder.addPlanQueueData(planQueueData);
                expectedPlanQueueDatas.add(planQueueData);
            }

            SimulationState simulationState = simulationStateBuilder.setPlanningQueueArrivalId(1).build();

            List<PlanQueueData> actualPlanQueueDatas = simulationState.getPlanQueueDatas();
            assertEquals(expectedPlanQueueDatas, actualPlanQueueDatas);
        }

        // precondition:
        // LocalDate is null
        ContractException contractException = assertThrows(ContractException.class, () -> {
            SimulationState.builder().addPlanQueueData(null);
        });

        assertEquals(NucleusError.NULL_PLAN_QUEUE_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = SimulationState.class, name = "builder", args = {})
    public void testBuilder() {
        assertNotNull(SimulationState.builder());
    }

    @Test
    @UnitTestMethod(target = SimulationState.class, name = "getPlanningQueueArrivalId", args = {})
    public void testGetPlanningQueueArrivalId() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2212750783748698797L);

        for (int i = 0; i < 10; i++) {
            long arrivalId = randomGenerator.nextLong();
            SimulationState simulationState = SimulationState.builder()
                    .setPlanningQueueArrivalId(arrivalId)
                    .build();

            assertEquals(arrivalId, simulationState.getPlanningQueueArrivalId());
        }
    }

    @Test
    @UnitTestMethod(target = SimulationState.class, name = "getStartTime", args = {})
    public void testGetStartTime() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5508769779925956678L);

        for (int i = 0; i < 10; i++) {
            double startTime = randomGenerator.nextDouble() * 10;
            SimulationState simulationState = SimulationState.builder()
                    .setStartTime(startTime)
                    .build();

            assertEquals(startTime, simulationState.getStartTime());
        }
    }

    @Test
    @UnitTestMethod(target = SimulationState.class, name = "getPlanQueueDatas", args = {})
    public void testGetPlanQueueDatas() {
        for (int i = 0; i < 10; i++) {

            SimulationState.Builder simulationStateBuilder = SimulationState.builder();
            List<PlanQueueData> expectedPlanQueueDatas = new ArrayList<>();
            for (int j = 0; j < i; j++) {
                PlanQueueData planQueueData = PlanQueueData.builder()
                        .setPlanData(new TestPlanData1())
                        .setTime(i)
                        .setPlanner(Planner.ACTOR)
                        .build();

                simulationStateBuilder.addPlanQueueData(planQueueData);
                expectedPlanQueueDatas.add(planQueueData);
            }

            SimulationState simulationState = simulationStateBuilder.setPlanningQueueArrivalId(1).build();

            List<PlanQueueData> actualPlanQueueDatas = simulationState.getPlanQueueDatas();
            assertEquals(expectedPlanQueueDatas, actualPlanQueueDatas);
        }
    }

    @Test
    @UnitTestMethod(target = SimulationState.class, name = "getBaseDate", args = {})
    public void testGetBaseDate() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1840591792412124210L);

        for (int i = 0; i < 10; i++) {
            int month = randomGenerator.nextInt(12) + 1;
            int day;
            int year = 2023 + randomGenerator.nextInt(2);

            if (month == 2) {
                day = randomGenerator.nextInt(28) + 1;
            } else if (month == 9 || month == 4 || month == 6 || month == 11) {
                day = randomGenerator.nextInt(30) + 1;
            } else {
                day = randomGenerator.nextInt(31) + 1;
            }

            LocalDate localDate = LocalDate.of(year, month, day);
            SimulationState simulationState = SimulationState.builder()
                    .setBaseDate(localDate)
                    .build();

            assertEquals(localDate, simulationState.getBaseDate());
        }
    }

    @Test
    @UnitTestMethod(target = SimulationState.class, name = "equals", args = { Object.class }, tags = {
            UnitTag.INCOMPLETE })
    public void testEquals() {

    }

    @Test
    @UnitTestMethod(target = SimulationState.class, name = "hashCode", args = {}, tags = { UnitTag.INCOMPLETE })
    public void testHashCode() {

    }
}
