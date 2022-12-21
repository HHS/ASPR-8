package plugins.groups.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.groups.support.GroupId;
import plugins.people.support.PersonId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = GroupMembershipAdditionEvent.class)
public class AT_GroupMembershipAdditionEvent {

	@Test
	@UnitTestConstructor(args = { PersonId.class, GroupId.class })
	public void testConstructor() {
		// nothing to test
	}
}
