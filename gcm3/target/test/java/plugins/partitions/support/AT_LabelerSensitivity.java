package plugins.partitions.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import plugins.partitions.events.PartitionAdditionEvent;
import plugins.partitions.events.PartitionRemovalEvent;
import plugins.people.support.PersonId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = LabelerSensitivity.class)
public final class AT_LabelerSensitivity {
	 

	@Test
	@UnitTestConstructor(args = { Class.class, Function.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getEventClass", args = {})
	public void testGetEventClass() {
		LabelerSensitivity<Event> labelerSensitivity1 = new LabelerSensitivity<>(Event.class, (e) -> Optional.ofNullable(null));
		assertEquals(Event.class, labelerSensitivity1.getEventClass());

		/*
		 * Note that we are using two event types here just to show that it
		 * works. These events do not carry person information and normally a
		 * LabelerSensitivity is only used with such events.
		 */

		LabelerSensitivity<PartitionAdditionEvent> labelerSensitivity2 = new LabelerSensitivity<>(PartitionAdditionEvent.class, (e) -> Optional.ofNullable(null));
		assertEquals(PartitionAdditionEvent.class, labelerSensitivity2.getEventClass());

		LabelerSensitivity<PartitionRemovalEvent> labelerSensitivity3 = new LabelerSensitivity<>(PartitionRemovalEvent.class, (e) -> Optional.ofNullable(null));
		assertEquals(PartitionRemovalEvent.class, labelerSensitivity3.getEventClass());

	}

	@Test
	@UnitTestMethod(name = "getPersonId", args = { Event.class })
	public void testGetPersonId() {
		
		LabelerSensitivity<Event> labelerSensitivity = new LabelerSensitivity<>(Event.class, (e) -> Optional.empty());
		Optional<PersonId> optional = labelerSensitivity.getPersonId( new Event() {
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