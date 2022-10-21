package plugins.groups.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

@UnitTest(target = GroupId.class)
public class AT_GroupId {

	
	@Test
	@UnitTestConstructor(args = { int.class })
	public void testConstructor() {
		for (int i = 0; i < 10; i++) {
			GroupId GroupId = new GroupId(i);
			assertEquals(i, GroupId.getValue());
		}
		//precondition test: if the id < 0		
		ContractException contractException = assertThrows(ContractException.class, ()->new GroupId(-1));
		assertEquals(GroupError.NEGATIVE_GROUP_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "compareTo", args = { GroupId.class })
	public void testCompareTo() {
		for (int i = 0; i < 10; i++) {
			GroupId groupA = new GroupId(i);
			for (int j = 0; j < 10; j++) {
				GroupId groupB = new GroupId(j);
				int comparisonValue = groupA.compareTo(groupB);
				if (i < j) {
					assertTrue(comparisonValue < 0);
				} else if (i > j) {
					assertTrue(comparisonValue > 0);
				} else {
					assertTrue(comparisonValue == 0);
				}
			}
		}
	}

	@Test
	@UnitTestMethod(name = "equals", args = { Object.class })
	public void testEquals() {
		for (int i = 0; i < 10; i++) {
			GroupId groupA = new GroupId(i);
			for (int j = 0; j < 10; j++) {
				GroupId groupB = new GroupId(j);				
				if (i == j) {
					assertEquals(groupA,groupB);
				} else {
					assertNotEquals(groupA,groupB);
				}
			}
		}
	}

	@Test
	@UnitTestMethod(name = "getValue", args = {})
	public void testGetValue() {
		for (int i = 0; i < 10; i++) {
			GroupId group = new GroupId(i);
			assertEquals(i, group.getValue());
		}
	}

	@Test
	@UnitTestMethod(name = "hashCode", args = {})
	public void testHashCode() {
		for (int i = 0; i < 10; i++) {
			GroupId group = new GroupId(i);
			assertEquals(i, group.hashCode());
		}
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		for (int i = 0; i < 10; i++) {
			GroupId group = new GroupId(i);
			assertEquals(Integer.toString(i), group.toString());
		}
	}
	
}
