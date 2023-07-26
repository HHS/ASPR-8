package gov.hhs.aspr.ms.gcm.plugins.regions.events;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_PersonRegionUpdateEvent {

	@Test
	@UnitTestConstructor(target = PersonRegionUpdateEvent.class, args = { PersonId.class, RegionId.class, RegionId.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonRegionUpdateEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonRegionUpdateEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonRegionUpdateEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonRegionUpdateEvent.class, name = "personId", args = {})
	public void testPersonId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonRegionUpdateEvent.class, name = "previousRegionId", args = {})
	public void testPreviousRegionId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonRegionUpdateEvent.class, name = "currentRegionId", args = {})
	public void testCurrentRegionId() {
		// nothing to test
	}

}
