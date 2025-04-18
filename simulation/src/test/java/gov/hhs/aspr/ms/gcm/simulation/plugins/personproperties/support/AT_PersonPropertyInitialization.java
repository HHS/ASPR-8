package gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.testsupport.TestPersonPropertyId;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_PersonPropertyInitialization {

	@Test
	@UnitTestConstructor(target = PersonPropertyValueInitialization.class, args = { PersonPropertyId.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonPropertyValueInitialization.class, name = "getPersonPropertyId", args = {})
	public void testGetPersonPropertyId() {
		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;
		Double value = 2.7;
		PersonPropertyValueInitialization personPropertyValueInitialization = new PersonPropertyValueInitialization(personPropertyId, value);
		assertEquals(personPropertyId, personPropertyValueInitialization.getPersonPropertyId());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyValueInitialization.class, name = "getValue", args = {})
	public void testGetValue() {
		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;
		Double value = 2.7;
		PersonPropertyValueInitialization personPropertyValueInitialization = new PersonPropertyValueInitialization(personPropertyId, value);
		assertEquals(value, personPropertyValueInitialization.getValue());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyValueInitialization.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8980821413367306870L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			PersonPropertyValueInitialization valueInit = getRandomPersonPropertyValueInitialization(randomGenerator.nextLong());
			assertFalse(valueInit.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			PersonPropertyValueInitialization valueInit = getRandomPersonPropertyValueInitialization(randomGenerator.nextLong());
			assertFalse(valueInit.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			PersonPropertyValueInitialization valueInit = getRandomPersonPropertyValueInitialization(randomGenerator.nextLong());
			assertTrue(valueInit.equals(valueInit));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			PersonPropertyValueInitialization valueInit1 = getRandomPersonPropertyValueInitialization(seed);
			PersonPropertyValueInitialization valueInit2 = getRandomPersonPropertyValueInitialization(seed);
			assertFalse(valueInit1 == valueInit2);
			for (int j = 0; j < 10; j++) {
				assertTrue(valueInit1.equals(valueInit2));
				assertTrue(valueInit2.equals(valueInit1));
			}
		}

		// different inputs yield unequal personPropertyValueInitializations
		Set<PersonPropertyValueInitialization> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			PersonPropertyValueInitialization valueInit = getRandomPersonPropertyValueInitialization(randomGenerator.nextLong());
			set.add(valueInit);
		}
		assertEquals(100, set.size());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyValueInitialization.class, name = "toString", args = {})
	public void testToString() {
		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;
		Double value = 2.7;
		PersonPropertyValueInitialization personPropertyValueInitialization = new PersonPropertyValueInitialization(personPropertyId, value);
		String expectedString = "PersonPropertyAssignment [personPropertyId=PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, value=2.7]";

		assertEquals(expectedString, personPropertyValueInitialization.toString());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyValueInitialization.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2653491501425183354L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			PersonPropertyValueInitialization valueInit1 = getRandomPersonPropertyValueInitialization(seed);
			PersonPropertyValueInitialization valueInit2 = getRandomPersonPropertyValueInitialization(seed);

			assertEquals(valueInit1, valueInit2);
			assertEquals(valueInit1.hashCode(), valueInit2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			PersonPropertyValueInitialization valueInit = getRandomPersonPropertyValueInitialization(randomGenerator.nextLong());
			hashCodes.add(valueInit.hashCode());
		}

		assertEquals(100, hashCodes.size());
	}

	private PersonPropertyValueInitialization getRandomPersonPropertyValueInitialization(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		// We remove boolean TestAttributeIds to increase randomness
		List<TestPersonPropertyId> selectedValues = new ArrayList<>();
		TestPersonPropertyId[] allValues = TestPersonPropertyId.values();
		for (TestPersonPropertyId value : allValues) {
			if (value.getPropertyDefinition().getType() != Boolean.class) {
				selectedValues.add(value);
			}
		}

		TestPersonPropertyId testPersonPropertyId = selectedValues.get(randomGenerator.nextInt(selectedValues.size()));
		Object propertyValue = testPersonPropertyId.getRandomPropertyValue(randomGenerator);

		return new PersonPropertyValueInitialization(testPersonPropertyId, propertyValue);
	}
}
