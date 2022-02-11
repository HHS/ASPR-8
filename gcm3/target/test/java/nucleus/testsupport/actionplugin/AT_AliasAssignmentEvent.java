package nucleus.testsupport.actionplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = AliasAssignmentEvent.class)
public class AT_AliasAssignmentEvent {
	@Test
	@UnitTestConstructor(args = { Object.class })
	public void test() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getAlias", args = {})
	public void testGetAlias() {
		assertEquals("alias", new AliasAssignmentEvent("alias").getAlias());
		assertEquals(45, new AliasAssignmentEvent(45).getAlias());
		assertEquals(false, new AliasAssignmentEvent(false).getAlias());
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		assertEquals("AliasAssignmentEvent [alias=alias]", new AliasAssignmentEvent("alias").toString());
		assertEquals("AliasAssignmentEvent [alias=45]", new AliasAssignmentEvent(45).toString());
		assertEquals("AliasAssignmentEvent [alias=false]", new AliasAssignmentEvent(false).toString());
	}

}
