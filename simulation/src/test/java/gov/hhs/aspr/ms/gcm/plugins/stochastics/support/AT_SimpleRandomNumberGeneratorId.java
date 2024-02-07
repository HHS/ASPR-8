package gov.hhs.aspr.ms.gcm.plugins.stochastics.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;

public class AT_SimpleRandomNumberGeneratorId {
	@Test
	@UnitTestConstructor(target = SimpleRandomNumberGeneratorId.class, args = { Object.class })
	public void testSimpleRandomNumberGeneratorId() {

		// precondition test: if the value is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> new SimpleRandomNumberGeneratorId(null));
		assertEquals(StochasticsError.NULL_RANDOM_NUMBER_GENERATOR_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = SimpleRandomNumberGeneratorId.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// not equal to null
		assertFalse(new SimpleRandomNumberGeneratorId("A").equals(null));

		// reflexive
		for (int i = 0; i < 30; i++) {
			SimpleRandomNumberGeneratorId simpleRandomNumberGeneratorId = new SimpleRandomNumberGeneratorId(i);
			assertTrue(simpleRandomNumberGeneratorId.equals(simpleRandomNumberGeneratorId));
		}

		// symmetric, transitive and consistent
		for (int i = 0; i < 30; i++) {
			SimpleRandomNumberGeneratorId a = new SimpleRandomNumberGeneratorId(i);
			SimpleRandomNumberGeneratorId b = new SimpleRandomNumberGeneratorId(i);
			for (int j = 0; j < 10; j++) {
				assertTrue(a.equals(b));
				assertTrue(b.equals(a));
			}
		}

		// different inputs yield unequal SimpleRandomNumberGeneratorIds
		for (int i = 0; i < 30; i++) {
			SimpleRandomNumberGeneratorId a = new SimpleRandomNumberGeneratorId(i);
			SimpleRandomNumberGeneratorId b = new SimpleRandomNumberGeneratorId(i + 1);
			assertNotEquals(a, b);
		}
	}

	@Test
	@UnitTestMethod(target = SimpleRandomNumberGeneratorId.class, name = "getValue", args = {})
	public void testGetValue() {
		for (int i = 0; i < 30; i++) {
			Integer input = i;
			SimpleRandomNumberGeneratorId simpleRandomNumberGeneratorId = new SimpleRandomNumberGeneratorId(input);
			assertEquals(input, simpleRandomNumberGeneratorId.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = SimpleRandomNumberGeneratorId.class, name = "hashCode", args = {})
	public void testHashCode() {
		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			SimpleRandomNumberGeneratorId a = new SimpleRandomNumberGeneratorId(i);
			SimpleRandomNumberGeneratorId b = new SimpleRandomNumberGeneratorId(i);
			assertEquals(a, b);
			assertEquals(a.hashCode(), b.hashCode());
		}
		
		//hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			SimpleRandomNumberGeneratorId a = new SimpleRandomNumberGeneratorId(i);
			hashCodes.add(a.hashCode());
		}
		assertEquals(100, hashCodes.size());
		

	}

	@Test
	@UnitTestMethod(target = SimpleRandomNumberGeneratorId.class, name = "toString", args = {})
	public void testToString() {
		SimpleRandomNumberGeneratorId simpleRandomNumberGeneratorId = new SimpleRandomNumberGeneratorId("Value");
		
		String actualValue = simpleRandomNumberGeneratorId.toString();
		String expectedValue = "Value";
		
		assertEquals(expectedValue, actualValue);
	}
}
