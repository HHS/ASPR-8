package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.BiFunction;

import javax.naming.Context;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = EventLabeler.class)
public class AT_EventLabeler {

	@Test
	@UnitTestConstructor(args = { EventLabelerId.class, BiFunction.class })
	public void testConstructor() {
		// nothing to test
	}

	private final static EventLabelerId id = new EventLabelerId() {
	};

	private static class TestEvent implements Event {
	}

	@Test
	@UnitTestMethod(name = "getEventClass", args = {})
	public void testGetEventClass() {
		EventLabeler<TestEvent> eventLabeler = EventLabeler	.builder(TestEvent.class)//
															.setEventLabelerId(id)//
															.setLabelFunction((c, t) -> {
																return null;
															})//
															.build();
		assertEquals(TestEvent.class, eventLabeler.getEventClass());
	}

	@Test
	@UnitTestMethod(name = "getEventLabel", args = { Context.class, Event.class })
	public void testGetEventLabel() {
		// create an event label that will be replicated by the simple event
		// labeler
		MultiKeyEventLabel<TestEvent> expectedEventLabel = new MultiKeyEventLabel<>(TestEvent.class, id, TestEvent.class, 1, 2, 3);

		// create a simple event labeler that will create the same event label
		// as above
		EventLabeler<TestEvent> eventLabeler = EventLabeler	.builder(TestEvent.class)//
															.setEventLabelerId(id).setLabelFunction((c, t) -> {
																return expectedEventLabel;
															}).build();

		// generate an event label from the labeler
		EventLabel<TestEvent> actualEventLabel = eventLabeler.getEventLabel(null, null);
		assertEquals(expectedEventLabel, actualEventLabel);
		/*
		 * Show that the generated event label is equal to the expected event
		 * label
		 * 
		 * Due to the streamlined, non-standard equality contract of
		 * MultiKeyEventLabel, we need to show that the non-key fields are also
		 * equal.
		 */
		assertEquals(expectedEventLabel.getEventClass(), actualEventLabel.getEventClass());
		assertEquals(expectedEventLabel.getLabelerId(), actualEventLabel.getLabelerId());
		assertEquals(expectedEventLabel.getPrimaryKeyValue(), actualEventLabel.getPrimaryKeyValue());

	}

	@Test
	@UnitTestMethod(name = "getEventLabelerId", args = {})
	public void testGetEventLabelerId() {
		EventLabeler<TestEvent> eventLabeler = EventLabeler	.builder(TestEvent.class)//
															.setEventLabelerId(id)//
															.setLabelFunction((c, t) -> {
																return null;
															})//
															.build();
		assertEquals(id, eventLabeler.getEventLabelerId());
	}

}
