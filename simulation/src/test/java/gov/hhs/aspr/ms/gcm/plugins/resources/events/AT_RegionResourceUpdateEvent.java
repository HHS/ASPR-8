package gov.hhs.aspr.ms.gcm.plugins.resources.events;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionId;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourceId;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_RegionResourceUpdateEvent {

	@Test
	@UnitTestConstructor(target = RegionResourceUpdateEvent.class, args = { RegionId.class, ResourceId.class, long.class, long.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = RegionResourceUpdateEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = RegionResourceUpdateEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = RegionResourceUpdateEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = RegionResourceUpdateEvent.class, name = "regionId", args = {})
	public void testRegionId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = RegionResourceUpdateEvent.class, name = "resourceId", args = {})
	public void testResourceId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = RegionResourceUpdateEvent.class, name = "previousResourceLevel", args = {})
	public void testPreviousResourceLevel() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = RegionResourceUpdateEvent.class, name = "currentResourceLevel", args = {})
	public void testCurrentResourceLevel() {
		// nothing to test
	}

}
