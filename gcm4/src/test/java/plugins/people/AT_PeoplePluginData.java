package plugins.people;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

public final class AT_PeoplePluginData {
	@Test
	@UnitTestMethod(target = PeoplePluginData.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(PeoplePluginData.builder());
	}

	@Test
	@UnitTestMethod(target = PeoplePluginData.Builder.class, name = "build", args = {})
	public void testBuild() {

		PeoplePluginData peoplePluginData = PeoplePluginData.builder().build();
		assertTrue(peoplePluginData.getPersonIds().isEmpty());

		Set<PersonId> expectedPersonIds = new LinkedHashSet<>();
		for (int i = 0; i < 10; i++) {
			expectedPersonIds.add(new PersonId(3 * 1 + 5));
		}
		PeoplePluginData.Builder builder = PeoplePluginData.builder();
		for (PersonId personId : expectedPersonIds) {
			builder.addPersonId(personId);
		}

		peoplePluginData = builder.build();
		Set<PersonId> actualPersonIds = new LinkedHashSet<>();
		for (PersonId personId : peoplePluginData.getPersonIds()) {
			if (personId != null) {
				actualPersonIds.add(personId);
			}
		}
		assertEquals(expectedPersonIds, actualPersonIds);

	}

	@Test
	@UnitTestMethod(target = PeoplePluginData.Builder.class, name = "addPersonId", args = { PersonId.class })
	public void testAddPersonId() {
		PeoplePluginData peoplePluginData = PeoplePluginData.builder().build();
		assertTrue(peoplePluginData.getPersonIds().isEmpty());

		Set<PersonId> expectedPersonIds = new LinkedHashSet<>();
		for (int i = 0; i < 10; i++) {
			expectedPersonIds.add(new PersonId(3 * i + 5));
		}
		PeoplePluginData.Builder builder = PeoplePluginData.builder();
		for (PersonId personId : expectedPersonIds) {
			builder.addPersonId(personId);
			// adding duplicate data to show that the value persists
			builder.addPersonId(personId);
		}

		peoplePluginData = builder.build();

		Set<PersonId> actualPersonIds = new LinkedHashSet<>();
		for (PersonId personId : peoplePluginData.getPersonIds()) {
			if (personId != null) {
				actualPersonIds.add(personId);
			}
		}

		assertEquals(expectedPersonIds, actualPersonIds);

		// precondition tests
		builder.addPersonId(new PersonId(5));

		// null person id
		ContractException contractException = assertThrows(ContractException.class, () -> builder.addPersonId(null));
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = PeoplePluginData.class, name = "getPersonIds", args = {})
	public void testGetPersonIds() {
		PeoplePluginData peoplePluginData = PeoplePluginData.builder().build();
		assertTrue(peoplePluginData.getPersonIds().isEmpty());

		Set<PersonId> expectedPersonIds = new LinkedHashSet<>();
		for (int i = 0; i < 10; i++) {
			expectedPersonIds.add(new PersonId(3 * 1 + 5));
		}
		PeoplePluginData.Builder builder = PeoplePluginData.builder();
		for (PersonId personId : expectedPersonIds) {
			builder.addPersonId(personId);
		}

		peoplePluginData = builder.build();
		Set<PersonId> actualPersonIds = new LinkedHashSet<>();
		for (PersonId personId : peoplePluginData.getPersonIds()) {
			if (personId != null) {
				actualPersonIds.add(personId);
			}
		}
		assertEquals(expectedPersonIds, actualPersonIds);

	}

	@Test
	@UnitTestMethod(target = PeoplePluginData.class, name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {

		Set<PersonId> expectedPersonIds = new LinkedHashSet<>();
		for (int i = 0; i < 10; i++) {
			expectedPersonIds.add(new PersonId(3 * 1 + 5));
		}
		PeoplePluginData.Builder builder = PeoplePluginData.builder();
		for (PersonId personId : expectedPersonIds) {
			builder.addPersonId(personId);
		}

		PeoplePluginData peoplePluginData = builder.build();
		PeoplePluginData peoplePluginData2 = (PeoplePluginData) peoplePluginData.getCloneBuilder().build();

		Set<PersonId> actualPersonIds = new LinkedHashSet<>();
		for (PersonId personId : peoplePluginData2.getPersonIds()) {
			if (personId != null) {
				actualPersonIds.add(personId);
			}
		}
		assertEquals(expectedPersonIds, actualPersonIds);
	}

}
