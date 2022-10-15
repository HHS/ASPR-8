package plugins.resources.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import plugins.regions.support.RegionId;
import plugins.regions.testsupport.TestRegionId;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.TestResourceId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = RegionResourceUpdateEvent.class)
public class AT_RegionResourceUpdateEvent implements Event {

	@Test
	@UnitTestConstructor(args = { RegionId.class, ResourceId.class, long.class, long.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getResourceId", args = {})
	public void testGetResourceId() {
		RegionId regionId = TestRegionId.REGION_4;
		ResourceId resourceId = TestResourceId.RESOURCE_2;
		long previousResourceLevel = 45L;
		long currentResourceLevel = 398L;
		RegionResourceUpdateEvent regionResourceUpdateEvent = new RegionResourceUpdateEvent(regionId, resourceId, previousResourceLevel, currentResourceLevel);
		assertEquals(resourceId, regionResourceUpdateEvent.getResourceId());
	}

	@Test
	@UnitTestMethod(name = "getRegionId", args = {})
	public void testGetRegionId() {
		RegionId regionId = TestRegionId.REGION_4;
		ResourceId resourceId = TestResourceId.RESOURCE_2;
		long previousResourceLevel = 45L;
		long currentResourceLevel = 398L;
		RegionResourceUpdateEvent regionResourceUpdateEvent = new RegionResourceUpdateEvent(regionId, resourceId, previousResourceLevel, currentResourceLevel);
		assertEquals(regionId, regionResourceUpdateEvent.getRegionId());
	}

	@Test
	@UnitTestMethod(name = "getPreviousResourceLevel", args = {})
	public void testGetPreviousResourceLevel() {
		RegionId regionId = TestRegionId.REGION_4;
		ResourceId resourceId = TestResourceId.RESOURCE_2;
		long previousResourceLevel = 45L;
		long currentResourceLevel = 398L;
		RegionResourceUpdateEvent regionResourceUpdateEvent = new RegionResourceUpdateEvent(regionId, resourceId, previousResourceLevel, currentResourceLevel);
		assertEquals(previousResourceLevel, regionResourceUpdateEvent.getPreviousResourceLevel());
	}

	@Test
	@UnitTestMethod(name = "getCurrentResourceLevel", args = {})
	public void testGetCurrentResourceLevel() {
		RegionId regionId = TestRegionId.REGION_4;
		ResourceId resourceId = TestResourceId.RESOURCE_2;
		long previousResourceLevel = 45L;
		long currentResourceLevel = 398L;
		RegionResourceUpdateEvent regionResourceUpdateEvent = new RegionResourceUpdateEvent(regionId, resourceId, previousResourceLevel, currentResourceLevel);
		assertEquals(currentResourceLevel, regionResourceUpdateEvent.getCurrentResourceLevel());
	}

}
