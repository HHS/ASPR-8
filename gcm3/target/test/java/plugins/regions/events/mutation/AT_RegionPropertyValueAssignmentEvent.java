package plugins.regions.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.regions.testsupport.TestRegionId;
import plugins.regions.testsupport.TestRegionPropertyId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = RegionPropertyValueAssignmentEvent.class)
public class AT_RegionPropertyValueAssignmentEvent {

	@Test
	@UnitTestConstructor(args = { RegionId.class, RegionPropertyId.class, Object.class })
	public void testConstructor() {
		// show that the event can be constructed
		RegionId regionId = TestRegionId.REGION_5;		
		RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1;
		Object regionPropertyValue = 0;

		RegionPropertyValueAssignmentEvent event = new RegionPropertyValueAssignmentEvent(regionId, regionPropertyId, regionPropertyValue);
		assertNotNull(event);

		// there are no precondition tests
	}

	@Test
	@UnitTestMethod(name = "getRegionId", args = {})
	public void testGetRegionId() {
		RegionId expectedRegionId = TestRegionId.REGION_5;
		RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1;
		Object regionPropertyValue = 0;

		RegionPropertyValueAssignmentEvent event = new RegionPropertyValueAssignmentEvent(expectedRegionId, regionPropertyId, regionPropertyValue);
		RegionId actualRegionId = event.getRegionId();

		assertEquals(expectedRegionId, actualRegionId);

		// there are no precondition tests
	}

	@Test
	@UnitTestMethod(name = "getRegionPropertyId", args = {})
	public void testGetRegionPropertyId() {
		RegionId regionId = TestRegionId.REGION_5;
		RegionPropertyId expectedRegionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1;
		Object regionPropertyValue = 0;

		RegionPropertyValueAssignmentEvent event = new RegionPropertyValueAssignmentEvent(regionId, expectedRegionPropertyId, regionPropertyValue);

		RegionPropertyId actualRegionPropertyId = event.getRegionPropertyId();
		assertEquals(expectedRegionPropertyId, actualRegionPropertyId);

		// there are no precondition tests
	}

	@Test
	@UnitTestMethod(name = "getRegionPropertyValue", args = {})
	public void testGetRegionPropertyValue() {
		RegionId regionId = TestRegionId.REGION_5;
		RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1;
		Object expectedRegionPropertyValue = 0;

		RegionPropertyValueAssignmentEvent event = new RegionPropertyValueAssignmentEvent(regionId, regionPropertyId, expectedRegionPropertyValue);
		Object actualRegionPropertyValue = event.getRegionPropertyValue();

		assertEquals(expectedRegionPropertyValue, actualRegionPropertyValue);
		// there are no precondition tests
	}
	
	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue",args = {})
	public void testGetPrimaryKeyValue() {
		RegionId regionId = TestRegionId.REGION_5;
		RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1;
		Object regionPropertyValue = 0;

		RegionPropertyValueAssignmentEvent event = new RegionPropertyValueAssignmentEvent(regionId, regionPropertyId, regionPropertyValue);
		assertEquals(RegionPropertyValueAssignmentEvent.class, event.getPrimaryKeyValue());
		// there are no precondition tests
	}

}
