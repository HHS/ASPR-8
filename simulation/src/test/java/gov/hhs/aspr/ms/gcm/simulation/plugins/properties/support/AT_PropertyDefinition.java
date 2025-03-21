package gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_PropertyDefinition {
	private static enum BooleanType {
		TRUE(Boolean.TRUE), FALSE(Boolean.FALSE);

		private final Boolean value;

		private BooleanType(Boolean value) {
			this.value = value;
		}

		public Boolean value() {
			return value;
		}
	}

	private static final int TEST_COUNT = 1000;

	/*
	 * Generates a random property definition from the given long seed.
	 * We reduce the odds of returning a boolean property definition
	 * because boolean property definitions only have four unique 
	 * combinations.
	 */
	private PropertyDefinition generateRandomPropertyDefinition(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		Class<?> type;
		int randomInt = randomGenerator.nextInt(100);
		Object defaultValue;

		int typeCase;
		if (randomInt < 24) typeCase = 0;
		else if (randomInt < 48) typeCase = 1;
		else if (randomInt < 73) typeCase = 2;
		else if (randomInt < 98) typeCase = 3;
		else typeCase = -1;

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

		boolean propertyValuesAreMutability = randomGenerator.nextBoolean();

		return PropertyDefinition	.builder()//
									.setType(type)//
									.setDefaultValue(defaultValue)//
									.setPropertyValueMutability(propertyValuesAreMutability)//									
									.build();//

	}

	@Test
	@UnitTestMethod(target = PropertyDefinition.class, name = "toString", args = {})
	public void testToString() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2790643065916150473L);

		/*
		 * Show that the toString returns a non-empty string. This is an
		 * otherwise boiler-plate implementation.
		 */
		for (int i = 0; i < TEST_COUNT; i++) {
			PropertyDefinition propertyDefinition = generateRandomPropertyDefinition(randomGenerator.nextLong());
			String toString = propertyDefinition.toString();
			assertNotNull(toString);
			assertTrue(toString.length() > 0);
		}
	}

	@Test
	@UnitTestMethod(target = PropertyDefinition.class, name = "getDefaultValue", args = {})
	public void testGetDefaultValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5344086660090478893L);

		// Show that a property definition that has a null default value (value
		// is set to null or was not set at all)
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Boolean.class)//
																	.build();//
		assertFalse(propertyDefinition.getDefaultValue().isPresent());

		/*
		 * Show that the default value used to form the property definition is
		 * returned by the property definition
		 */
		for (int i = 0; i < TEST_COUNT; i++) {
			Boolean defaultValue = randomGenerator.nextBoolean();
			propertyDefinition = PropertyDefinition	.builder()//
													.setType(Boolean.class)//
													.setDefaultValue(defaultValue)//
													.build();//
			assertTrue(propertyDefinition.getDefaultValue().isPresent());
			assertEquals(defaultValue, propertyDefinition.getDefaultValue().get());

		}
		for (int i = 0; i < TEST_COUNT; i++) {
			String defaultValue = "String " + randomGenerator.nextInt();
			propertyDefinition = PropertyDefinition.builder().setType(String.class).setDefaultValue(defaultValue).build();
			assertTrue(propertyDefinition.getDefaultValue().isPresent());
			assertEquals(defaultValue, propertyDefinition.getDefaultValue().get());
		}
		for (int i = 0; i < TEST_COUNT; i++) {
			Integer defaultValue = randomGenerator.nextInt();
			propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(defaultValue).build();
			assertTrue(propertyDefinition.getDefaultValue().isPresent());
			assertEquals(defaultValue, propertyDefinition.getDefaultValue().get());
		}
		for (int i = 0; i < TEST_COUNT; i++) {
			Double defaultValue = randomGenerator.nextDouble();
			propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(defaultValue).build();
			assertTrue(propertyDefinition.getDefaultValue().isPresent());
			assertEquals(defaultValue, propertyDefinition.getDefaultValue().get());
		}
		for (int i = 0; i < TEST_COUNT; i++) {
			Long defaultValue = randomGenerator.nextLong();
			propertyDefinition = PropertyDefinition.builder().setType(Long.class).setDefaultValue(defaultValue).build();
			assertTrue(propertyDefinition.getDefaultValue().isPresent());
			assertEquals(defaultValue, propertyDefinition.getDefaultValue().get());
		}

	}


	@Test
	@UnitTestMethod(target = PropertyDefinition.class, name = "getType", args = {})
	public void testGetType() {

		/*
		 * Show that the class type value used to form the property definition
		 * is returned by the property definition
		 */

		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(String.class)//
																	.setDefaultValue("default value")//
																	.build();//
		assertEquals(String.class, propertyDefinition.getType());

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Double.class)//
												.setDefaultValue(5.6)//
												.build();//
		assertEquals(Double.class, propertyDefinition.getType());

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Boolean.class)//
												.setDefaultValue(false)//
												.build();//
		assertEquals(Boolean.class, propertyDefinition.getType());

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Long.class)//
												.setDefaultValue(3453453L)//
												.build();//
		assertEquals(Long.class, propertyDefinition.getType());

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Integer.class)//
												.setDefaultValue(2234)//
												.build();//
		assertEquals(Integer.class, propertyDefinition.getType());

	}

	@Test
	@UnitTestMethod(target = PropertyDefinition.class, name = "propertyValuesAreMutable", args = {})
	public void testPropertyValuesAreMutable() {

		/*
		 * Show that the hasConstantPropertyValues value used to form the
		 * property definition is returned by the property definition
		 */

		for (BooleanType booleanType : BooleanType.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																		.setType(String.class)//
																		.setDefaultValue("default value")//
																		.setPropertyValueMutability(booleanType.value())//
																		.build();//
			assertEquals(booleanType.value(), propertyDefinition.propertyValuesAreMutable());

			propertyDefinition = PropertyDefinition	.builder()//
													.setType(Double.class)//
													.setDefaultValue(5.6)//
													.setPropertyValueMutability(booleanType.value())//
													.build();//
			assertEquals(booleanType.value(), propertyDefinition.propertyValuesAreMutable());

			propertyDefinition = PropertyDefinition	.builder()//
													.setType(Boolean.class)//
													.setDefaultValue(false)//
													.setPropertyValueMutability(booleanType.value())//
													.build();//
			assertEquals(booleanType.value(), propertyDefinition.propertyValuesAreMutable());

			propertyDefinition = PropertyDefinition	.builder()//
													.setType(Long.class)//
													.setDefaultValue(3453453L)//
													.setPropertyValueMutability(booleanType.value())//
													.build();//

			assertEquals(booleanType.value(), propertyDefinition.propertyValuesAreMutable());

			propertyDefinition = PropertyDefinition	.builder()//
													.setType(Integer.class)//
													.setDefaultValue(2345)//
													.setPropertyValueMutability(booleanType.value())//
													.build();//
			assertEquals(booleanType.value(), propertyDefinition.propertyValuesAreMutable());
		}

	}

	@Test
	@UnitTestMethod(target = PropertyDefinition.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6457927419499025913L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			PropertyDefinition propertyDefinition = generateRandomPropertyDefinition(randomGenerator.nextLong());
			assertFalse(propertyDefinition.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			PropertyDefinition propertyDefinition = generateRandomPropertyDefinition(randomGenerator.nextLong());
			assertFalse(propertyDefinition.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			PropertyDefinition propertyDefinition = generateRandomPropertyDefinition(randomGenerator.nextLong());
			assertTrue(propertyDefinition.equals(propertyDefinition));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			PropertyDefinition propertyDefinition1 = generateRandomPropertyDefinition(seed);
			PropertyDefinition propertyDefinition2 = generateRandomPropertyDefinition(seed);
			assertFalse(propertyDefinition1 == propertyDefinition2);
			for (int j = 0; j < 10; j++) {				
				assertTrue(propertyDefinition1.equals(propertyDefinition2));
				assertTrue(propertyDefinition2.equals(propertyDefinition1));
			}
		}

		// different inputs yield unequal PropertyDefinitions
		Set<PropertyDefinition> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			PropertyDefinition propertyDefinition = generateRandomPropertyDefinition(randomGenerator.nextLong());
			set.add(propertyDefinition);
		}
		assertEquals(100, set.size());
	}

	@Test
	@UnitTestMethod(target = PropertyDefinition.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6456927419491275913L);
	
		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			PropertyDefinition propertyDefinition1 = generateRandomPropertyDefinition(seed);
			PropertyDefinition propertyDefinition2 = generateRandomPropertyDefinition(seed);

			assertEquals(propertyDefinition1, propertyDefinition2);
			assertEquals(propertyDefinition1.hashCode(), propertyDefinition2.hashCode());

		}

		// hash codes are reasonable distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			PropertyDefinition propertyDefinition = generateRandomPropertyDefinition(randomGenerator.nextLong());
			hashCodes.add(propertyDefinition.hashCode());
		}
		
		assertEquals(100, hashCodes.size());
	}

	@Test
	@UnitTestMethod(target = PropertyDefinition.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(PropertyDefinition.builder());
	}

	@Test
	@UnitTestMethod(target = PropertyDefinition.Builder.class, name = "build", args = {})
	public void testBuild() {

		// precondition checks only -- post condition checks are in the other
		// tests

		// if the class type of the definition is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PropertyDefinition.builder().setType(null).setDefaultValue("default value").build();
		});
		assertEquals(PropertyError.NULL_PROPERTY_TYPE, contractException.getErrorType());

		// if the default value is assigned and not compatible with the class
		// type
		contractException = assertThrows(ContractException.class, () -> {
			PropertyDefinition	.builder().setType(Integer.class).setDefaultValue("default value")//
								.build();
		});
		assertEquals(PropertyError.INCOMPATIBLE_DEFAULT_VALUE, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = PropertyDefinition.Builder.class, name = "setDefaultValue", args = { Object.class })
	public void testSetDefaultValue() {

		for (int i = 0; i < 10; i++) {
			PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																		.setType(Integer.class)//
																		.setDefaultValue(i)//
																		.build();//

			Optional<Object> optional = propertyDefinition.getDefaultValue();
			assertTrue(optional.isPresent());
			Object object = optional.get();
			assertEquals(Integer.class, object.getClass());
			Integer actualValue = (Integer) object;
			assertEquals(i, actualValue.intValue());
		}

		for (int i = 0; i < 10; i++) {
			double defaultValue = i * 123.2346;
			PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																		.setType(Double.class)//
																		.setDefaultValue(defaultValue)//
																		.build();//

			Optional<Object> optional = propertyDefinition.getDefaultValue();
			assertTrue(optional.isPresent());
			Object object = optional.get();
			assertEquals(Double.class, object.getClass());
			Double actualValue = (Double) object;
			assertEquals(defaultValue, actualValue.doubleValue());
		}

		// show that not setting the default yields an empty optional
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.build();//

		Optional<Object> optional = propertyDefinition.getDefaultValue();
		assertFalse(optional.isPresent());

	}

	@Test
	@UnitTestMethod(target = PropertyDefinition.Builder.class, name = "setPropertyValueMutability", args = { boolean.class })
	public void testSetPropertyValueMutability() {

		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(10)//
																	.setPropertyValueMutability(true)//
																	.build();//

		assertTrue(propertyDefinition.propertyValuesAreMutable());

		// show the default is true
		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Integer.class)//
												.setDefaultValue(10)//
												// .setPropertyValueMutability(true)//
												.build();//

		assertTrue(propertyDefinition.propertyValuesAreMutable());

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Integer.class)//
												.setDefaultValue(10)//
												.setPropertyValueMutability(false).build();//

		assertFalse(propertyDefinition.propertyValuesAreMutable());

	}

	@Test
	@UnitTestMethod(target = PropertyDefinition.Builder.class, name = "setType", args = { Class.class })
	public void testSetType() {

		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(10)//
																	.build();//

		assertEquals(Integer.class, propertyDefinition.getType());

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Double.class)//
												.setDefaultValue(10.5)//
												.build();//

		assertEquals(Double.class, propertyDefinition.getType());

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Boolean.class)//
												.setDefaultValue(true)//
												.build();//

		assertEquals(Boolean.class, propertyDefinition.getType());

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(String.class)//
												.setDefaultValue("value")//
												.build();//

		assertEquals(String.class, propertyDefinition.getType());

	}

}
