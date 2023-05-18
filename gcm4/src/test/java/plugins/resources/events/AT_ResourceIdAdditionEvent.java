package plugins.resources.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.util.properties.TimeTrackingPolicy;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_ResourceIdAdditionEvent {

	@Test
	@UnitTestConstructor(target = ResourceIdAdditionEvent.class, args = { ResourceId.class, TimeTrackingPolicy.class })
	public void testConstructor() {
		
		// test case: null resource id
		ContractException resourceIdContractException = assertThrows(ContractException.class, () -> new ResourceIdAdditionEvent(null, true));
		assertEquals(resourceIdContractException.getErrorType(), ResourceError.NULL_RESOURCE_ID);
	}

	@Test
	@UnitTestMethod(target = ResourceIdAdditionEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = ResourceIdAdditionEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = ResourceIdAdditionEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = ResourceIdAdditionEvent.class, name = "resourceId", args = {})
	public void testResourceId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = ResourceIdAdditionEvent.class, name = "timeTrackingPolicy", args = {})
	public void testTimeTrackingPolicy() {
		// nothing to test
	}

}