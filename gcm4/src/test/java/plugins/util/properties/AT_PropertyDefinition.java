package plugins.util.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import plugins.util.properties.PropertyDefinition.Builder;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

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
	 * Generates a random property definition from the given Random instance
	 */
	private PropertyDefinition generateRandomPropertyDefinition(RandomGenerator randomGenerator) {
		Class<?> type;
		final int typeCase = randomGenerator.nextInt(5);
		Object defaultValue;
		switch (typeCase) {
		case 0:
			type = Boolean.class;
			defaultValue = randomGenerator.nextBoolean();
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
			type = Long.class;
			defaultValue = randomGenerator.nextLong();
			break;
		}

		boolean propertyValuesAreMutability = randomGenerator.nextBoolean();
		TimeTrackingPolicy timeTrackingPolicy = TimeTrackingPolicy.values()[randomGenerator.nextInt(TimeTrackingPolicy.values().length)];

		return PropertyDefinition	.builder()//
									.setType(type)//
									.setDefaultValue(defaultValue)//
									.setPropertyValueMutability(propertyValuesAreMutability)//
									.setTimeTrackingPolicy(timeTrackingPolicy)//
									.build();//

	}

	/*
	 * Generates a random property definition from the given Random instance
	 * that has at least one field value that does not match the given property
	 * definition.
	 */
	private PropertyDefinition generateNonMatchingRandomPropertyDefinition(PropertyDefinition propertyDefinition, RandomGenerator randomGenerator) {
		while (true) {
			PropertyDefinition result = generateRandomPropertyDefinition(randomGenerator);
			boolean different = result.propertyValuesAreMutable() != propertyDefinition.propertyValuesAreMutable();

			different |= propertyDefinition.getDefaultValue().isPresent() != result.getDefaultValue().isPresent();
			if (propertyDefinition.getDefaultValue().isPresent() && result.getDefaultValue().isPresent()) {
				different |= !result.getDefaultValue().get().equals(propertyDefinition.getDefaultValue().get());
			}

			different |= !result.getTimeTrackingPolicy().equals(propertyDefinition.getTimeTrackingPolicy());
			different |= !result.getType().equals(propertyDefinition.getType());
			if (different) {
				return result;
			}
		}
	}

	/*
	 * Generates a matching property definition created from the given property
	 * definition's field values
	 */
	private PropertyDefinition generateMatchingPropertyDefinition(PropertyDefinition propertyDefinition) {
		Builder builder = PropertyDefinition.builder();//
		if (propertyDefinition.getDefaultValue().isPresent()) {
			builder.setDefaultValue(propertyDefinition.getDefaultValue().get());//
		}

		return builder	.setType(propertyDefinition.getType())//
						.setPropertyValueMutability(propertyDefinition.propertyValuesAreMutable())//
						.setTimeTrackingPolicy(propertyDefinition.getTimeTrackingPolicy())//
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
			PropertyDefinition propertyDefinition = generateRandomPropertyDefinition(randomGenerator);
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
	@UnitTestMethod(target = PropertyDefinition.class, name = "getTimeTrackingPolicy", args = {})
	public void testGetTimeTrackingPolicy() {

		/*
		 * Show that the TimeTrackingPolicy value used to form the property
		 * definition is returned by the property definition
		 */
		for (TimeTrackingPolicy timeTrackingPolicy : TimeTrackingPolicy.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																		.setType(String.class)//
																		.setDefaultValue("defaultValue")//
																		.setTimeTrackingPolicy(timeTrackingPolicy)//
																		.build();//
			assertEquals(timeTrackingPolicy, propertyDefinition.getTimeTrackingPolicy());
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
		PropertyDefinition propertyDefinition1 = PropertyDefinition	.builder()//
																	.setType(String.class)//
																	.setDefaultValue("asdf")//
																	.setPropertyValueMutability(true)//
																	.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
																	.build();//

		PropertyDefinition propertyDefinition2 = PropertyDefinition	.builder()//
																	.setType(String.class)//
																	.setDefaultValue("asdf")//
																	.setPropertyValueMutability(true)//
																	.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
																	.build();//

		PropertyDefinition propertyDefinition3 = PropertyDefinition	.builder()//
																	.setType(String.class)//
																	.setDefaultValue("xxx")//
																	.setPropertyValueMutability(true)//
																	.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
																	.build();//

		PropertyDefinition propertyDefinition4 = PropertyDefinition	.builder()//
																	.setType(String.class)//
																	.setDefaultValue("asdf")//
																	.setPropertyValueMutability(false)//
																	.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
																	.build();//

		PropertyDefinition propertyDefinition5 = PropertyDefinition	.builder()//
																	.setType(String.class)//
																	.setDefaultValue("asdf")//
																	.setPropertyValueMutability(true)//
																	.setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)//
																	.build();//

		PropertyDefinition propertyDefinition6 = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(45)//
																	.setPropertyValueMutability(true)//
																	.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
																	.build();//

		assertEquals(propertyDefinition1, propertyDefinition1);
		assertEquals(propertyDefinition1, propertyDefinition2);
		assertEquals(propertyDefinition2, propertyDefinition1);

		assertNotEquals(propertyDefinition1, propertyDefinition3);
		assertNotEquals(propertyDefinition1, propertyDefinition4);
		assertNotEquals(propertyDefinition1, propertyDefinition5);
		assertNotEquals(propertyDefinition1, propertyDefinition6);

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3951851825163960855L);

		/*
		 * Show that two Property Definitions are equal if and only if their
		 * fields are equal
		 */
		for (int i = 0; i < TEST_COUNT; i++) {
			PropertyDefinition def1 = generateRandomPropertyDefinition(randomGenerator);
			PropertyDefinition def2 = generateMatchingPropertyDefinition(def1);
			assertEquals(def1, def2);
			PropertyDefinition def3 = generateNonMatchingRandomPropertyDefinition(def1, randomGenerator);
			assertNotEquals(def1, def3);
		}

		/*
		 * Show that a property definition is not equal to null
		 */
		for (int i = 0; i < TEST_COUNT; i++) {
			PropertyDefinition def = generateRandomPropertyDefinition(randomGenerator);
			assertFalse(def.equals(null));
		}

		/*
		 * Show that a property definition is equal to itself
		 */
		for (int i = 0; i < TEST_COUNT; i++) {
			PropertyDefinition def = generateRandomPropertyDefinition(randomGenerator);
			assertTrue(def.equals(def));
		}

		/*
		 * Show that a property definition is not equal to an instance of
		 * another class
		 */
		for (int i = 0; i < TEST_COUNT; i++) {
			PropertyDefinition def = generateRandomPropertyDefinition(randomGenerator);
			assertFalse(def.equals(new Object()));
		}

		/*
		 * Show that equal objects have equal hash codes
		 */
		for (int i = 0; i < TEST_COUNT; i++) {
			PropertyDefinition def1 = generateRandomPropertyDefinition(randomGenerator);
			PropertyDefinition def2 = generateMatchingPropertyDefinition(def1);
			assertEquals(def1.hashCode(), def2.hashCode());
		}

	}

	@Test
	@UnitTestMethod(target = PropertyDefinition.class, name = "hashCode", args = {})
	public void testHashCode() {
		PropertyDefinition propertyDefinition1 = PropertyDefinition	.builder()//
																	.setType(String.class)//
																	.setDefaultValue("asdf")//
																	.setPropertyValueMutability(true)//
																	.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
																	.build();//

		PropertyDefinition propertyDefinition2 = PropertyDefinition	.builder()//
																	.setType(String.class)//
																	.setDefaultValue("asdf")//
																	.setPropertyValueMutability(true)//
																	.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
																	.build();//

		assertEquals(propertyDefinition1.hashCode(), propertyDefinition2.hashCode());
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
	@UnitTestMethod(target = PropertyDefinition.Builder.class, name = "setTimeTrackingPolicy", args = { TimeTrackingPolicy.class })
	public void testSetTimeTrackingPolicy() {

		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(10)//
																	.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
																	.build();//

		assertEquals(TimeTrackingPolicy.DO_NOT_TRACK_TIME, propertyDefinition.getTimeTrackingPolicy());

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Integer.class)//
												.setDefaultValue(10)//
												.setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)//
												.build();//

		assertEquals(TimeTrackingPolicy.TRACK_TIME, propertyDefinition.getTimeTrackingPolicy());

		// show that the default is DO_NOT_TRACK_TIME
		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Integer.class)//
												.setDefaultValue(10)//
												// .setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
												.build();//

		assertEquals(TimeTrackingPolicy.DO_NOT_TRACK_TIME, propertyDefinition.getTimeTrackingPolicy());

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
