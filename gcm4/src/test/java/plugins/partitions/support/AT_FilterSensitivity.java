package plugins.partitions.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import nucleus.SimulationContext;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.partitions.testsupport.PartitionsTestPluginFactory;
import plugins.people.support.PersonId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_FilterSensitivity {

	@Test
	@UnitTestConstructor(target = FilterSensitivity.class,args = { Class.class, EventPredicate.class })
	public void testConstructor() {
		// nothing to test
	}
	
	private static class Event2 implements Event{}
	private static class Event3 implements Event{}

	@Test
	@UnitTestMethod(target = FilterSensitivity.class,name = "getEventClass", args = {})
	public void testGetEventClass() {

		FilterSensitivity<Event> filterSensitivity1 = new FilterSensitivity<>(Event.class, (c, e) -> Optional.empty());
		assertEquals(Event.class, filterSensitivity1.getEventClass());

		/*
		 * Note that we are using two event types here just to show that it
		 * works. These events do not carry person information and normally a
		 * FilterSensitivity is only used with such events.
		 */
		FilterSensitivity<Event2> filterSensitivity2 = new FilterSensitivity<>(Event2.class, (c, e) -> Optional.empty());
		assertEquals(Event2.class, filterSensitivity2.getEventClass());

		FilterSensitivity<Event3> filterSensitivity3 = new FilterSensitivity<>(Event3.class, (c, e) -> Optional.empty());
		assertEquals(Event3.class, filterSensitivity3.getEventClass());

	}

	@Test
	@UnitTestMethod(target = FilterSensitivity.class,name = "requiresRefresh", args = { SimulationContext.class, Event.class })
	public void testRequiresRefresh() {
		TestSimulation.executeSimulation(PartitionsTestPluginFactory.factory(10, 8678712526990350206L, (context)->{
			FilterSensitivity<Event> filterSensitivity = new FilterSensitivity<>(Event.class, (c, e) -> Optional.empty());
			Optional<PersonId> optional = filterSensitivity.requiresRefresh(context, new Event() {
			});
			assertFalse(optional.isPresent());

			PersonId personId = new PersonId(0);
			filterSensitivity = new FilterSensitivity<>(Event.class, (c, e) -> Optional.of(personId));
			optional = filterSensitivity.requiresRefresh(context, new Event() {
			});
			assertTrue(optional.isPresent());
			assertEquals(personId, optional.get());			
		}).getPlugins());

	}

}