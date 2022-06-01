package plugins.groups.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import plugins.groups.testsupport.TestGroupPropertyId;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.people.support.PersonError;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;
import util.wrappers.MultiKey;

@UnitTest(target = BulkGroupMembershipData.class)
public class AT_BulkGroupMembershipData {

	@Test
	@UnitTestMethod(name = "", args = {})
	public void testBuilder() {
		assertNotNull(BulkGroupMembershipData.builder());
	}

	@Test
	@UnitTestMethod(target = BulkGroupMembershipData.Builder.class, name = "setGroupPropertyValue", args = { int.class, GroupPropertyId.class, Object.class })
	public void testSetGroupPropertyValue() {

		BulkGroupMembershipData bulkGroupMembershipData = BulkGroupMembershipData//
																					.builder()//
																					.addGroup(TestGroupTypeId.GROUP_TYPE_1)//
																					.addGroup(TestGroupTypeId.GROUP_TYPE_2)//
																					.addGroup(TestGroupTypeId.GROUP_TYPE_3)//
																					.setGroupPropertyValue(0, TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK, 23.4)//
																					.setGroupPropertyValue(0, TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 19)//
																					.setGroupPropertyValue(1, TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, true)//
																					.setGroupPropertyValue(2, TestGroupPropertyId.GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK, 88)//
																					.setGroupPropertyValue(2, TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK, 14.7)//
																					.build();//

		assertEquals(23.4, bulkGroupMembershipData.getGroupPropertyValue(0, TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK).get());
		assertEquals(19, bulkGroupMembershipData.getGroupPropertyValue(0, TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK).get());
		assertEquals(true, bulkGroupMembershipData.getGroupPropertyValue(1, TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK).get());
		assertEquals(88, bulkGroupMembershipData.getGroupPropertyValue(2, TestGroupPropertyId.GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK).get());
		assertEquals(14.7, bulkGroupMembershipData.getGroupPropertyValue(2, TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK).get());

		// precondition test: if the group id is negative
		ContractException contractException = assertThrows(ContractException.class, () -> BulkGroupMembershipData//
																													.builder()//
																													.addGroup(TestGroupTypeId.GROUP_TYPE_1)//
																													.setGroupPropertyValue(-1,
																															TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK, 23.4)//
																													.build());

		assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

		// precondition test: if the group property id is null
		contractException = assertThrows(ContractException.class, () -> BulkGroupMembershipData//
																								.builder()//
																								.addGroup(TestGroupTypeId.GROUP_TYPE_1)//
																								.setGroupPropertyValue(0, null, 23.4)//
																								.build());

		assertEquals(GroupError.NULL_GROUP_PROPERTY_ID, contractException.getErrorType());

		// precondition test: if the property value is null
		contractException = assertThrows(ContractException.class, () -> BulkGroupMembershipData//
																								.builder()//
																								.addGroup(TestGroupTypeId.GROUP_TYPE_1)//
																								.setGroupPropertyValue(0, TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK, null)//
																								.build());

		assertEquals(GroupError.NULL_GROUP_PROPERTY_VALUE, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = BulkGroupMembershipData.Builder.class, name = "build", args = {})
	public void testBuild() {

		/*
		 * precondition : if a group membership was a negative group index
		 */
		ContractException contractException = assertThrows(ContractException.class, () -> BulkGroupMembershipData.builder().addPersonToGroup(0, 0).build());
		assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

		/*
		 * precondition : if a group membership was added for a group index that
		 * was not added as a group
		 */
		contractException = assertThrows(ContractException.class, () -> BulkGroupMembershipData.builder().addPersonToGroup(0, -1).build());
		assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = BulkGroupMembershipData.Builder.class, name = "addGroup", args = { GroupTypeId.class })
	public void testAddGroup() {

		BulkGroupMembershipData bulkGroupMembershipData = BulkGroupMembershipData//
																					.builder()//
																					.addGroup(TestGroupTypeId.GROUP_TYPE_1)//
																					.addGroup(TestGroupTypeId.GROUP_TYPE_2)//
																					.addGroup(TestGroupTypeId.GROUP_TYPE_3)//
																					.build();//

		List<GroupTypeId> groupTypeIds = bulkGroupMembershipData.getGroupTypeIds();
		
		assertEquals(3, groupTypeIds.size());
		
		assertEquals(TestGroupTypeId.GROUP_TYPE_1, groupTypeIds.get(0));

		assertEquals(TestGroupTypeId.GROUP_TYPE_2, groupTypeIds.get(1));

		assertEquals(TestGroupTypeId.GROUP_TYPE_3, groupTypeIds.get(2));

		// precondition test : if the group type id is null
		ContractException contractException = assertThrows(ContractException.class, () -> BulkGroupMembershipData.builder().addGroup(null));
		assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = BulkGroupMembershipData.Builder.class, name = "addPersonToGroup", args = { int.class, int.class })
	public void testAddPersonToGroup() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4636241749018649588L);
		BulkGroupMembershipData.Builder builder = BulkGroupMembershipData.builder();

