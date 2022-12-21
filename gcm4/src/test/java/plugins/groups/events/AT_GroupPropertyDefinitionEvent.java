package plugins.groups.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import plugins.groups.support.GroupError;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupTypeId;
import plugins.groups.testsupport.TestGroupPropertyId;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.util.properties.PropertyError;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

@UnitTest(target = GroupPropertyDefinitionEvent.class)
public class AT_GroupPropertyDefinitionEvent {

	@Test
	@UnitTestConstructor(args = { GroupTypeId.class, GroupPropertyId.class })
	public void testConstructor() {
		GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
		GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;

		assertNotNull(new GroupPropertyDefinitionEvent(groupTypeId, groupPropertyId));

		// precondition: group type id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> new GroupPropertyDefinitionEvent(null, groupPropertyId));
		assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

		// precondition: group property id is null
		contractException = assertThrows(ContractException.class,
				() -> new GroupPropertyDefinitionEvent(groupTypeId, null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

	}
}
