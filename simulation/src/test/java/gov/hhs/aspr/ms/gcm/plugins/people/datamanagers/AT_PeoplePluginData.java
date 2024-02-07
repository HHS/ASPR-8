package gov.hhs.aspr.ms.gcm.plugins.people.datamanagers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.PluginData;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonRange;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public final class AT_PeoplePluginData {
	@Test
	@UnitTestMethod(target = PeoplePluginData.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(PeoplePluginData.builder());
	}

	@Test
	@UnitTestMethod(target = PeoplePluginData.Builder.class, name = "build", args = {})
	public void testBuild() {

		// RandomGenerator randomGenerator =
		// RandomGeneratorProvider.getRandomGenerator(1713830743266777795L);

		// show that an empty builder returns an empty set of people
		PeoplePluginData peoplePluginData = PeoplePluginData.builder().build();
		assertEquals(0, peoplePluginData.getPersonCount());
		assertTrue(peoplePluginData.getPersonIds().isEmpty());
		assertTrue(peoplePluginData.getPersonRanges().isEmpty());

		// precondition test: if an invalid person count is set
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PeoplePluginData.builder().addPersonRange(new PersonRange(5, 19)).setPersonCount(10).build();
		});
		assertEquals(PersonError.INVALID_PERSON_COUNT, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PeoplePluginData.Builder.class, name = "addPersonRange", args = { PersonRange.class })
	public void testAddPersonRange() {

		List<PersonRange> expectedPersonRanges = new ArrayList<>();
		expectedPersonRanges.add(new PersonRange(3, 8));
		expectedPersonRanges.add(new PersonRange(12, 18));
		expectedPersonRanges.add(new PersonRange(20, 22));

		List<PersonRange> actualPersonRanges = //
				PeoplePluginData.builder()//
						.addPersonRange(new PersonRange(12, 15))//
						.addPersonRange(new PersonRange(3, 8))//
						.addPersonRange(new PersonRange(13, 18))//
						.addPersonRange(new PersonRange(20, 22))//
						.build()//
						.getPersonRanges();

		assertEquals(expectedPersonRanges, actualPersonRanges);

		expectedPersonRanges = new ArrayList<>();
		expectedPersonRanges.add(new PersonRange(1, 13));

		actualPersonRanges = PeoplePluginData.builder()//
				.addPersonRange(new PersonRange(3, 5))//
				.addPersonRange(new PersonRange(1, 5))//
				.addPersonRange(new PersonRange(4, 8)).addPersonRange(new PersonRange(9, 13))//
				.build()//
				.getPersonRanges();

		assertEquals(expectedPersonRanges, actualPersonRanges);

		// precondition test : if a person range is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> PeoplePluginData.builder().addPersonRange(null));
		assertEquals(PersonError.NULL_PERSON_RANGE, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = PeoplePluginData.class, name = "getPersonIds", args = {})
	public void testGetPersonIds() {

		List<PersonId> expectedPersonIds = new ArrayList<>();
		for (int i = 3; i <= 8; i++) {
			expectedPersonIds.add(new PersonId(i));
		}
		for (int i = 12; i <= 18; i++) {
			expectedPersonIds.add(new PersonId(i));
		}
		for (int i = 20; i <= 22; i++) {
			expectedPersonIds.add(new PersonId(i));
		}

		List<PersonId> actualPersonIds = //
				PeoplePluginData.builder()//
						.addPersonRange(new PersonRange(12, 15))//
						.addPersonRange(new PersonRange(3, 8))//
						.addPersonRange(new PersonRange(13, 18))//
						.addPersonRange(new PersonRange(20, 22))//
						.build()//
						.getPersonIds();

		assertEquals(expectedPersonIds, actualPersonIds);

		expectedPersonIds = new ArrayList<>();
		for (int i = 1; i <= 13; i++) {
			expectedPersonIds.add(new PersonId(i));
		}

		actualPersonIds = PeoplePluginData.builder()//
				.addPersonRange(new PersonRange(3, 5))//
				.addPersonRange(new PersonRange(1, 5))//
				.addPersonRange(new PersonRange(4, 8)).addPersonRange(new PersonRange(9, 13))//
				.build()//
				.getPersonIds();

		assertEquals(expectedPersonIds, actualPersonIds);

	}

	@Test
	@UnitTestMethod(target = PeoplePluginData.class, name = "getPersonRanges", args = {})
	public void testGetPersonRanges() {
		List<PersonRange> expectedPersonRanges = new ArrayList<>();
		expectedPersonRanges.add(new PersonRange(3, 8));
		expectedPersonRanges.add(new PersonRange(12, 18));
		expectedPersonRanges.add(new PersonRange(20, 22));

		List<PersonRange> actualPersonRanges = //
				PeoplePluginData.builder()//
						.addPersonRange(new PersonRange(12, 15))//
						.addPersonRange(new PersonRange(3, 8))//
						.addPersonRange(new PersonRange(13, 18))//
						.addPersonRange(new PersonRange(20, 22))//
						.build()//
						.getPersonRanges();

		assertEquals(expectedPersonRanges, actualPersonRanges);

		expectedPersonRanges = new ArrayList<>();
		expectedPersonRanges.add(new PersonRange(1, 13));

		actualPersonRanges = PeoplePluginData.builder()//
				.addPersonRange(new PersonRange(3, 5))//
				.addPersonRange(new PersonRange(1, 5))//
				.addPersonRange(new PersonRange(4, 8)).addPersonRange(new PersonRange(9, 13))//
				.build()//
				.getPersonRanges();

		assertEquals(expectedPersonRanges, actualPersonRanges);
	}

	@Test
	@UnitTestMethod(target = PeoplePluginData.Builder.class, name = "setPersonCount", args = { int.class })
	public void testSetPersonCount() {

		// if the builder is empty
		assertEquals(0, PeoplePluginData.builder().build().getPersonCount());

		// if we explicitly set the person count on an empty builder
		assertEquals(5, PeoplePluginData.builder().setPersonCount(5).build().getPersonCount());

		// if we do not explicitly set the person count
		int actualPersonCount = PeoplePluginData.builder()//
				.addPersonRange(new PersonRange(12, 15))//
				.addPersonRange(new PersonRange(3, 8))//
				.addPersonRange(new PersonRange(13, 18))//
				.addPersonRange(new PersonRange(20, 22))//
				.build()//
				.getPersonCount();
		assertEquals(23, actualPersonCount);

		// if we explicitly set the person count
		actualPersonCount = PeoplePluginData.builder()//
				.addPersonRange(new PersonRange(12, 15))//
				.addPersonRange(new PersonRange(3, 8))//
				.addPersonRange(new PersonRange(13, 18))//
				.addPersonRange(new PersonRange(20, 22))//
				.setPersonCount(45).build()//
				.getPersonCount();
		assertEquals(45, actualPersonCount);

		/*
		 * precondition : if the person count is explicitly set to a value less than or
		 * equal to the highest value in an included range
		 */

		ContractException contractException = assertThrows(ContractException.class, () -> {
			PeoplePluginData.builder()//
					.addPersonRange(new PersonRange(12, 15))//
					.setPersonCount(15).build();
		});//
		assertEquals(PersonError.INVALID_PERSON_COUNT, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = PeoplePluginData.class, name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {

		PeoplePluginData pluginData = PeoplePluginData.builder()//
				.addPersonRange(new PersonRange(3, 9)).addPersonRange(new PersonRange(8, 12))
				.addPersonRange(new PersonRange(15, 19)).build();

		PluginData pluginData2 = pluginData.getCloneBuilder().build();

		assertEquals(pluginData, pluginData2);
	}

	@Test
	@UnitTestMethod(target = PeoplePluginData.Builder.class, name = "setAssignmentTime", args = { double.class })
	public void testSetAssignmentTime() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2239063975495496234L);

		for (int i = 0; i < 30; i++) {
			double expectedAssignmentTime = randomGenerator.nextDouble();
			double actualAssignmentTime = //
					PeoplePluginData.builder()//
							.addPersonRange(new PersonRange(0, 15))//
							.setAssignmentTime(expectedAssignmentTime)//
							.build()//
							.getAssignmentTime();
			assertEquals(expectedAssignmentTime, actualAssignmentTime);
		}

	}

	@Test
	@UnitTestMethod(target = PeoplePluginData.class, name = "getPersonCount", args = {})
	public void testGetPersonCount() {

		// if the builder is empty
		assertEquals(0, PeoplePluginData.builder().build().getPersonCount());

		// if we explicitly set the person count on an empty builder
		assertEquals(5, PeoplePluginData.builder().setPersonCount(5).build().getPersonCount());

		// if we do not explicitly set the person count
		int actualPersonCount = PeoplePluginData.builder()//
				.addPersonRange(new PersonRange(12, 15))//
				.addPersonRange(new PersonRange(3, 8))//
				.addPersonRange(new PersonRange(13, 18))//
				.addPersonRange(new PersonRange(20, 22))//
				.build()//
				.getPersonCount();
		assertEquals(23, actualPersonCount);

		// if we explicitly set the person count
		actualPersonCount = PeoplePluginData.builder()//
				.addPersonRange(new PersonRange(12, 15))//
				.addPersonRange(new PersonRange(3, 8))//
				.addPersonRange(new PersonRange(13, 18))//
				.addPersonRange(new PersonRange(20, 22))//
				.setPersonCount(45).build()//
				.getPersonCount();
		assertEquals(45, actualPersonCount);

	}

	@Test
	@UnitTestMethod(target = PeoplePluginData.class, name = "getAssignmentTime", args = {})
	public void testGetAssignmentTime() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2239063975495496234L);

		for (int i = 0; i < 30; i++) {
			double expectedAssignmentTime = randomGenerator.nextDouble();
			double actualAssignmentTime = //
					PeoplePluginData.builder()//
							.addPersonRange(new PersonRange(0, 15))//
							.setAssignmentTime(expectedAssignmentTime)//
							.build()//
							.getAssignmentTime();
			assertEquals(expectedAssignmentTime, actualAssignmentTime);
		}

	}

	private PeoplePluginData getRandomPeoplePluginData(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		PeoplePluginData.Builder builder = PeoplePluginData.builder();
		int low = 1;
		int high = 1;
		int rangeCount = randomGenerator.nextInt(3) + 1;
		
		for (int i = 0; i < rangeCount; i++) {
			low += randomGenerator.nextInt(10);
			high = low + randomGenerator.nextInt(10) + 1;			
			builder.addPersonRange(new PersonRange(low, high));			
			low = high+1;
			
		}

		builder.setAssignmentTime(randomGenerator.nextDouble() * 100 - 50);
		int personCount = high+randomGenerator.nextInt(5)+1;		
		builder.setPersonCount(personCount);
		return builder.build();

	}
 
	@Test
	@UnitTestMethod(target = PeoplePluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8980821493557306870L);

		// never equal to null
		for (int i = 0; i < 30; i++) {
			PeoplePluginData pluginData = getRandomPeoplePluginData(randomGenerator.nextLong());
			assertFalse(pluginData.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			PeoplePluginData pluginData = getRandomPeoplePluginData(randomGenerator.nextLong());
			assertTrue(pluginData.equals(pluginData));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			PeoplePluginData pluginData1 = getRandomPeoplePluginData(seed);
			PeoplePluginData pluginData2 = getRandomPeoplePluginData(seed);
			for (int j = 0; j < 5; j++) {
				assertTrue(pluginData1.equals(pluginData2));
				assertTrue(pluginData2.equals(pluginData1));
			}
		}

		// different inputs yield unequal plugin datas
		Set<PeoplePluginData> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			PeoplePluginData pluginData = getRandomPeoplePluginData(randomGenerator.nextLong());
			set.add(pluginData);
		}
		assertEquals(100, set.size());
	}
	
	@Test
	@UnitTestMethod(target = PeoplePluginData.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6496930019491275913L);
		
		//equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			PeoplePluginData pluginData1 = getRandomPeoplePluginData(seed);
			PeoplePluginData pluginData2 = getRandomPeoplePluginData(seed);
			
				assertEquals(pluginData1 ,pluginData2);
				assertEquals(pluginData1.hashCode() ,pluginData2.hashCode());
			
		}
		
		//hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			PeoplePluginData pluginData = getRandomPeoplePluginData(randomGenerator.nextLong());
			hashCodes.add(pluginData.hashCode());
		}
		assertTrue(hashCodes.size()>95);
	}
	
	@Test
	@UnitTestMethod(target = PeoplePluginData.class, name = "toString", args = {})
	public void testToString() {
		PeoplePluginData pluginData = getRandomPeoplePluginData(8839731936101813165L);
		
		String actualValue = pluginData.toString();
		
		//Expected value validated by inspection
		String expectedValue = "PeoplePluginData [data=Data ["
				+ "personCount=34, "
				+ "personRanges=["
				+ "PersonRange [lowPersonId=4, highPersonId=13], "
				+ "PersonRange [lowPersonId=19, highPersonId=20], "
				+ "PersonRange [lowPersonId=27, highPersonId=30]], "
				+ "assignmentTime=49.458417619948875, "
				+ "locked=true]]";
		assertEquals(expectedValue, actualValue);
		
	}

}