		// using 10 groups and 25 people, select 50 unique, random pairings

		// create containers to hold expected group memberships and actual group
		// memberships
		Set<MultiKey> expectedPairs = new LinkedHashSet<>();
		Set<MultiKey> actualPairs = new LinkedHashSet<>();

		// add the 10 groups
		for (int i = 0; i < 10; i++) {
			builder.addGroup(TestGroupTypeId.getRandomGroupTypeId(randomGenerator));
		}

		// create the 250 pairs
		List<MultiKey> pairs = new ArrayList<>();
		for (int i = 0; i < 25; i++) {
			for (int j = 0; j < 10; j++) {
				pairs.add(new MultiKey(i, j));
			}
		}

		// select 50 of the pairs at random to add to the builder
		Collections.shuffle(pairs, new Random(randomGenerator.nextLong()));
		for (int i = 0; i < 50; i++) {
			MultiKey multiKey = (pairs.get(i));
			Integer personIndex = multiKey.getKey(0);
			Integer groupIndex = multiKey.getKey(1);
			builder.addPersonToGroup(personIndex, groupIndex);
			expectedPairs.add(multiKey);
		}

		// build the bulk group membership data
		BulkGroupMembershipData bulkGroupMembershipData = builder.build();//

		// determine the actual pairing from the bulk group membership data
		int personCount = bulkGroupMembershipData.getGroupTypeIds().size();
		for (int personIndex = 0; personIndex < personCount; personCount++) {
			List<Integer> groupIndices = bulkGroupMembershipData.getGroupIndicesForPersonIndex(personIndex);
			for (Integer groupIndex : groupIndices) {
				actualPairs.add(new MultiKey(personIndex, groupIndex));
			}
		}

		// show that the bulk group membership data contains the expected
		// pairings
		assertEquals(expectedPairs, actualPairs);

		// precondition test if the person index is negative
		ContractException contractException = assertThrows(ContractException.class, () -> BulkGroupMembershipData.builder().addPersonToGroup(-1, 0));
		assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		// precondition test: if the group index is negative
		contractException = assertThrows(ContractException.class, () -> BulkGroupMembershipData.builder().addPersonToGroup(0, -1));
		assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

		// precondition test: if the person is already associated with the group
		contractException = assertThrows(ContractException.class, () -> BulkGroupMembershipData.builder().addPersonToGroup(0, 0).addPersonToGroup(0, 0));
		assertEquals(GroupError.DUPLICATE_GROUP_MEMBERSHIP, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getGroupIndicesForPersonIndex", args = { int.class })
	public void testGetGroupIndicesForPersonIndex() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3006860401462487897L);
		BulkGroupMembershipData.Builder builder = BulkGroupMembershipData.builder();

		// using 10 groups and 25 people, select 50 unique, random pairings

		// create containers to hold expected group memberships and actual group
		// memberships
		Set<MultiKey> expectedPairs = new LinkedHashSet<>();
		Set<MultiKey> actualPairs = new LinkedHashSet<>();

		// add the 10 groups
		for (int i = 0; i < 10; i++) {
			builder.addGroup(TestGroupTypeId.getRandomGroupTypeId(randomGenerator));
		}

		// create the 250 pairs
		List<MultiKey> pairs = new ArrayList<>();
		for (int i = 0; i < 25; i++) {
			for (int j = 0; j < 10; j++) {
				pairs.add(new MultiKey(i, j));
			}
		}

		// select 50 of the pairs at random to add to the builder
		Collections.shuffle(pairs, new Random(randomGenerator.nextLong()));
		for (int i = 0; i < 50; i++) {
			MultiKey multiKey = (pairs.get(i));
			Integer personIndex = multiKey.getKey(0);
			Integer groupIndex = multiKey.getKey(1);
			builder.addPersonToGroup(personIndex, groupIndex);
			expectedPairs.add(multiKey);
		}

		// build the bulk group membership data
		BulkGroupMembershipData bulkGroupMembershipData = builder.build();//

		// determine the actual pairing from the bulk group membership data
		int personCount = bulkGroupMembershipData.getPersonCount();
		for (int personIndex = 0;personIndex< personCount;personIndex++) {
			List<Integer> groupIndices = bulkGroupMembershipData.getGroupIndicesForPersonIndex(personIndex);
			for (Integer groupIndex : groupIndices) {
				actualPairs.add(new MultiKey(personIndex, groupIndex));
			}
			assertThrows(UnsupportedOperationException.class, () -> groupIndices.add(1234));
		}

		// show that the bulk group membership data contains the expected
		// pairings
		assertEquals(expectedPairs, actualPairs);

