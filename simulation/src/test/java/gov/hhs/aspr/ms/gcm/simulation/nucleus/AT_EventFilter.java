package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;

public class AT_EventFilter {

	private static enum functionId {
		X, Y;
	}

	private static class EventA implements Event {
		private int x;
		private double y;

	}

	private static class EventB implements Event {

	}

	private static class EventC implements Event {

	}

	@Test
	@UnitTestMethod(target = EventFilter.class,name = "builder", args = { Class.class })
	public void testBuilder() {
		// show that a builder instance is returned
		assertNotNull(EventFilter.builder(EventA.class));

		// precondition test: if the class reference is null
		ContractException contractException = assertThrows(ContractException.class, () -> EventFilter.builder(null));
		assertEquals(NucleusError.NULL_EVENT_CLASS, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = EventFilter.class,name = "getEventClass", args = {})
	public void testGetEventClass() {
		/*
		 * Show that the event class used to build the event filter can be
		 * retrieved from the event filter for a few event classes.
		 */

		EventFilter<EventA> eventFilterA = EventFilter.builder(EventA.class).build();
		assertEquals(EventA.class, eventFilterA.getEventClass());

		EventFilter<EventB> eventFilterB = EventFilter.builder(EventB.class).build();
		assertEquals(EventB.class, eventFilterB.getEventClass());

		EventFilter<EventC> eventFilterC = EventFilter.builder(EventC.class).build();
		assertEquals(EventC.class, eventFilterC.getEventClass());

	}

	@Test
	@UnitTestMethod(target = EventFilter.class,name = "getFunctionValuePairs", args = {})
	public void testGetFunctionValuePairs() {
		// create two identifiable functions -- one for each field of EventA
		IdentifiableFunction<EventA> xFunction = new IdentifiableFunction<>(functionId.X, (e) -> e.x);
		IdentifiableFunction<EventA> yFunction = new IdentifiableFunction<>(functionId.Y, (e) -> e.y);
		/*
		 * Create the list of expected pairs that we should be able to retrieve
		 * from the constructed event filter. It will start out empty.
		 */
		List<Pair<IdentifiableFunction<?>, Object>> expectedFunctionValuePairs = new ArrayList<>();

		/*
		 * show that an event filter constructed without any function will
		 * result in an empty list of function value pairs.
		 */
		EventFilter<EventA> eventFilter = EventFilter.builder(EventA.class).build();
		List<Pair<IdentifiableFunction<EventA>, Object>> functionValuePairs = eventFilter.getFunctionValuePairs();
		assertNotNull(functionValuePairs);
		assertEquals(expectedFunctionValuePairs, functionValuePairs);

		/*
		 * Add a function and value for the x field of the event and show that
		 * the expected function value pairs can be retrieved from the event
		 * filter.
		 */
		eventFilter = EventFilter	.builder(EventA.class)//
									.addFunctionValuePair(xFunction, 2)//
									.build();
		functionValuePairs = eventFilter.getFunctionValuePairs();
		expectedFunctionValuePairs.add(new Pair<>(xFunction, 2));
		assertNotNull(functionValuePairs);
		assertEquals(expectedFunctionValuePairs, functionValuePairs);

		/*
		 * Add another function and value for the y field of the event and show
		 * that the expected function value pairs can be retrieved from the
		 * event filter.
		 */

		eventFilter = EventFilter	.builder(EventA.class)//
									.addFunctionValuePair(xFunction, 2)//
									.addFunctionValuePair(yFunction, 3.0)//
									.build();
		functionValuePairs = eventFilter.getFunctionValuePairs();
		expectedFunctionValuePairs.add(new Pair<>(yFunction, 3.0));
		assertNotNull(functionValuePairs);
		assertEquals(expectedFunctionValuePairs, functionValuePairs);

	}

	@Test
	@UnitTestMethod(target = EventFilter.Builder.class, name = "addFunctionValuePair", args = { IdentifiableFunction.class, Object.class })
	public void addFunctionValuePair() {
		testGetFunctionValuePairs();
	}

	@Test
	@UnitTestMethod(target = EventFilter.Builder.class, name = "build", args = {})
	public void testBuild() {
		testGetEventClass();		
		testGetFunctionValuePairs();
	}

}
