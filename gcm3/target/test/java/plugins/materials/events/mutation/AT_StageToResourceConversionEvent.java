package plugins.materials.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.materials.support.StageId;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.TestResourceId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;


@UnitTest(target = StageToResourceConversionEvent.class)
public final class AT_StageToResourceConversionEvent  {
	
	@Test
	@UnitTestConstructor(args = { StageId.class, ResourceId.class, long.class })
	public void testConstructor() {
		//nothing to test
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = { })
	public void testGetPrimaryKeyValue() {
		StageId stageId = new StageId(345646);
		ResourceId resourceId = TestResourceId.RESOURCE_5;
		long amount = 4745;
		StageToResourceConversionEvent stageToResourceConversionEvent = new StageToResourceConversionEvent(stageId,resourceId,amount);
		assertEquals(StageToResourceConversionEvent.class,stageToResourceConversionEvent.getPrimaryKeyValue());
	}

	@Test
	@UnitTestMethod(name = "getStageId", args = {  })
	public void testGetStageId() {
		StageId stageId = new StageId(345646);
		ResourceId resourceId = TestResourceId.RESOURCE_5;
		long amount = 4745;
		StageToResourceConversionEvent stageToResourceConversionEvent = new StageToResourceConversionEvent(stageId,resourceId,amount);
		assertEquals(stageId,stageToResourceConversionEvent.getStageId());
	}

	@Test
	@UnitTestMethod(name = "getResourceId", args = {  })
	public void testGetResourceId() {
		StageId stageId = new StageId(345646);
		ResourceId resourceId = TestResourceId.RESOURCE_5;
		long amount = 4745;
		StageToResourceConversionEvent stageToResourceConversionEvent = new StageToResourceConversionEvent(stageId,resourceId,amount);
		assertEquals(resourceId,stageToResourceConversionEvent.getResourceId());
	}

	@Test
	@UnitTestMethod(name = "getAmount", args = {  })
	public void testGetAmount() {
		StageId stageId = new StageId(345646);
		ResourceId resourceId = TestResourceId.RESOURCE_5;
		long amount = 4745;
		StageToResourceConversionEvent stageToResourceConversionEvent = new StageToResourceConversionEvent(stageId,resourceId,amount);
		assertEquals(amount,stageToResourceConversionEvent.getAmount());
	}

}