		// precondition tests -- none

	}

	@Test
	@UnitTestMethod(name = "getPersonCount", args = {})
	public void testGetPersonCount() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7269766947053474864L);
		BulkGroupMembershipData.Builder builder = BulkGroupMembershipData.builder();

		// show that the getPersonIndices returns an empty list if no people
		// were added
		int personCount = builder.build().getPersonCount();
		
		assertEquals(0,personCount);

		// using 10 groups and 25 people, select 50 unique, random pairings

		// create a container to hold expected people
		Set<Integer> expectedPeople = new LinkedHashSet<>();

		// add the 10 groups
		for (int i = 0; i < 10; i++) {
			builder.addGroup(TestGroupTypeId.getRandomGroupTypeId(randomGenerator));
		}

		// create the 250 pairs
		List<MultiKey> pairs = new ArrayList<>();
		for (int i = 0; i < 25; i++) {
			for (int j = 0; j < 10; j++) {
				pairs.add(new MultiKey(i, j));
			}
		}

		// select 50 of the pairs at random to add to the builder
		Collections.shuffle(pairs, new Random(randomGenerator.nextLong()));
		for (int i = 0; i < 50; i++) {
			MultiKey multiKey = (pairs.get(i));
			Integer personIndex = multiKey.getKey(0);
			Integer groupIndex = multiKey.getKey(1);
			builder.addPersonToGroup(personIndex, groupIndex);
			expectedPeople.add(personIndex);
		}

		// build the bulk group membership data
		BulkGroupMembershipData bulkGroupMembershipData = builder.build();//

		// determine the actual people from the bulk group membership data
		int actualPeopleCount = bulkGroupMembershipData.getPersonCount();
		assertEquals(expectedPeople.size(), actualPeopleCount);
		

		// precondition tests -- none
	}

	@Test
	@UnitTestMethod(name = "getGroupPropertyIds", args = { int.class })
	public void testGetGroupPropertyIds() {
		BulkGroupMembershipData bulkGroupMembershipData = BulkGroupMembershipData//
																					.builder()//
																					.addGroup(TestGroupTypeId.GROUP_TYPE_1)//
																					.addGroup(TestGroupTypeId.GROUP_TYPE_2)//
																					.addGroup(TestGroupTypeId.GROUP_TYPE_3)//
																					.setGroupPropertyValue(0, TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK, 23.4)//
																					.setGroupPropertyValue(0, TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 19)//
																					.setGroupPropertyValue(1, TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, true)//
																					.setGroupPropertyValue(2, TestGroupPropertyId.GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK, 88)//
																					.setGroupPropertyValue(2, TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK, 14.7)//
																					.build();//

		// show that property values that were set can be retrieved
		Set<GroupPropertyId> expectedGroupPropertyIds = new LinkedHashSet<>();
		expectedGroupPropertyIds.add(TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK);
		expectedGroupPropertyIds.add(TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK);
		assertEquals(expectedGroupPropertyIds, bulkGroupMembershipData.getGroupPropertyIds(0));

		expectedGroupPropertyIds = new LinkedHashSet<>();
		expectedGroupPropertyIds.add(TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK);
		assertEquals(expectedGroupPropertyIds, bulkGroupMembershipData.getGroupPropertyIds(1));

		expectedGroupPropertyIds = new LinkedHashSet<>();
		expectedGroupPropertyIds.add(TestGroupPropertyId.GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK);
		expectedGroupPropertyIds.add(TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK);
		assertEquals(expectedGroupPropertyIds, bulkGroupMembershipData.getGroupPropertyIds(2));
	}

	@Test
	@UnitTestMethod(name = "getGroupPropertyValue", args = { int.class, GroupPropertyId.class })
	public void testGetGroupPropertyValue() {
		BulkGroupMembershipData bulkGroupMembershipData = BulkGroupMembershipData//
																					.builder()//
																					.addGroup(TestGroupTypeId.GROUP_TYPE_1)//
																					.addGroup(TestGroupTypeId.GROUP_TYPE_2)//
																					.addGroup(TestGroupTypeId.GROUP_TYPE_3)//
																					.setGroupPropertyValue(0, TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK, 23.4)//
																					.setGroupPropertyValue(0, TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 19)//
																					.setGroupPropertyValue(1, TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, true)//
																					.setGroupPropertyValue(2, TestGroupPropertyId.GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK, 88)//
																					.setGroupPropertyValue(2, TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK, 14.7)//
																					.build();//

		// show that property values that were set can be retrieved
		assertEquals(23.4, bulkGroupMembershipData.getGroupPropertyValue(0, TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK).get());
		assertEquals(19, bulkGroupMembershipData.getGroupPropertyValue(0, TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK).get());
		assertEquals(true, bulkGroupMembershipData.getGroupPropertyValue(1, TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK).get());
		assertEquals(88, bulkGroupMembershipData.getGroupPropertyValue(2, TestGroupPropertyId.GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK).get());
		assertEquals(14.7, bulkGroupMembershipData.getGroupPropertyValue(2, TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK).get());

		// show that property values that were not set cannot be retrieved
		assertFalse(bulkGroupMembershipData.getGroupPropertyValue(2, TestGroupPropertyId.GROUP_PROPERTY_3_1_BOOLEAN_IMMUTABLE_NO_TRACK).isPresent());
		assertFalse(bulkGroupMembershipData.getGroupPropertyValue(1, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK).isPresent());
		assertFalse(bulkGroupMembershipData.getGroupPropertyValue(1, TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK).isPresent());

	}

}
