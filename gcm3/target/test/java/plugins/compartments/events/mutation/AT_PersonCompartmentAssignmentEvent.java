package plugins.compartments.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import plugins.compartments.support.CompartmentId;
import plugins.compartments.testsupport.TestCompartmentId;
import plugins.people.support.PersonId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = PersonCompartmentAssignmentEvent.class)
public class AT_PersonCompartmentAssignmentEvent {

	@Test
	@UnitTestConstructor(args = { PersonId.class, CompartmentId.class })
	public void testConstructor() {
		PersonId personId = new PersonId(6);
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_3;
		assertNotNull(new PersonCompartmentAssignmentEvent(personId, compartmentId));
	}

	@Test
	@UnitTestMethod(name = "getCompartmentId", args = {})
	public void testGetCompartmentId() {
		PersonId personId = new PersonId(6);
		CompartmentId expectedCompartmentId = TestCompartmentId.COMPARTMENT_3;
		PersonCompartmentAssignmentEvent event = new PersonCompartmentAssignmentEvent(personId, expectedCompartmentId);
		CompartmentId actualCompartmentId = event.getCompartmentId();
		assertEquals(expectedCompartmentId, actualCompartmentId);
	}

	@Test
	@UnitTestMethod(name = "getPersonId", args = {})
	public void testGetPersonId() {
		PersonId expectedPersonId = new PersonId(6);
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_3;
		PersonCompartmentAssignmentEvent event = new PersonCompartmentAssignmentEvent(expectedPersonId, compartmentId);
		PersonId actualPersonId = event.getPersonId();
		assertEquals(expectedPersonId, actualPersonId);
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue",args = {})
	public void testGetPrimaryKeyValue() {
		PersonId personId = new PersonId(6);
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_3;
		PersonCompartmentAssignmentEvent event = new PersonCompartmentAssignmentEvent(personId, compartmentId);
		
		assertEquals(PersonCompartmentAssignmentEvent.class, event.getPrimaryKeyValue());
		// there are no precondition tests
	}

}
