package plugins.groups.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.util.ContractException;
import plugins.groups.testsupport.TestGroupTypeId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
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
	@UnitTestMethod(target = BulkGroupMembershipData.Builder.class, name = "build", args = {})
	public void testBuild() {
		ContractException contractException = assertThrows(ContractException.class, () -> BulkGroupMembershipData.builder().addPersonToGroup(0, 0).build());
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

		assertEquals(3, bulkGroupMembershipData.getGroupCount());

		assertEquals(TestGroupTypeId.GROUP_TYPE_1, bulkGroupMembershipData.getGroupTypeId(0));

		assertEquals(TestGroupTypeId.GROUP_TYPE_2, bulkGroupMembershipData.getGroupTypeId(1));

		assertEquals(TestGroupTypeId.GROUP_TYPE_3, bulkGroupMembershipData.getGroupTypeId(2));

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
		List<Integer> personIndices = bulkGroupMembershipData.getPersonIndices();
		for (Integer personIndex : personIndices) {
			List<Integer> groupIndices = bulkGroupMembershipData.getGroupIndicesForPersonIndex(personIndex);
			for (Integer groupIndex : groupIndices) {
				actualPairs.add(new MultiKey(personIndex, groupIndex));
			}
		}

		// show that the bulk group membership data contains the expected
		// pairings
		assertEquals(expectedPairs, actualPairs);

	}

	@Test
	@UnitTestMethod(name = "getGroupCount", args = {})
	public void testGetGroupCount() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6040970044186644538L);

		// show that zero to nine groups result in the correct group count
		for (int i = 0; i < 10; i++) {
			BulkGroupMembershipData.Builder builder = BulkGroupMembershipData.builder();
			for (int j = 0; j < i; j++) {
				builder.addGroup(TestGroupTypeId.getRandomGroupTypeId(randomGenerator));
			}
			BulkGroupMembershipData bulkGroupMembershipData = builder.build();
			assertEquals(i, bulkGroupMembershipData.getGroupCount());
		}
	}

	@Test
	@UnitTestMethod(name = "getGroupTypeId", args = { int.class })
	public void testGetGroupTypeId() {
		Map<Integer, GroupTypeId> expectedGroupTypes = new LinkedHashMap<>();

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8757283026856511464L);

		// Add 30 groups with randomized types
		BulkGroupMembershipData.Builder builder = BulkGroupMembershipData.builder();
		for (int i = 9; i < 30; i++) {
			GroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
			builder.addGroup(groupTypeId);
			expectedGroupTypes.put(expectedGroupTypes.size(), groupTypeId);
		}
		BulkGroupMembershipData bulkGroupMembershipData = builder.build();

		// show that the 30 groups have the correct type
		for (Integer index : expectedGroupTypes.keySet()) {
			assertEquals(expectedGroupTypes.get(index), bulkGroupMembershipData.getGroupTypeId(index));
		}

		// precondition tests
		assertThrows(IndexOutOfBoundsException.class, () -> bulkGroupMembershipData.getGroupTypeId(-1));
		assertThrows(IndexOutOfBoundsException.class, () -> bulkGroupMembershipData.getGroupTypeId(30));

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
		List<Integer> personIndices = bulkGroupMembershipData.getPersonIndices();
		for (Integer personIndex : personIndices) {
			List<Integer> groupIndices = bulkGroupMembershipData.getGroupIndicesForPersonIndex(personIndex);
			for (Integer groupIndex : groupIndices) {
				actualPairs.add(new MultiKey(personIndex, groupIndex));
			}
		}

		// show that the bulk group membership data contains the expected
		// pairings
		assertEquals(expectedPairs, actualPairs);

		// precondition tests -- none

	}

	@Test
	@UnitTestMethod(name = "getPersonIndices", args = {})
	public void testGetPersonIndices() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7269766947053474864L);
		BulkGroupMembershipData.Builder builder = BulkGroupMembershipData.builder();
		
		//show that the getPersonIndices returns an empty list if no people were added
		List<Integer> people = builder.build().getPersonIndices();
		assertNotNull(people);
		assertTrue(people.isEmpty());

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
		List<Integer> actualPeople = bulkGroupMembershipData.getPersonIndices();
		assertEquals(expectedPeople.size(), actualPeople.size());
		assertEquals(expectedPeople, new LinkedHashSet<>(actualPeople));

		// precondition tests -- none
	}

}
