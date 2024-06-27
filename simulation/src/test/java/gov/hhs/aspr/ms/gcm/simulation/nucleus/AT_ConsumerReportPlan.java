package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.errors.ContractException;

public class AT_ConsumerReportPlan {

    @Test
    @UnitTestConstructor(target = ConsumerReportPlan.class, args = { double.class, Consumer.class })
    public void testConstructor() {
    	//precondition test: if the consumer is null
    	ContractException contractException = assertThrows(ContractException.class, () -> {
			new ConsumerReportPlan(0, null);
		});
		assertEquals(NucleusError.NULL_PLAN_CONSUMER, contractException.getErrorType());
    }

    @Test
    @UnitTestConstructor(target = ConsumerReportPlan.class, args = { double.class, long.class, Consumer.class })
    public void testConstructor_Arrival() {
    	//precondition test: if the consumer is null
    	ContractException contractException = assertThrows(ContractException.class, () -> {
			new ConsumerReportPlan(0,3453L, null);
		});
		assertEquals(NucleusError.NULL_PLAN_CONSUMER, contractException.getErrorType());
    }
}
