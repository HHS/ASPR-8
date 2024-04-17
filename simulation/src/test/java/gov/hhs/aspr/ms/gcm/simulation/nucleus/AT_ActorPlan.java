package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;

public class AT_ActorPlan {

    @Test
    @UnitTestConstructor(target = ActorPlan.class, args = { double.class, Consumer.class })
    public void testConstructor() {
        for (int i = 0; i < 10; i++) {
            ActorPlan actorPlan = new ActorPlan(i, (c) -> {
            });

            assertNotNull(actorPlan);
        }
    }

    @Test
    @UnitTestConstructor(target = ActorPlan.class, args = { double.class, boolean.class, Consumer.class })
    public void testConstructor_Active() {
        for (int i = 0; i < 10; i++) {
            ActorPlan actorPlan = new ActorPlan(i, i % 2 == 0, (c) -> {
            });

            assertNotNull(actorPlan);
        }
    }

    @Test
    @UnitTestConstructor(target = ActorPlan.class, args = { double.class, boolean.class, long.class, Consumer.class })
    public void testConstructor_Active_Arrival() {
        for (int i = 0; i < 10; i++) {
            ActorPlan actorPlan = new ActorPlan(i, i % 2 == 0, (long) i, (c) -> {
            });

            assertNotNull(actorPlan);
        }
    }
}
