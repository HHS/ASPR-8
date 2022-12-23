package plugins.groups.events;

import org.junit.jupiter.api.Test;

import plugins.groups.support.GroupId;
import plugins.people.support.PersonId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;

@UnitTest(target = GroupMembershipRemovalEvent.class)
public class AT_GroupMembershipRemovalEvent {

	@Test
	@UnitTestConstructor(args = { PersonId.class, GroupId.class })
	public void testConstructor() {
		// nothing to test
	}

}
