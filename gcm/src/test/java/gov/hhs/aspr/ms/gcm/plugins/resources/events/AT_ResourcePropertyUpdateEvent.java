package gov.hhs.aspr.ms.gcm.plugins.resources.events;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourceId;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourcePropertyId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_ResourcePropertyUpdateEvent {

	@Test
	@UnitTestConstructor(target = ResourcePropertyUpdateEvent.class, args = { ResourceId.class, ResourcePropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = ResourcePropertyUpdateEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = ResourcePropertyUpdateEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = ResourcePropertyUpdateEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = ResourcePropertyUpdateEvent.class, name = "resourceId", args = {})
	public void testResourceId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = ResourcePropertyUpdateEvent.class, name = "resourcePropertyId", args = {})
	public void testResourcePropertyId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = ResourcePropertyUpdateEvent.class, name = "previousPropertyValue", args = {})
	public void testPreviousPropertyValue() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = ResourcePropertyUpdateEvent.class, name = "currentPropertyValue", args = {})
	public void testCurrentPropertyValue() {
		// nothing to test
	}

}
