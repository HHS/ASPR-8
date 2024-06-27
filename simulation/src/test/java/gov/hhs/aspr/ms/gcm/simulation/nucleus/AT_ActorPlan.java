package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;

public class AT_ActorPlan {

    @Test
    @UnitTestConstructor(target = ActorPlan.class, args = { double.class })
    public void testActorPlan() {
    	//nothing to test
    }

    @Test
    @UnitTestConstructor(target = ActorPlan.class, args = { double.class, boolean.class})
    public void testConstructor_Active() {
        //nothing to test
    }

    @Test
    @UnitTestConstructor(target = ActorPlan.class, args = { double.class, boolean.class, long.class})
    public void testConstructor_Active_Arrival() {
    	//nothing to test
    }
}
