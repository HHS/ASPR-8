package gov.hhs.aspr.ms.gcm.plugins.regions.events;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionId;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionPropertyId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_RegionPropertyUpdateEvent {

	@Test
	@UnitTestConstructor(target = RegionPropertyUpdateEvent.class, args = { RegionId.class, RegionPropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = RegionPropertyUpdateEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = RegionPropertyUpdateEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = RegionPropertyUpdateEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = RegionPropertyUpdateEvent.class, name = "regionId", args = {})
	public void testRegionId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = RegionPropertyUpdateEvent.class, name = "regionPropertyId", args = {})
	public void testRegionPropertyId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = RegionPropertyUpdateEvent.class, name = "previousPropertyValue", args = {})
	public void testPreviousPropertyValue() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = RegionPropertyUpdateEvent.class, name = "currentPropertyValue", args = {})
	public void testCurrentPropertyValue() {
		// nothing to test
	}

}
