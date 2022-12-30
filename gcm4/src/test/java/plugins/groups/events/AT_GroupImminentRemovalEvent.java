package plugins.groups.events;

import org.junit.jupiter.api.Test;

import plugins.groups.support.GroupId;
import tools.annotations.UnitTestConstructor;

public class AT_GroupImminentRemovalEvent {

	@Test
	@UnitTestConstructor(target = GroupImminentRemovalEvent.class,args = { GroupId.class })
	public void testConstructor() {
		// nothing to test
	}
	
}
