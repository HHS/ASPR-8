package plugins.globalproperties.events;

import org.junit.jupiter.api.Test;

import plugins.globalproperties.support.GlobalPropertyId;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

public class AT_GlobalPropertyUpdateEvent {

	@Test
	@UnitTestConstructor(target = GlobalPropertyUpdateEvent.class, args = { GlobalPropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyUpdateEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyUpdateEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyUpdateEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}
	
	@Test
	@UnitTestMethod(target = GlobalPropertyUpdateEvent.class, name = "globalPropertyId", args = {})
	public void testGlobalPropertyId() {
		// nothing to test
	}
	
	@Test
	@UnitTestMethod(target = GlobalPropertyUpdateEvent.class, name = "previousPropertyValue", args = {})
	public void testPreviousPropertyValue() {
		// nothing to test
	}
	
	@Test
	@UnitTestMethod(target = GlobalPropertyUpdateEvent.class, name = "currentPropertyValue", args = {})
	public void testCurrentPropertyValue() {
		// nothing to test
	}
	
	
	
}
