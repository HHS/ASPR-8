package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
/**
 * Test unit for Event
 * @author Shawn Hatch
 *
 */
@UnitTest(target = Event.class)
public class AT_Event {

	private static class EventA implements Event {

	}

	private static class EventB implements Event {

	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {
		// show that event types have default primary keys equal to their own
		// classes
		assertEquals(EventA.class, new EventA().getPrimaryKeyValue());
		assertEquals(EventB.class, new EventB().getPrimaryKeyValue());

	}
}