package plugins.people;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.PluginData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.people.support.PersonRange;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

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

		actualPersonRanges = PeoplePluginData	.builder()//
												.addPersonRange(new PersonRange(3, 5))//
												.addPersonRange(new PersonRange(1, 5))//
												.addPersonRange(new PersonRange(4, 8)).addPersonRange(new PersonRange(9, 13))//
												.build()//
												.getPersonRanges();

		assertEquals(expectedPersonRanges, actualPersonRanges);

		// precondition test : if a person range is null
		ContractException contractException = assertThrows(ContractException.class, () -> PeoplePluginData.builder().addPersonRange(null));
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

		actualPersonIds = PeoplePluginData	.builder()//
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

		actualPersonRanges = PeoplePluginData	.builder()//
												.addPersonRange(new PersonRange(3, 5))//
												.addPersonRange(new PersonRange(1, 5))//
												.addPersonRange(new PersonRange(4, 8)).addPersonRange(new PersonRange(9, 13))//
												.build()//
												.getPersonRanges();

		assertEquals(expectedPersonRanges, actualPersonRanges);
	}

	@Test
	@UnitTestMethod(target = PeoplePluginData.Builder.class, name = "setPersonCount", args = {int.class})
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
		 * precondition : if the person count is explicitly set to a value less
		 * than or equal to the highest value in an included range
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

		PeoplePluginData pluginData = PeoplePluginData	.builder()//
														.addPersonRange(new PersonRange(3, 9)).addPersonRange(new PersonRange(8, 12)).addPersonRange(new PersonRange(15, 19)).build();

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

}
