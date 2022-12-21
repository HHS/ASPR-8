package plugins.groups.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.groups.support.GroupTypeId;
import plugins.groups.testsupport.TestGroupTypeId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = GroupTypeAdditionEvent.class)
public class AT_GroupTypeAdditionEvent {

	@Test
	@UnitTestConstructor(args = { GroupTypeId.class })
	public void testConstructor() {
		// nothing to test
	}

}
