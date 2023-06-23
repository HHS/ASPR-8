package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

public class AT_PlanQueueData {
    class TestPlanData1 implements PlanData {
    }

    @Test
    @UnitTestMethod(target = PlanQueueData.Builder.class, name = "build", args = {})
    public void testBuild() {
        assertNotNull(PlanQueueData.builder().setPlanData(new TestPlanData1()).setPlanner(Planner.ACTOR).build());

        // preconditions:
        // PlanData is null
        ContractException contractException = assertThrows(ContractException.class, () -> {
            PlanQueueData.builder().setPlanner(Planner.ACTOR).build();
        });

        assertEquals(NucleusError.NULL_PLAN_DATA, contractException.getErrorType());

        // Planner is null
        contractException = assertThrows(ContractException.class, () -> {
            PlanQueueData.builder().setPlanData(new TestPlanData1()).build();
        });

        assertEquals(NucleusError.NULL_PLANNER, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = PlanQueueData.Builder.class, name = "setTime", args = { double.class })
    public void testSetTime() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(594478369345316212L);

        for (int i = 0; i < 10; i++) {
            double time = randomGenerator.nextDouble() * 100;
            PlanQueueData planQueueData = PlanQueueData.builder()
                    .setPlanData(new TestPlanData1())
                    .setPlanner(Planner.ACTOR)
                    .setTime(time)
                    .build();

            assertEquals(time, planQueueData.getTime());
        }

        PlanQueueData planQueueData = PlanQueueData.builder()
                .setPlanData(new TestPlanData1())
                .setPlanner(Planner.ACTOR)
                .build();

        assertEquals(0, planQueueData.getTime());
    }

