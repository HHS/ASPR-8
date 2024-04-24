package gov.hhs.aspr.ms.gcm.simulation.plugins.resources.events;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.support.ResourceId;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_PersonResourceUpdateEvent {

	@Test
	@UnitTestConstructor(target = PersonResourceUpdateEvent.class, args = { PersonId.class, ResourceId.class, long.class, long.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonResourceUpdateEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonResourceUpdateEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonResourceUpdateEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonResourceUpdateEvent.class, name = "personId", args = {})
	public void testPersonId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonResourceUpdateEvent.class, name = "resourceId", args = {})
	public void testResourceId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonResourceUpdateEvent.class, name = "previousResourceLevel", args = {})
	public void testPreviousResourceLevel() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonResourceUpdateEvent.class, name = "currentResourceLevel", args = {})
	public void testCurrentResourceLevel() {
		// nothing to test
	}

}
