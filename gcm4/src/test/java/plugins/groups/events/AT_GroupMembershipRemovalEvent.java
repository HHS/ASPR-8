package plugins.groups.events;

import org.junit.jupiter.api.Test;

import plugins.groups.support.GroupId;
import plugins.people.support.PersonId;
import tools.annotations.UnitTestConstructor;

public class AT_GroupMembershipRemovalEvent {

	@Test
	@UnitTestConstructor(target = GroupMembershipRemovalEvent.class, args = { PersonId.class, GroupId.class })
	public void testConstructor() {
		// nothing to test
	}

}