    @Test
    @UnitTestMethod(target = PlanQueueData.Builder.class, name = "setActive", args = { boolean.class })
    public void testSetActive() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2212750783748698797L);

        for (int i = 0; i < 10; i++) {
            boolean active = randomGenerator.nextBoolean();
            PlanQueueData planQueueData = PlanQueueData.builder()
                    .setPlanData(new TestPlanData1())
                    .setPlanner(Planner.ACTOR)
                    .setActive(active)
                    .build();

            assertEquals(active, planQueueData.isActive());
        }

        PlanQueueData planQueueData = PlanQueueData.builder()
                .setPlanData(new TestPlanData1())
                .setPlanner(Planner.ACTOR)
                .build();

        assertTrue(planQueueData.isActive());
    }

    @Test
    @UnitTestMethod(target = PlanQueueData.Builder.class, name = "setKey", args = { Object.class })
    public void testSetKey() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6278569817385464648L);

        for (int i = 0; i < 10; i++) {
            String key = "TestKey" + (i * randomGenerator.nextInt(100) + 1) + i % 2;
            PlanQueueData planQueueData = PlanQueueData.builder()
                    .setPlanData(new TestPlanData1())
                    .setPlanner(Planner.ACTOR)
                    .setKey(key)
                    .build();

            assertEquals(key, planQueueData.getKey());
        }

        PlanQueueData planQueueData = PlanQueueData.builder()
                .setPlanData(new TestPlanData1())
                .setPlanner(Planner.ACTOR)
                .build();

        assertTrue(planQueueData.getKey() == null);
    }

    @Test
    @UnitTestMethod(target = PlanQueueData.Builder.class, name = "setPlanData", args = { PlanData.class })
    public void testSetPlanData() {
        for (int i = 0; i < 10; i++) {
            TestPlanData1 testPlanData1 = new TestPlanData1();
            PlanQueueData planQueueData = PlanQueueData.builder()
                    .setPlanData(testPlanData1)
                    .setPlanner(Planner.ACTOR)
                    .build();

            assertEquals(testPlanData1, planQueueData.getPlanData());
        }
    }

    @Test
    @UnitTestMethod(target = PlanQueueData.Builder.class, name = "setPlanner", args = { Planner.class })
    public void testSetPlanner() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5508769779925956678L);

        for (int i = 0; i < 10; i++) {
            Planner planner = Planner.values()[randomGenerator.nextInt(3)];

            PlanQueueData planQueueData = PlanQueueData.builder()
                    .setPlanData(new TestPlanData1())
                    .setPlanner(planner)
                    .build();

            assertEquals(planner, planQueueData.getPlanner());
        }
    }

    @Test
    @UnitTestMethod(target = PlanQueueData.Builder.class, name = "setPlannerId", args = { int.class })
    public void testSetPlannerId() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(594478369345316212L);

        for (int i = 0; i < 10; i++) {
            int plannerId = randomGenerator.nextInt(100);
            PlanQueueData planQueueData = PlanQueueData.builder()
                    .setPlanData(new TestPlanData1())
                    .setPlanner(Planner.ACTOR)
                    .setPlannerId(plannerId)
                    .build();

            assertEquals(plannerId, planQueueData.getPlannerId());
        }

        PlanQueueData planQueueData = PlanQueueData.builder()
                .setPlanData(new TestPlanData1())
                .setPlanner(Planner.ACTOR)
                .build();

        assertEquals(0, planQueueData.getPlannerId());
    }

    @Test
    @UnitTestMethod(target = PlanQueueData.Builder.class, name = "setArrivalId", args = { long.class })
    public void testSetArrivalId() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5508769779925956678L);

        for (int i = 0; i < 10; i++) {
            long arrivalId = randomGenerator.nextLong();
            PlanQueueData planQueueData = PlanQueueData.builder()
                    .setPlanData(new TestPlanData1())
                    .setPlanner(Planner.ACTOR)
                    .setArrivalId(arrivalId)
                    .build();

            assertEquals(arrivalId, planQueueData.getArrivalId());
        }

        PlanQueueData planQueueData = PlanQueueData.builder()
                .setPlanData(new TestPlanData1())
                .setPlanner(Planner.ACTOR)
                .build();

        assertEquals(0, planQueueData.getArrivalId());
    }

    @Test
    @UnitTestMethod(target = PlanQueueData.class, name = "builder", args = {})
    public void testBuilder() {
        assertNotNull(PlanQueueData.builder());
    }

    @Test
    @UnitTestMethod(target = PlanQueueData.class, name = "getArrivalId", args = {})
    public void testGetArrivalId() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5508769779925956678L);

        for (int i = 0; i < 10; i++) {
            long arrivalId = randomGenerator.nextLong();
            PlanQueueData planQueueData = PlanQueueData.builder()
                    .setPlanData(new TestPlanData1())
                    .setPlanner(Planner.ACTOR)
                    .setArrivalId(arrivalId)
                    .build();

            assertEquals(arrivalId, planQueueData.getArrivalId());
        }

        PlanQueueData planQueueData = PlanQueueData.builder()
                .setPlanData(new TestPlanData1())
                .setPlanner(Planner.ACTOR)
                .build();

        assertEquals(0, planQueueData.getArrivalId());
    }

    @Test
    @UnitTestMethod(target = PlanQueueData.class, name = "getKey", args = {})
    public void testGetKey() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6278569817385464648L);

        for (int i = 0; i < 10; i++) {
            String key = "TestKey" + (i * randomGenerator.nextInt(100) + 1) + i % 2;
            PlanQueueData planQueueData = PlanQueueData.builder()
                    .setPlanData(new TestPlanData1())
                    .setPlanner(Planner.ACTOR)
                    .setKey(key)
                    .build();

            assertEquals(key, planQueueData.getKey());
        }

        PlanQueueData planQueueData = PlanQueueData.builder()
                .setPlanData(new TestPlanData1())
                .setPlanner(Planner.ACTOR)
                .build();

        assertTrue(planQueueData.getKey() == null);
    }

    @Test
    @UnitTestMethod(target = PlanQueueData.class, name = "getPlanData", args = {})
    public void testGetPlanData() {
        for (int i = 0; i < 10; i++) {
            TestPlanData1 testPlanData1 = new TestPlanData1();
            PlanQueueData planQueueData = PlanQueueData.builder()
                    .setPlanData(testPlanData1)
                    .setPlanner(Planner.ACTOR)
                    .build();

            assertEquals(testPlanData1, planQueueData.getPlanData());
        }
    }

    @Test
    @UnitTestMethod(target = PlanQueueData.class, name = "getPlanner", args = {})
    public void testGetPlanner() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5508769779925956678L);

        for (int i = 0; i < 10; i++) {
            Planner planner = Planner.values()[randomGenerator.nextInt(3)];

            PlanQueueData planQueueData = PlanQueueData.builder()
                    .setPlanData(new TestPlanData1())
                    .setPlanner(planner)
                    .build();

            assertEquals(planner, planQueueData.getPlanner());
        }
    }

    @Test
    @UnitTestMethod(target = PlanQueueData.class, name = "getPlannerId", args = {})
    public void testGetPlannerId() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(594478369345316212L);

        for (int i = 0; i < 10; i++) {
            int plannerId = randomGenerator.nextInt(100);
            PlanQueueData planQueueData = PlanQueueData.builder()
                    .setPlanData(new TestPlanData1())
                    .setPlanner(Planner.ACTOR)
                    .setPlannerId(plannerId)
                    .build();

            assertEquals(plannerId, planQueueData.getPlannerId());
        }

        PlanQueueData planQueueData = PlanQueueData.builder()
                .setPlanData(new TestPlanData1())
                .setPlanner(Planner.ACTOR)
                .build();

        assertEquals(0, planQueueData.getPlannerId());
    }

    @Test
    @UnitTestMethod(target = PlanQueueData.class, name = "getTime", args = {})
    public void testGetTime() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(594478369345316212L);

        for (int i = 0; i < 10; i++) {
            double time = randomGenerator.nextDouble() * 100;
            PlanQueueData planQueueData = PlanQueueData.builder()
                    .setPlanData(new TestPlanData1())
                    .setPlanner(Planner.ACTOR)
                    .setTime(time)
                    .build();

            assertEquals(time, planQueueData.getTime());
        }

        PlanQueueData planQueueData = PlanQueueData.builder()
                .setPlanData(new TestPlanData1())
                .setPlanner(Planner.ACTOR)
                .build();

        assertEquals(0, planQueueData.getTime());
    }

    @Test
    @UnitTestMethod(target = PlanQueueData.class, name = "isActive", args = {})
    public void testIsActive() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2212750783748698797L);

        for (int i = 0; i < 10; i++) {
            boolean active = randomGenerator.nextBoolean();
            PlanQueueData planQueueData = PlanQueueData.builder()
                    .setPlanData(new TestPlanData1())
                    .setPlanner(Planner.ACTOR)
                    .setActive(active)
                    .build();

            assertEquals(active, planQueueData.isActive());
        }

        PlanQueueData planQueueData = PlanQueueData.builder()
                .setPlanData(new TestPlanData1())
                .setPlanner(Planner.ACTOR)
                .build();

        assertTrue(planQueueData.isActive());
    }

    @Test
    public void testHashCode() {

    }

    @Test
    public void testEquals() {

    }
}
