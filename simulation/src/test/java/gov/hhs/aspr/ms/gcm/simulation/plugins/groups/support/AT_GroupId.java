package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_GroupId {

	
	@Test
	@UnitTestConstructor(target = GroupId.class,args = { int.class })
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
	@UnitTestMethod(target = GroupId.class,name = "compareTo", args = { GroupId.class })
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
	@UnitTestMethod(target = GroupId.class,name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1974882207275755576L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			GroupId groupId = getRandomGroupId(randomGenerator.nextLong());
			assertFalse(groupId.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			GroupId groupId = getRandomGroupId(randomGenerator.nextLong());
			assertFalse(groupId.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			GroupId groupId = getRandomGroupId(randomGenerator.nextLong());
			assertTrue(groupId.equals(groupId));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			GroupId groupId1 = getRandomGroupId(seed);
			GroupId groupId2 = getRandomGroupId(seed);
			assertFalse(groupId1 == groupId2);
			for (int j = 0; j < 10; j++) {
				assertTrue(groupId1.equals(groupId2));
				assertTrue(groupId2.equals(groupId1));
			}
		}

		// different inputs yield unequal groupIds
		Set<GroupId> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			GroupId groupId = getRandomGroupId(randomGenerator.nextLong());
			set.add(groupId);
		}
		assertEquals(100, set.size());
	}

	@Test
	@UnitTestMethod(target = GroupId.class,name = "getValue", args = {})
	public void testGetValue() {
		for (int i = 0; i < 10; i++) {
			GroupId group = new GroupId(i);
			assertEquals(i, group.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = GroupId.class,name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(389994196593528301L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			GroupId groupId1 = getRandomGroupId(seed);
			GroupId groupId2 = getRandomGroupId(seed);

			assertEquals(groupId1, groupId2);
			assertEquals(groupId1.hashCode(), groupId2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			GroupId groupId = getRandomGroupId(randomGenerator.nextLong());
			hashCodes.add(groupId.hashCode());
		}

		assertEquals(100, hashCodes.size());
	}

	@Test
	@UnitTestMethod(target = GroupId.class,name = "toString", args = {})
	public void testToString() {
		for (int i = 0; i < 10; i++) {
			GroupId group = new GroupId(i);
			assertEquals(Integer.toString(i), group.toString());
		}
	}

	private GroupId getRandomGroupId(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		return new GroupId(randomGenerator.nextInt(Integer.MAX_VALUE));
	}
	
}
