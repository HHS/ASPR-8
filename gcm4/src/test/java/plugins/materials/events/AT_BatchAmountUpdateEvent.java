package plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.materials.support.BatchId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;


@UnitTest(target = BatchAmountUpdateEvent.class)
public class AT_BatchAmountUpdateEvent {
	
	@Test
	@UnitTestConstructor(args = {BatchId.class, double.class, double.class})
	public void testConstructor() {
		//nothing to test
	}
	
	@Test
	@UnitTestMethod(name="toString",args = {})
	public void testToString() {
		BatchId batchId = new BatchId(23423);
		double previousAmount = 14.5;
		double currentAmount = 44.7;		
		BatchAmountUpdateEvent batchAmountUpdateEvent = new BatchAmountUpdateEvent(batchId, previousAmount, currentAmount);
		assertEquals("BatchAmountUpdateEvent [batchId=23423, previousAmount=14.5, currentAmount=44.7]", batchAmountUpdateEvent.toString());
	}

}
