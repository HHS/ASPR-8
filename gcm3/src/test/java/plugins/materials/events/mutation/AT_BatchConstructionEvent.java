package plugins.materials.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.materials.support.BatchConstructionInfo;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;


@UnitTest(target = BatchConstructionEvent.class)
public final class AT_BatchConstructionEvent {

	@Test
	@UnitTestConstructor(args = { BatchConstructionInfo.class })
	public void testConstructor() {
		//nothing to test
	}

	@Test
	@UnitTestMethod(name = "getBatchConstructionInfo",args = {})	
	public void testGetBatchConstructionInfo() {
		BatchConstructionInfo batchConstructionInfo = BatchConstructionInfo.builder().build();
		assertEquals(batchConstructionInfo, new BatchConstructionEvent(batchConstructionInfo).getBatchConstructionInfo());
	}
	
	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue",args = { BatchConstructionInfo.class })	
	public void testGetPrimaryKeyValue() {
		BatchConstructionInfo batchConstructionInfo = BatchConstructionInfo.builder().build();
		assertEquals(BatchConstructionEvent.class, new BatchConstructionEvent(batchConstructionInfo).getPrimaryKeyValue());
	}

}
