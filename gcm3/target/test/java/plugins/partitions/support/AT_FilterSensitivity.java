package plugins.partitions.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import javax.naming.Context;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import nucleus.testsupport.MockSimulationContext;
import plugins.partitions.events.PartitionAdditionEvent;
import plugins.partitions.events.PartitionRemovalEvent;
import plugins.people.support.PersonId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = FilterSensitivity.class)
public class AT_FilterSensitivity {

	@Test
	@UnitTestConstructor(args = { Class.class, EventPredicate.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getEventClass", args = {})
	public void testGetEventClass() {

		FilterSensitivity<Event> filterSensitivity1 = new FilterSensitivity<>(Event.class, (c, e) -> Optional.empty());
		assertEquals(Event.class, filterSensitivity1.getEventClass());

		/*
		 * Note that we are using two event types here just to show that it
		 * works. These events do not carry person information and normally a
		 * FilterSensitivity is only used with such events.
		 */
		FilterSensitivity<PartitionAdditionEvent> filterSensitivity2 = new FilterSensitivity<>(PartitionAdditionEvent.class, (c, e) -> Optional.empty());
		assertEquals(PartitionAdditionEvent.class, filterSensitivity2.getEventClass());

		FilterSensitivity<PartitionRemovalEvent> filterSensitivity3 = new FilterSensitivity<>(PartitionRemovalEvent.class, (c, e) -> Optional.empty());
		assertEquals(PartitionRemovalEvent.class, filterSensitivity3.getEventClass());

	}

	@Test
	@UnitTestMethod(name = "requiresRefresh", args = { Context.class, Event.class })
	public void testRequiresRefresh() {
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();

		FilterSensitivity<Event> filterSensitivity = new FilterSensitivity<>(Event.class, (c, e) -> Optional.empty());
		Optional<PersonId> optional = filterSensitivity.requiresRefresh(mockSimulationContext, new Event() {
		});
		assertFalse(optional.isPresent());

		PersonId personId = new PersonId(0);
		filterSensitivity = new FilterSensitivity<>(Event.class, (c, e) -> Optional.of(personId));
		optional = filterSensitivity.requiresRefresh(mockSimulationContext, new Event() {
		});
		assertTrue(optional.isPresent());
		assertEquals(personId, optional.get());
	}

}