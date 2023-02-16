package plugins.resources.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.TestResourceId;
import plugins.util.properties.PropertyError;
import plugins.util.properties.TimeTrackingPolicy;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_ResourceIdAdditionEvent {

	@Test
	@UnitTestConstructor(target = ResourceIdAdditionEvent.class, args = { ResourceId.class, TimeTrackingPolicy.class })
	public void testConstructor() {
		TimeTrackingPolicy timeTrackingPolicy = TimeTrackingPolicy.TRACK_TIME;
		TestResourceId testResourceId = TestResourceId.RESOURCE_1;

		// test case: null resource id
		ContractException resourceIdContractException = assertThrows(ContractException.class, () -> new ResourceIdAdditionEvent(null, timeTrackingPolicy));
		assertEquals(resourceIdContractException.getErrorType(), ResourceError.NULL_RESOURCE_ID);

		// test case: null time tracking policy
		ContractException timeContractException = assertThrows(ContractException.class, () -> new ResourceIdAdditionEvent(testResourceId, null));
		assertEquals(timeContractException.getErrorType(), PropertyError.NULL_TIME_TRACKING_POLICY);
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