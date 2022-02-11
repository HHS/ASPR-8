package plugins.resources.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import plugins.regions.support.RegionId;
import plugins.regions.testsupport.TestRegionId;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.TestResourceId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = RegionResourceAdditionEvent.class)
public final class AT_RegionResourceAdditionEvent implements Event {

	@Test
	@UnitTestConstructor(args = { ResourceId.class, RegionId.class, long.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getResourceId", args = {})
	public void testGetResourceId() {
		ResourceId resourceId = TestResourceId.RESOURCE_3;
		RegionId regionId = TestRegionId.REGION_2;
		long amount = 5234L;
		RegionResourceAdditionEvent regionResourceAdditionEvent = new RegionResourceAdditionEvent(resourceId, regionId, amount);
		assertEquals(resourceId, regionResourceAdditionEvent.getResourceId());
	}

	@Test
	@UnitTestMethod(name = "getRegionId", args = {})
	public void testGetRegionId() {
		ResourceId resourceId = TestResourceId.RESOURCE_3;
		RegionId regionId = TestRegionId.REGION_2;
		long amount = 5234L;
		RegionResourceAdditionEvent regionResourceAdditionEvent = new RegionResourceAdditionEvent(resourceId, regionId, amount);
		assertEquals(regionId, regionResourceAdditionEvent.getRegionId());
	}

	@Test
	@UnitTestMethod(name = "getAmount", args = {})
	public void testGetAmount() {
		ResourceId resourceId = TestResourceId.RESOURCE_3;
		RegionId regionId = TestRegionId.REGION_2;
		long amount = 5234L;
		RegionResourceAdditionEvent regionResourceAdditionEvent = new RegionResourceAdditionEvent(resourceId, regionId, amount);
		assertEquals(amount, regionResourceAdditionEvent.getAmount());
	}
	
	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {
		ResourceId resourceId = TestResourceId.RESOURCE_3;
		RegionId regionId = TestRegionId.REGION_2;
		long amount = 5234L;
		RegionResourceAdditionEvent regionResourceAdditionEvent = new RegionResourceAdditionEvent(resourceId, regionId, amount);
		assertEquals(RegionResourceAdditionEvent.class, regionResourceAdditionEvent.getPrimaryKeyValue());
	}

}
