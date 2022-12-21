package plugins.regions.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.regions.testsupport.TestRegionId;
import plugins.regions.testsupport.TestRegionPropertyId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = RegionPropertyUpdateEvent.class)
public class AT_RegionPropertyUpdateEvent {

	@Test
	@UnitTestConstructor(args = { RegionId.class, RegionPropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		// Nothing to test here. All fields covered by other tests.
	}

	@Test
	@UnitTestMethod(name = "getRegionId", args = {})
	public void testGetRegionId() {
		for (TestRegionId testRegionId : TestRegionId.values()) {
			RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE;
			Object previousValue = true;
			Object currentValue = false;
			RegionPropertyUpdateEvent event = new RegionPropertyUpdateEvent(testRegionId, regionPropertyId, previousValue, currentValue);
			assertEquals(testRegionId, event.regionId());
		}
	}

	@Test
	@UnitTestMethod(name = "getRegionPropertyId", args = {})
	public void testGetRegionPropertyId() {
		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			RegionId regionId = TestRegionId.REGION_2;
			Object previousValue = true;
			Object currentValue = false;
			RegionPropertyUpdateEvent event = new RegionPropertyUpdateEvent(regionId, testRegionPropertyId, previousValue, currentValue);
			assertEquals(testRegionPropertyId, event.regionPropertyId());
		}
	}

	@Test
	@UnitTestMethod(name = "getPreviousPropertyValue", args = {})
	public void testGetPreviousPropertyValue() {
		for (int i = 0; i < 10; i++) {
			RegionId regionId = TestRegionId.REGION_2;
			RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_5_INTEGER_IMMUTABLE;
			Object previousValue = i;
			Object currentValue = false;
			RegionPropertyUpdateEvent event = new RegionPropertyUpdateEvent(regionId, regionPropertyId, previousValue, currentValue);
			assertEquals(previousValue, event.previousPropertyValue());
		}
	}

	@Test
	@UnitTestMethod(name = "getCurrentPropertyValue", args = {})
	public void testGetCurrentPropertyValue() {
		for (int i = 0; i < 10; i++) {
			RegionId regionId = TestRegionId.REGION_2;
			RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_5_INTEGER_IMMUTABLE;
			Object previousValue = true;
			Object currentValue = i;
			RegionPropertyUpdateEvent event = new RegionPropertyUpdateEvent(regionId, regionPropertyId, previousValue, currentValue);
			assertEquals(currentValue, event.currentPropertyValue());
		}
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		RegionId regionId = TestRegionId.REGION_2;
		RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_5_INTEGER_IMMUTABLE;
		Object previousValue = 45;
		Object currentValue = 88;
		RegionPropertyUpdateEvent event = new RegionPropertyUpdateEvent(regionId, regionPropertyId, previousValue, currentValue);
		String actualValue = event.toString();
		String expectedValue =	"RegionPropertyUpdateEvent [regionId=REGION_2, regionPropertyId=REGION_PROPERTY_5_INTEGER_IMMUTABLE, previousPropertyValue=45, currentPropertyValue=88]";
		assertEquals(expectedValue, actualValue);
	}


}
