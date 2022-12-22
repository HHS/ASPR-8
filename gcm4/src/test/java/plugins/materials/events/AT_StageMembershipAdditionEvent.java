package plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.materials.support.BatchId;
import plugins.materials.support.StageId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = StageMembershipAdditionEvent.class)
public class AT_StageMembershipAdditionEvent {
	

	@Test
	@UnitTestConstructor(args = {BatchId.class, StageId.class})
	public void testConstructor() {
		//nothing to test
	}
}
