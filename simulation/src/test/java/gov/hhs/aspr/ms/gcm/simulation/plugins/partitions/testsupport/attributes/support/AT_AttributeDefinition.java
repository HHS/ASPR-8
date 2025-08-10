package gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.attributes.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public final class AT_AttributeDefinition {

	@Test
	@UnitTestMethod(target = AttributeDefinition.class,name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(AttributeDefinition.builder());
	}

	@Test
	@UnitTestMethod(target = AttributeDefinition.Builder.class, name = "build", args = {})
	public void testBuild() {
		//precondition tests
		
		//if the class type of the definition is not assigned or null
		ContractException contractException = assertThrows(ContractException.class,()-> AttributeDefinition.builder().setDefaultValue(12).setType(null).build());
		assertEquals(AttributeError.NULL_ATTRIBUTE_TYPE, contractException.getErrorType());
		
		//if the default value null
		contractException = assertThrows(ContractException.class,()-> AttributeDefinition.builder().setDefaultValue(null).setType(Integer.class).build());
		assertEquals(AttributeError.NULL_DEFAULT_VALUE, contractException.getErrorType());

		//if the class type is not a super-type of the default value
		contractException = assertThrows(ContractException.class,()-> AttributeDefinition.builder().setDefaultValue("bad value").setType(Integer.class).build());
		assertEquals(AttributeError.INCOMPATIBLE_DEFAULT_VALUE, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = AttributeDefinition.Builder.class, name = "setType", args = { Class.class })
	public void testSetType() {
		AttributeDefinition attributeDefinition = AttributeDefinition.builder().setDefaultValue(12).setType(Integer.class).build();
		assertEquals(Integer.class, attributeDefinition.getType());
		
		attributeDefinition = AttributeDefinition.builder().setDefaultValue("value").setType(String.class).build();
		assertEquals(String.class, attributeDefinition.getType());
	}

	@Test
	@UnitTestMethod(target = AttributeDefinition.Builder.class, name = "setDefaultValue", args = { Object.class })
	public void testSetDefaultValue() {
		AttributeDefinition attributeDefinition = AttributeDefinition.builder().setDefaultValue(12).setType(Integer.class).build();
		assertEquals(12, attributeDefinition.getDefaultValue());
		
		attributeDefinition = AttributeDefinition.builder().setDefaultValue(13).setType(Integer.class).build();
		assertEquals(13, attributeDefinition.getDefaultValue());

	}

	@Test
	@UnitTestMethod(target = AttributeDefinition.class,name = "getDefaultValue", args = {})
	public void testGetDefaultValue() {
		AttributeDefinition attributeDefinition = AttributeDefinition.builder().setDefaultValue(12).setType(Integer.class).build();
		assertEquals(12, attributeDefinition.getDefaultValue());
		
		attributeDefinition = AttributeDefinition.builder().setDefaultValue(13).setType(Integer.class).build();
		assertEquals(13, attributeDefinition.getDefaultValue());
	}

	@Test
	@UnitTestMethod(target = AttributeDefinition.class,name = "getType", args = {})
	public void testGetType() {
		AttributeDefinition attributeDefinition = AttributeDefinition.builder().setDefaultValue(12).setType(Integer.class).build();
		assertEquals(Integer.class, attributeDefinition.getType());
		
		attributeDefinition = AttributeDefinition.builder().setDefaultValue("value").setType(String.class).build();
		assertEquals(String.class, attributeDefinition.getType());

	}

	@Test
	@UnitTestMethod(target = AttributeDefinition.class,name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2653401233475183354L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			AttributeDefinition attributeDefinition1 = getRandomAttributeDefinition(seed);
			AttributeDefinition attributeDefinition2 = getRandomAttributeDefinition(seed);

			assertEquals(attributeDefinition1, attributeDefinition2);
			assertEquals(attributeDefinition1.hashCode(), attributeDefinition2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			AttributeDefinition attributeDefinition = getRandomAttributeDefinition(randomGenerator.nextLong());
			hashCodes.add(attributeDefinition.hashCode());
		}

		assertEquals(100, hashCodes.size());
	}

	@Test
	@UnitTestMethod(target = AttributeDefinition.class,name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8980828788377306870L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			AttributeDefinition attributeDefinition = getRandomAttributeDefinition(randomGenerator.nextLong());
			assertFalse(attributeDefinition.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			AttributeDefinition attributeDefinition = getRandomAttributeDefinition(randomGenerator.nextLong());
			assertFalse(attributeDefinition.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			AttributeDefinition attributeDefinition = getRandomAttributeDefinition(randomGenerator.nextLong());
			assertTrue(attributeDefinition.equals(attributeDefinition));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			AttributeDefinition attributeDefinition1 = getRandomAttributeDefinition(seed);
			AttributeDefinition attributeDefinition2 = getRandomAttributeDefinition(seed);
			assertFalse(attributeDefinition1 == attributeDefinition2);
			for (int j = 0; j < 10; j++) {
				assertTrue(attributeDefinition1.equals(attributeDefinition2));
				assertTrue(attributeDefinition2.equals(attributeDefinition1));
			}
		}

		// different inputs yield unequal attributeDefinitions
		Set<AttributeDefinition> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			AttributeDefinition attributeDefinition = getRandomAttributeDefinition(randomGenerator.nextLong());
			set.add(attributeDefinition);
		}
		assertEquals(100, set.size());
	}

	@Test
	@UnitTestMethod(target = AttributeDefinition.class,name = "toString", args = {})
	public void testToString() {
		AttributeDefinition attributeDefinition = AttributeDefinition.builder().setDefaultValue(12).setType(Integer.class).build();
		String expectedValue = "AttributeDefinition [data=Data [type=class java.lang.Integer, defaultValue=12]]";
		assertEquals(expectedValue, attributeDefinition.toString());
	}

	/*
	 * Generates a random attribute definition from the given long seed.
	 * We reduce the odds of returning a boolean attribute definition
	 * because boolean attribute definitions only have two unique 
	 * combinations.
	 */
	private AttributeDefinition getRandomAttributeDefinition(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		
		int randomInt = randomGenerator.nextInt(100);
		int typeCase;
		if (randomInt < 24) typeCase = 0;
		else if (randomInt < 48) typeCase = 1;
		else if (randomInt < 73) typeCase = 2;
		else if (randomInt < 98) typeCase = 3;
		else typeCase = -1;

		Class<?> type;
		Object defaultValue;

		switch (typeCase) {
		case 0:
			type = Long.class;
			defaultValue = randomGenerator.nextLong();
			break;
		case 1:
			type = Integer.class;
			defaultValue = randomGenerator.nextInt();
			break;
		case 2:
			type = String.class;
			defaultValue = "String " + randomGenerator.nextInt();
			break;
		case 3:
			type = Double.class;
			defaultValue = randomGenerator.nextDouble();
			break;
		default:
			type = Boolean.class;
			defaultValue = randomGenerator.nextBoolean();
			break;
		}

		return AttributeDefinition.builder()//
				.setType(type)//
				.setDefaultValue(defaultValue)//
				.build();
	}
}