package plugins.people.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;


@UnitTest(target = PersonId.class)
public final class AT_PersonId {

	@Test
	@UnitTestConstructor(args = { int.class })
	public void testConstructor() {
		for (int i = 0; i < 10; i++) {
			PersonId personId = new PersonId(i);
			assertEquals(i, personId.getValue());
		}
		
		//precondition test: if the id < 0		
		ContractException contractException = assertThrows(ContractException.class, ()->new PersonId(-1));
		assertEquals(PersonError.NEGATIVE_PERSON_ID, contractException.getErrorType());

		
	}

	@Test
	@UnitTestMethod(name = "compareTo", args = { PersonId.class })
	public void testCompareTo() {
		for (int i = 0; i < 10; i++) {
			PersonId personA = new PersonId(i);
			for (int j = 0; j < 10; j++) {
				PersonId personB = new PersonId(j);
				int comparisonValue = personA.compareTo(personB);
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
			PersonId personA = new PersonId(i);
			for (int j = 0; j < 10; j++) {
				PersonId personB = new PersonId(j);				
				if (i == j) {
					assertEquals(personA,personB);
				} else {
					assertNotEquals(personA,personB);
				}
			}
		}
	}

	@Test
	@UnitTestMethod(name = "getValue", args = {})
	public void testGetValue() {
		for (int i = 0; i < 10; i++) {
			PersonId person = new PersonId(i);
			assertEquals(i, person.getValue());
		}
	}

	@Test
	@UnitTestMethod(name = "hashCode", args = {})
	public void testHashCode() {
		for (int i = 0; i < 10; i++) {
			PersonId person = new PersonId(i);
			assertEquals(i, person.hashCode());
		}
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		for (int i = 0; i < 10; i++) {
			PersonId person = new PersonId(i);
			assertEquals(Integer.toString(i), person.toString());
		}
	}

}
