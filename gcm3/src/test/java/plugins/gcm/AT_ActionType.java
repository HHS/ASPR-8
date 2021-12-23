package plugins.gcm;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import plugins.gcm.input.ActionType;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

/**
 * Test class for {@link ActionType}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = ActionType.class)
public class AT_ActionType {

	/**
	 * Tests {@link ActionType#toString()}
	 */
	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		for (ActionType actionType : ActionType.values()) {
			String value = actionType.toString();
			assertNotNull(value);
			assertTrue(value.length() > 0);
		}
	}

	/**
	 * Test {@link ActionType#valueOf(String)}
	 */
	@Test
	@UnitTestMethod(name = "valueOf", args = { String.class })
	public void testValueOf() {
		// nothing to test

	}

	/**
	 * Test {@link ActionType#values()}
	 */
	@Test
	@UnitTestMethod(name = "values", args = {})
	public void testValues() {
		// nothing to test
	}

}
