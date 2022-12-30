package plugins.groups.events;

import org.junit.jupiter.api.Test;

import plugins.groups.support.GroupTypeId;
import tools.annotations.UnitTestConstructor;


public class AT_GroupTypeAdditionEvent {

	@Test
	@UnitTestConstructor(target = GroupTypeAdditionEvent.class,args = { GroupTypeId.class })
	public void testConstructor() {
		// nothing to test
	}

}
