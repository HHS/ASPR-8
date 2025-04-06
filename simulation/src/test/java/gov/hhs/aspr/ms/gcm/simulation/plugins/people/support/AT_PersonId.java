package gov.hhs.aspr.ms.gcm.simulation.plugins.people.support;

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


public final class AT_PersonId {

	@Test
	@UnitTestConstructor(target = PersonId.class,args = { int.class })
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
	@UnitTestMethod(target = PersonId.class,name = "compareTo", args = { PersonId.class })
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
	@UnitTestMethod(target = PersonId.class,name = "equals", args = { Object.class })
	public void testEquals() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8980821493937106496L);
        
        // never equal to another type
		for (int i = 0; i < 30; i++) {
            PersonId personId = getRandomPersonId(randomGenerator.nextLong());
            assertFalse(personId.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			PersonId personId = getRandomPersonId(randomGenerator.nextLong());
			assertFalse(personId.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			PersonId personId = getRandomPersonId(randomGenerator.nextLong());
			assertTrue(personId.equals(personId));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			PersonId personId1 = getRandomPersonId(seed);
			PersonId personId2 = getRandomPersonId(seed);
			assertFalse(personId1 == personId2);
			for (int j = 0; j < 10; j++) {				
				assertTrue(personId1.equals(personId2));
				assertTrue(personId2.equals(personId1));
			}
		}

		// different inputs yield unequal PersonIds
		Set<PersonId> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			PersonId personId = getRandomPersonId(randomGenerator.nextLong());
			set.add(personId);
		}
		assertEquals(100, set.size());
	}

	@Test
	@UnitTestMethod(target = PersonId.class,name = "getValue", args = {})
	public void testGetValue() {
		for (int i = 0; i < 10; i++) {
			PersonId person = new PersonId(i);
			assertEquals(i, person.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = PersonId.class,name = "hashCode", args = {})
	public void testHashCode() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1948930019926275913L);

        // equal objects have equal hash codes
        for (int i = 0; i < 30; i++) {
            long seed = randomGenerator.nextLong();
			PersonId personId1 = getRandomPersonId(seed);
			PersonId personId2 = getRandomPersonId(seed);

            assertEquals(personId1, personId2);
            assertEquals(personId1.hashCode(), personId2.hashCode());
        }

        // hash codes are reasonably distributed
        Set<Integer> hashCodes = new LinkedHashSet<>();
        for (int i = 0; i < 100; i++) {
            PersonId personId = getRandomPersonId(randomGenerator.nextLong());
            hashCodes.add(personId.hashCode());
        }

        assertEquals(100, hashCodes.size());
	}

	@Test
	@UnitTestMethod(target = PersonId.class,name = "toString", args = {})
	public void testToString() {
		for (int i = 0; i < 10; i++) {
			PersonId person = new PersonId(i);
			assertEquals(Integer.toString(i), person.toString());
		}
	}

	private PersonId getRandomPersonId(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		return new PersonId(randomGenerator.nextInt(Integer.MAX_VALUE));
	}
}
