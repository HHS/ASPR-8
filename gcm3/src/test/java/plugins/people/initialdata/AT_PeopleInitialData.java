package plugins.people.initialdata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.DataView;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import util.ContractException;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = PeopleInitialData.class)
public final class AT_PeopleInitialData implements DataView {

	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(PeopleInitialData.builder());
	}

	@Test
	@UnitTestMethod(target = PeopleInitialData.Builder.class, name = "build", args = {})
	public void testBuild() {
		PeopleInitialData peopleInitialData = PeopleInitialData.builder().build();
		assertTrue(peopleInitialData.getPersonIds().isEmpty());
		
		Set<PersonId> expectedPersonIds = new LinkedHashSet<>();
		for(int i = 0;i<10;i++) {
			expectedPersonIds.add(new PersonId(3*1+5));
		}
		PeopleInitialData.Builder builder = PeopleInitialData.builder();
		for(PersonId personId : expectedPersonIds) {
			builder.addPersonId(personId);
		}
		
		peopleInitialData = builder.build();
		assertEquals(expectedPersonIds, peopleInitialData.getPersonIds());
		
	}

	@Test
	@UnitTestMethod(target = PeopleInitialData.Builder.class, name = "addPersonId", args = { PersonId.class })
	public void testAddPersonId() {
		PeopleInitialData peopleInitialData = PeopleInitialData.builder().build();
		assertTrue(peopleInitialData.getPersonIds().isEmpty());
		
		Set<PersonId> expectedPersonIds = new LinkedHashSet<>();
		for(int i = 0;i<10;i++) {
			expectedPersonIds.add(new PersonId(3*1+5));
		}
		PeopleInitialData.Builder builder = PeopleInitialData.builder();
		for(PersonId personId : expectedPersonIds) {
			builder.addPersonId(personId);
		}
		
		peopleInitialData = builder.build();
		assertEquals(expectedPersonIds, peopleInitialData.getPersonIds());

		//precondition tests
		builder.addPersonId(new PersonId(5));
		
		ContractException contractException = assertThrows(ContractException.class,()->builder.addPersonId(new PersonId(5)));
		assertEquals(PersonError.DUPLICATE_PERSON_ID, contractException.getErrorType());
		
		contractException = assertThrows(ContractException.class,()->builder.addPersonId(null));
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
		
	}

	@Test
	@UnitTestMethod(name = "getPersonIds", args = {})
	public void testGetPersonIds() {
		PeopleInitialData peopleInitialData = PeopleInitialData.builder().build();
		assertTrue(peopleInitialData.getPersonIds().isEmpty());
		
		Set<PersonId> expectedPersonIds = new LinkedHashSet<>();
		for(int i = 0;i<10;i++) {
			expectedPersonIds.add(new PersonId(3*1+5));
		}
		PeopleInitialData.Builder builder = PeopleInitialData.builder();
		for(PersonId personId : expectedPersonIds) {
			builder.addPersonId(personId);
		}
		
		peopleInitialData = builder.build();
		assertEquals(expectedPersonIds, peopleInitialData.getPersonIds());

	}

}
