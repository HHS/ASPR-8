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
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_GroupPropertyDefinitionEvent {

	@Test
	@UnitTestConstructor(target = GroupPropertyDefinitionEvent.class, args = { GroupTypeId.class, GroupPropertyId.class })
	public void testConstructor() {
		GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
		GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;

		assertNotNull(new GroupPropertyDefinitionEvent(groupTypeId, groupPropertyId));

		// precondition: group type id is null
		ContractException contractException = assertThrows(ContractException.class, () -> new GroupPropertyDefinitionEvent(null, groupPropertyId));
		assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

		// precondition: group property id is null
		contractException = assertThrows(ContractException.class, () -> new GroupPropertyDefinitionEvent(groupTypeId, null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupPropertyDefinitionEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GroupPropertyDefinitionEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GroupPropertyDefinitionEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GroupPropertyDefinitionEvent.class, name = "groupTypeId", args = {})
	public void testGroupTypeId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GroupPropertyDefinitionEvent.class, name = "groupPropertyId", args = {})
	public void testGroupPropertyId() {
		// nothing to test
	}

}
