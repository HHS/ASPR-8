package plugins.people.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import plugins.people.support.PersonContructionData;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = PersonCreationEvent.class)
public final class AT_PersonCreationEvent implements Event {

	@Test
	@UnitTestConstructor(args = { PersonContructionData.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getPersonContructionData", args = {})
	public void testGetPersonContructionData() {
		PersonContructionData personContructionData = PersonContructionData.builder().build();
		PersonCreationEvent personCreationEvent = new PersonCreationEvent(personContructionData);
		
		assertEquals(personContructionData, personCreationEvent.getPersonContructionData());
	}

}
