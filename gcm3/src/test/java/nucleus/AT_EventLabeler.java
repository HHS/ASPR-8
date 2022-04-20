package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.naming.Context;

import org.junit.jupiter.api.Test;

import nucleus.util.ContractException;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;

@UnitTest(target = EventLabeler.class)
public class AT_EventLabeler {

	@Test
	@UnitTestMethod(target = EventLabeler.Builder.class, name = "build", args = {})
	public void testBuild() {

		// precondition test: if the id is not set
		ContractException contractException = assertThrows(ContractException.class, () -> EventLabeler	.builder(TestEvent.class)//
																										.setLabelFunction((c, t) -> {
																											return new EventLabel<>(TestEvent.class, id, TestEvent.class);
																										})//
																										.build());

		assertEquals(NucleusError.NULL_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

		// precondition test: if the label function is not set

		contractException = assertThrows(ContractException.class, () -> EventLabeler.builder(TestEvent.class)//
																					.setEventLabelerId(id)//
																					.build());

		assertEquals(NucleusError.NULL_EVENT_LABEL_FUNCTION, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = EventLabeler.Builder.class, name = "setEventLabelerId", args = {})
	public void testSetEventLabelerId() {
		EventLabeler<TestEvent> eventLabeler = EventLabeler	.builder(TestEvent.class)//
															.setEventLabelerId(id)//
															.setLabelFunction((c, t) -> {
																return new EventLabel<>(TestEvent.class, id, TestEvent.class);
															})//
															.build();
		assertEquals(id, eventLabeler.getEventLabelerId());

		// precondition test: if the id is null
		ContractException contractException = assertThrows(ContractException.class, () -> EventLabeler	.builder(TestEvent.class)//
																										.setEventLabelerId(null)//
																										.setLabelFunction((c, t) -> {
																											return new EventLabel<>(TestEvent.class, id, TestEvent.class);
																										})//
																										.build());

		assertEquals(NucleusError.NULL_EVENT_LABELER_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = EventLabeler.Builder.class, name = "setLabelFunction", args = { Context.class, Event.class })
	public void testSetLabelFunction() {
		// create an event label that will be replicated by the simple event
		// labeler
		EventLabel<TestEvent> expectedEventLabel = new EventLabel<>(TestEvent.class, id, TestEvent.class, 1, 2, 3);

		// create a simple event labeler that will create the same event label
		// as above
		EventLabeler<TestEvent> eventLabeler = EventLabeler	.builder(TestEvent.class)//
															.setEventLabelerId(id)//
															.setLabelFunction((c, t) -> {
																return expectedEventLabel;
															})//
															.build();

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

		// precondition test: if the label function is set to null

		ContractException contractException = assertThrows(ContractException.class, () -> EventLabeler	.builder(TestEvent.class)//
																										.setEventLabelerId(id)//
																										.setLabelFunction(null)//
																										.build());
		assertEquals(NucleusError.NULL_EVENT_LABEL_FUNCTION, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "builder", args = { Class.class })
	public void testBuilder() {
		// show that the builder is returned
		assertNotNull(EventLabeler.builder(TestEvent.class));

		// precondition test: if the class reference is null
		ContractException contractException = assertThrows(ContractException.class, () -> EventLabeler.builder(null));
		assertEquals(NucleusError.NULL_EVENT_CLASS, contractException.getErrorType());
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
																return new EventLabel<>(TestEvent.class, id, TestEvent.class);
															})//
															.build();
		assertEquals(TestEvent.class, eventLabeler.getEventClass());
	}

	@Test
	@UnitTestMethod(name = "getEventLabel", args = { Context.class, Event.class })
	public void testGetEventLabel() {
		// create an event label that will be replicated by the simple event
		// labeler
		EventLabel<TestEvent> expectedEventLabel = new EventLabel<>(TestEvent.class, id, TestEvent.class, 1, 2, 3);

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
																return new EventLabel<>(TestEvent.class, id, TestEvent.class);
															})//
															.build();
		assertEquals(id, eventLabeler.getEventLabelerId());
	}

}
