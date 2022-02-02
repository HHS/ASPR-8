package plugins.compartments.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import plugins.compartments.support.CompartmentId;
import plugins.compartments.support.CompartmentPropertyId;
import plugins.compartments.testsupport.TestCompartmentId;
import plugins.compartments.testsupport.TestCompartmentPropertyId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = CompartmentPropertyValueAssignmentEvent.class)
public class AT_CompartmentPropertyValueAssignmentEvent {

	@Test
	@UnitTestConstructor(args = { CompartmentId.class, CompartmentPropertyId.class, Object.class })
	public void testConstructor() {
		// show that the event can be constructed
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_5;
		CompartmentPropertyId compartmentPropertyId = TestCompartmentPropertyId.COMPARTMENT_PROPERTY_5_3;
		Object compartmentPropertyValue = 0;

		CompartmentPropertyValueAssignmentEvent event = new CompartmentPropertyValueAssignmentEvent(compartmentId, compartmentPropertyId, compartmentPropertyValue);
		assertNotNull(event);

		// there are no precondition tests
	}

	@Test
	@UnitTestMethod(name = "getCompartmentId", args = {})
	public void testGetCompartmentId() {
		CompartmentId expectedCompartmentId = TestCompartmentId.COMPARTMENT_5;
		CompartmentPropertyId compartmentPropertyId = TestCompartmentPropertyId.COMPARTMENT_PROPERTY_5_3;
		Object compartmentPropertyValue = 0;

		CompartmentPropertyValueAssignmentEvent event = new CompartmentPropertyValueAssignmentEvent(expectedCompartmentId, compartmentPropertyId, compartmentPropertyValue);
		CompartmentId actualCompartmentId = event.getCompartmentId();

		assertEquals(expectedCompartmentId, actualCompartmentId);

		// there are no precondition tests
	}

	@Test
	@UnitTestMethod(name = "getCompartmentPropertyId", args = {})
	public void testGetCompartmentPropertyId() {
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_5;
		CompartmentPropertyId expectedCompartmentPropertyId = TestCompartmentPropertyId.COMPARTMENT_PROPERTY_5_2;
		Object compartmentPropertyValue = 0;

		CompartmentPropertyValueAssignmentEvent event = new CompartmentPropertyValueAssignmentEvent(compartmentId, expectedCompartmentPropertyId, compartmentPropertyValue);

		CompartmentPropertyId actualCompartmentPropertyId = event.getCompartmentPropertyId();
		assertEquals(expectedCompartmentPropertyId, actualCompartmentPropertyId);

		// there are no precondition tests
	}

	@Test
	@UnitTestMethod(name = "getCompartmentPropertyValue", args = {})
	public void testGetCompartmentPropertyValue() {
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_5;
		CompartmentPropertyId compartmentPropertyId = TestCompartmentPropertyId.COMPARTMENT_PROPERTY_5_1;
		Object expectedCompartmentPropertyValue = 0;

		CompartmentPropertyValueAssignmentEvent event = new CompartmentPropertyValueAssignmentEvent(compartmentId, compartmentPropertyId, expectedCompartmentPropertyValue);
		Object actualCompartmentPropertyValue = event.getCompartmentPropertyValue();

		assertEquals(expectedCompartmentPropertyValue, actualCompartmentPropertyValue);
		// there are no precondition tests
	}
	
	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue",args = {})
	public void testGetPrimaryKeyValue() {
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_5;
		CompartmentPropertyId compartmentPropertyId = TestCompartmentPropertyId.COMPARTMENT_PROPERTY_5_2;
		Object compartmentPropertyValue = 0;

		CompartmentPropertyValueAssignmentEvent event = new CompartmentPropertyValueAssignmentEvent(compartmentId, compartmentPropertyId, compartmentPropertyValue);
		assertEquals(CompartmentPropertyValueAssignmentEvent.class, event.getPrimaryKeyValue());
		// there are no precondition tests
	}

}
