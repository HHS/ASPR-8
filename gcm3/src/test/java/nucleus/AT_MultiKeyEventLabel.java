package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import nucleus.util.ContractException;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

/**
 * Unit test for MultiKeyEventLabel.
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = EventLabel.class)
public class AT_MultiKeyEventLabel {

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
	@UnitTestConstructor(args = { Object.class, EventLabelerId.class, Class.class, Object[].class })
	public void testConstructor() {
		// precondition tests

		ContractException contractException = assertThrows(ContractException.class, () -> new EventLabel<>(null, eventLabelerId1, EventA.class));
		assertEquals(NucleusError.NULL_PRIMARY_KEY_VALUE, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> new EventLabel<>(EventA.class, null, EventA.class));
		assertEquals(NucleusError.NULL_LABELER_ID_IN_EVENT_LABEL, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> new EventLabel<>(EventA.class, eventLabelerId1, null));
		assertEquals(NucleusError.NULL_EVENT_CLASS_IN_EVENT_LABEL, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "hashCode", args = {})
	public void testHashCode() {
		//Equal event labels have equal auxiliary keys, but may disagree on other constructor arguments
		
		//Show equal objects have equal hash codes
		EventLabel<EventA> multiKeyEventLabel1 = new EventLabel<>("key1", eventLabelerId1, EventA.class, 1, 2, 3);
		EventLabel<EventB> multiKeyEventLabel2 = new EventLabel<>("key2", eventLabelerId2, EventB.class, 1, 2, 3);		
		assertEquals(multiKeyEventLabel1.hashCode(), multiKeyEventLabel2.hashCode());
		
		
	}

	@Test
	@UnitTestMethod(name = "equals", args = { Object.class })
	public void testEquals() {
		//Equal event labels have equal auxiliary keys, but may disagree on other constructor arguments
		
		//show that matching auxiliary keys forces equality
		EventLabel<EventA> multiKeyEventLabel1 = new EventLabel<>("key1", eventLabelerId1, EventA.class, 1, 2, 3);
		EventLabel<EventB> multiKeyEventLabel2 = new EventLabel<>("key2", eventLabelerId2, EventB.class, 1, 2, 3);		
		assertEquals(multiKeyEventLabel1, multiKeyEventLabel2);
		

		//show that non-matching auxiliary keys forcec non-equality
		EventLabel<EventA> multiKeyEventLabel3 = new EventLabel<>("key1", eventLabelerId1, EventA.class, 1, 2, 3);
		EventLabel<EventA> multiKeyEventLabel4 = new EventLabel<>("key1", eventLabelerId1, EventA.class, 1, 2);
		assertNotEquals(multiKeyEventLabel3, multiKeyEventLabel4);

	}

	@Test
	@UnitTestMethod(name = "getEventClass", args = {})
	public void testGetEventClass() {
		EventLabel<EventA> eventLabelA = new EventLabel<>("key", eventLabelerId1, EventA.class);
		assertEquals(EventA.class, eventLabelA.getEventClass());
		
		EventLabel<EventB> eventLabelB = new EventLabel<>("key", eventLabelerId1, EventB.class);
		assertEquals(EventB.class, eventLabelB.getEventClass());
		
		EventLabel<EventC> eventLabelC = new EventLabel<>("key", eventLabelerId1, EventC.class);
		assertEquals(EventC.class, eventLabelC.getEventClass());
	}

	@Test
	@UnitTestMethod(name = "getLabelerId", args = {})
	public void testGetLabelerId() {
		
		EventLabelerId eventLabelerId1 = new EventLabelerId() {};
		EventLabelerId eventLabelerId2 = new EventLabelerId() {};
		EventLabelerId eventLabelerId3 = new EventLabelerId() {};
		
			
		EventLabel<EventA> eventLabel = new EventLabel<>("key", eventLabelerId1, EventA.class);
		assertEquals(eventLabelerId1, eventLabel.getLabelerId());
		
		eventLabel = new EventLabel<>("key", eventLabelerId2, EventA.class);
		assertEquals(eventLabelerId2, eventLabel.getLabelerId());
		
		eventLabel = new EventLabel<>("key", eventLabelerId3, EventA.class);
		assertEquals(eventLabelerId3, eventLabel.getLabelerId());

		
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {

		for (int i = 0; i < 10; i++) {
			Object key = "key" + i;
			EventLabel<EventA> eventLabel = new EventLabel<>(key, eventLabelerId1, EventA.class);
			assertEquals(key, eventLabel.getPrimaryKeyValue());
		}
	}

}
