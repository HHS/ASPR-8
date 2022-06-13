package plugins.groups.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import util.errors.ContractException;

@UnitTest(target = GroupId.class)
public class AT_GroupId {

	@Test
	@UnitTestConstructor(args = {int.class})
	public void testConstructor() {
		
		//precondition test: if the id < 0		
		ContractException contractException = assertThrows(ContractException.class, ()->new GroupId(-1));
		assertEquals(GroupError.NEGATIVE_GROUP_ID, contractException.getErrorType());

	}
}
