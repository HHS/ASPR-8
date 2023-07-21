package plugins.partitions.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import plugins.people.support.PersonId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public final class AT_LabelerSensitivity {

	@Test
	@UnitTestConstructor(target = LabelerSensitivity.class, args = { Class.class, Function.class })
	public void testConstructor() {
		// nothing to test
	}

	private static class Event2 implements Event {
	}

	private static class Event3 implements Event {
	}

	@Test
	@UnitTestMethod(target = LabelerSensitivity.class, name = "getEventClass", args = {})
	public void testGetEventClass() {
		LabelerSensitivity<Event> labelerSensitivity1 = new LabelerSensitivity<>(Event.class, (e) -> Optional.ofNullable(null));
		assertEquals(Event.class, labelerSensitivity1.getEventClass());

		/*
		 * Note that we are using two event types here just to show that it
		 * works. These events do not carry person information and normally a
		 * LabelerSensitivity is only used with such events.
		 */

		LabelerSensitivity<Event2> labelerSensitivity2 = new LabelerSensitivity<>(Event2.class, (e) -> Optional.ofNullable(null));
		assertEquals(Event2.class, labelerSensitivity2.getEventClass());

		LabelerSensitivity<Event3> labelerSensitivity3 = new LabelerSensitivity<>(Event3.class, (e) -> Optional.ofNullable(null));
		assertEquals(Event3.class, labelerSensitivity3.getEventClass());

	}

	@Test
	@UnitTestMethod(target = LabelerSensitivity.class, name = "getPersonId", args = { Event.class })
	public void testGetPersonId() {

		LabelerSensitivity<Event> labelerSensitivity = new LabelerSensitivity<>(Event.class, (e) -> Optional.empty());
		Optional<PersonId> optional = labelerSensitivity.getPersonId(new Event() {
		});
		assertFalse(optional.isPresent());

		PersonId personId = new PersonId(0);
		labelerSensitivity = new LabelerSensitivity<>(Event.class, (e) -> Optional.of(personId));
		optional = labelerSensitivity.getPersonId(new Event() {
		});
		assertTrue(optional.isPresent());
		assertEquals(personId, optional.get());
	}

}