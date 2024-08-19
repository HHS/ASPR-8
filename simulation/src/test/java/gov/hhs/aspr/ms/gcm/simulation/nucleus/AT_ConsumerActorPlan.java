package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.errors.ContractException;

public class AT_ConsumerActorPlan {

    @Test
    @UnitTestConstructor(target = ConsumerActorPlan.class, args = { double.class, Consumer.class })
    public void testActorPlan() {
    	//precondition test: if the consumer is null
    	ContractException contractException = assertThrows(ContractException.class, ()->{
    		new ConsumerActorPlan(0, null);
    	});
    	assertEquals(NucleusError.NULL_PLAN_CONSUMER, contractException.getErrorType());
    }

    @Test
    @UnitTestConstructor(target = ConsumerActorPlan.class, args = { double.class, boolean.class, Consumer.class })
    public void testConstructor_Active() {
    	//precondition test: if the consumer is null
    	ContractException contractException = assertThrows(ContractException.class, ()->{
    		new ConsumerActorPlan(0,true, null);
    	});
    	assertEquals(NucleusError.NULL_PLAN_CONSUMER, contractException.getErrorType());
    }

    @Test
    @UnitTestConstructor(target = ConsumerActorPlan.class, args = { double.class, boolean.class, long.class, Consumer.class })
    public void testConstructor_Active_Arrival() {
    	//precondition test: if the consumer is null
    	ContractException contractException = assertThrows(ContractException.class, ()->{
    		new ConsumerActorPlan(0,true,2345345L, null);
    	});
    	assertEquals(NucleusError.NULL_PLAN_CONSUMER, contractException.getErrorType());
    }
}
