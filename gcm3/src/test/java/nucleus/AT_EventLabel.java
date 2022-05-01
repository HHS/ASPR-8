package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

/**
 * Unit test for MultiKeyEventLabel.
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = EventLabel.class)
public class AT_EventLabel {

	private static class EventA implements Event {

	}

	private static class EventB implements Event {

	}

	private static class EventC implements Event {

	}

	private static EventLabelerId eventLabelerId1 = new EventLabelerId() {

	};

	private static EventLabelerId eventLabelerId2 = new EventLabelerId() {

	};

	@Test
	@UnitTestMethod(target = EventLabel.Builder.class, name = "builder", args = {})
	public void testBuilder() {

		// show that a builder instance is returned
		assertNotNull(EventLabel.builder(EventA.class));

		// precondition test: if the class reference is null
		ContractException contractException = assertThrows(ContractException.class, () -> EventLabel.builder(null));
		assertEquals(NucleusError.NULL_EVENT_CLASS, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = EventLabel.Builder.class, name = "addKey", args = { Object.class })
	public void testAddKey() {

		EventLabel<EventA> eventLabel = EventLabel.builder(EventA.class).setEventLabelerId(eventLabelerId1).addKey(54).build();
		assertEquals(54, eventLabel.getPrimaryKeyValue());

		// precondition test: if a key is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			EventLabel.builder(EventA.class).setEventLabelerId(eventLabelerId1).addKey(1).addKey(null).addKey(3).build();
		});

		assertEquals(NucleusError.NULL_EVENT_LABEL_KEY, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = EventLabel.Builder.class, name = "setEventLabelerId", args = { EventLabelerId.class })
	public void testSetEventLabelerId() {
		EventLabel<EventA> eventLabel = EventLabel.builder(EventA.class).setEventLabelerId(eventLabelerId1).addKey(54).build();
		assertEquals(eventLabelerId1, eventLabel.getLabelerId());

		// precondition test: if the labeler id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			EventLabel.builder(EventA.class).setEventLabelerId(null).addKey(54).build();
		});

		assertEquals(NucleusError.NULL_EVENT_LABELER_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = EventLabel.Builder.class, name = "build", args = { EventLabelerId.class })
	public void testBuild() {

		// show that build returns a label
		EventLabel<EventA> eventLabel = EventLabel.builder(EventA.class).setEventLabelerId(eventLabelerId1).addKey(54).build();
		assertNotNull(eventLabel);

		// precondition test: if no keys were added
		ContractException contractException = assertThrows(ContractException.class, () -> EventLabel.builder(EventA.class).setEventLabelerId(eventLabelerId1).build());
		assertEquals(NucleusError.NULL_PRIMARY_KEY_VALUE, contractException.getErrorType());

		// precondition test: if no labeler id is set
		contractException = assertThrows(ContractException.class, () -> EventLabel.builder(EventA.class).addKey(54).build());
		assertEquals(NucleusError.NULL_LABELER_ID_IN_EVENT_LABEL, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "hashCode", args = {})
	public void testHashCode() {
		// Equal event labels have equal auxiliary keys, but may disagree on
		// other constructor arguments

		// Show equal objects have equal hash codes
		EventLabel<EventA> multiKeyEventLabel1 = EventLabel	.builder(EventA.class).setEventLabelerId(eventLabelerId1)//
															.addKey(1)//
															.addKey(2)//
															.addKey(3)//
															.build();

		EventLabel<EventB> multiKeyEventLabel2 = EventLabel	.builder(EventB.class).setEventLabelerId(eventLabelerId2)//
															.addKey(1)//
															.addKey(2)//
															.addKey(3)//
															.build();

		assertEquals(multiKeyEventLabel1.hashCode(), multiKeyEventLabel2.hashCode());

	}

	@Test
	@UnitTestMethod(name = "equals", args = { Object.class })
	public void testEquals() {
		// Equal event labels have equal auxiliary keys, but may disagree on
		// other constructor arguments

		// show that matching auxiliary keys forces equality
		EventLabel<EventA> multiKeyEventLabel1 = EventLabel	.builder(EventA.class)//
															.setEventLabelerId(eventLabelerId1)//
															.addKey(1)//
															.addKey(2)//
															.addKey(3)//
															.build();//

		EventLabel<EventB> multiKeyEventLabel2 = EventLabel	.builder(EventB.class)//
															.setEventLabelerId(eventLabelerId2)//
															.addKey(1)//
															.addKey(2)//
															.addKey(3)//
															.build();//

		assertEquals(multiKeyEventLabel1, multiKeyEventLabel2);

		// show that non-matching auxiliary keys forcec non-equality
		EventLabel<EventA> multiKeyEventLabel3 = EventLabel	.builder(EventA.class)//
															.setEventLabelerId(eventLabelerId1)//
															.addKey(1)//
															.addKey(2)//
															.addKey(3)//
															.build();//

		EventLabel<EventA> multiKeyEventLabel4 = EventLabel	.builder(EventA.class)//
															.setEventLabelerId(eventLabelerId1)//
															.addKey(1)//
															.addKey(2)//
															.build();//
		assertNotEquals(multiKeyEventLabel3, multiKeyEventLabel4);

	}

	@Test
	@UnitTestMethod(name = "getEventClass", args = {})
	public void testGetEventClass() {
		EventLabel<EventA> eventLabelA = EventLabel	.builder(EventA.class)//
													.setEventLabelerId(eventLabelerId1)//
													.addKey("key")//
													.build();//
		assertEquals(EventA.class, eventLabelA.getEventClass());

		EventLabel<EventB> eventLabelB = EventLabel	.builder(EventB.class)//
													.setEventLabelerId(eventLabelerId1)//
													.addKey("key")//
													.build();//

		assertEquals(EventB.class, eventLabelB.getEventClass());

		EventLabel<EventC> eventLabelC = EventLabel	.builder(EventC.class)//
													.setEventLabelerId(eventLabelerId1)//
													.addKey("key")//
													.build();//
		assertEquals(EventC.class, eventLabelC.getEventClass());
	}

	@Test
	@UnitTestMethod(name = "getLabelerId", args = {})
	public void testGetLabelerId() {

		EventLabelerId eventLabelerId1 = new EventLabelerId() {
		};
		EventLabelerId eventLabelerId2 = new EventLabelerId() {
		};
		EventLabelerId eventLabelerId3 = new EventLabelerId() {
		};

		EventLabel<EventA> eventLabel = EventLabel	.builder(EventA.class)//
													.setEventLabelerId(eventLabelerId1)//
													.addKey("key")//
													.build();//

		assertEquals(eventLabelerId1, eventLabel.getLabelerId());

		eventLabel = EventLabel	.builder(EventA.class)//
								.setEventLabelerId(eventLabelerId2)//
								.addKey("key")//
								.build();//

		assertEquals(eventLabelerId2, eventLabel.getLabelerId());

		eventLabel = EventLabel	.builder(EventA.class)//
								.setEventLabelerId(eventLabelerId3)//
								.addKey("key")//
								.build();//

		assertEquals(eventLabelerId3, eventLabel.getLabelerId());

	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {

		for (int i = 0; i < 10; i++) {
			Object key = "key" + i;
			EventLabel<EventA> eventLabel = EventLabel	.builder(EventA.class)//
														.setEventLabelerId(eventLabelerId1)//
														.addKey(key)//
														.build();//

			assertEquals(key, eventLabel.getPrimaryKeyValue());
		}
	}

}
