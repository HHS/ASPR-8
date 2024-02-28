package gov.hhs.aspr.ms.gcm.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_Plan {
    private class TestPlan extends Plan {

        TestPlan(double time, boolean active, long arrivalId) {
            super(time, active, arrivalId, Planner.ACTOR);
        }

        TestPlan(double time, boolean active) {
            super(time, active, -1, Planner.ACTOR);
        }

    }

    @Test
    @UnitTestMethod(target = Plan.class, name = "isActive", args = {})
    public void testIsActive() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2100477668025057520L);
        for (int i = 0; i < 10; i++) {
            boolean isActive = randomGenerator.nextBoolean();
            Plan plan = new TestPlan(0.0, isActive);

            assertEquals(isActive, plan.isActive());
        }
    }

    @Test
    @UnitTestMethod(target = Plan.class, name = "getTime", args = {})
    public void testGetTime() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3197772890909709777L);
        for (int i = 0; i < 10; i++) {
            double time = randomGenerator.nextDouble();
            Plan plan = new TestPlan(time, true);

            assertEquals(time, plan.getTime());
        }
    }

    @Test
    @UnitTestMethod(target = Plan.class, name = "cancelPlan", args = {})
    public void testCancelPlan() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(997434850790606431L);
        for (int i = 0; i < 10; i++) {
            boolean shouldCancel = randomGenerator.nextBoolean();
            Plan plan = new TestPlan(0.0, true);
            if (shouldCancel) {
                plan.cancelPlan();
            }

            assertEquals(shouldCancel, plan.canceled);
        }
    }

    @Test
    @UnitTestMethod(target = Plan.class, name = "getArrivalId", args = {})
    public void testGetArrivalId() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6965029307930301694L);
        for (int i = 0; i < 10; i++) {
            boolean shouldSetArrivalId = randomGenerator.nextBoolean();
            long arrivalId = -1;
            Plan plan;
            if (shouldSetArrivalId) {
                arrivalId = randomGenerator.nextLong();
                plan = new TestPlan(0.0, true, arrivalId);
            } else {
                plan = new TestPlan(0.0, true);
            }

            assertEquals(arrivalId, plan.getArrivalId());
        }
    }
}
