package gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.testsupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.random.RandomGenerator;

import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.support.GlobalPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyDefinition;

/**
 * Enumeration that provides a variety of global property definitions for
 * testing purposes
 */
public enum TestGlobalPropertyId implements GlobalPropertyId {

	GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE(PropertyDefinition.builder()//
			.setType(Boolean.class)//
			.setDefaultValue(false)//
			.setPropertyValueMutability(true)//
			.build() //
	), //
	GLOBAL_PROPERTY_2_INTEGER_MUTABLE(PropertyDefinition.builder()//
			.setType(Integer.class)//
			.setDefaultValue(0)//
			.setPropertyValueMutability(true)//
			.build() //
	), //
	GLOBAL_PROPERTY_3_DOUBLE_MUTABLE(PropertyDefinition.builder()//
			.setType(Double.class)//
			.setPropertyValueMutability(true)//
			.build() //
	), //
	GLOBAL_PROPERTY_4_BOOLEAN_IMMUTABLE(PropertyDefinition.builder()//
			.setType(Boolean.class)//
			.setDefaultValue(false)//
			.setPropertyValueMutability(false)//
			.build() //
	), //
	GLOBAL_PROPERTY_5_INTEGER_IMMUTABLE(PropertyDefinition.builder()//
			.setType(Integer.class)//
			.setDefaultValue(0)//
			.setPropertyValueMutability(false)//
			.build() //
	), //
	GLOBAL_PROPERTY_6_DOUBLE_IMMUTABLE(PropertyDefinition.builder()//
			.setType(Double.class)//
			.setDefaultValue(0.0)//
			.setPropertyValueMutability(false)//
			.build() //
	);//

	/**
	 * Returns a randomly selected member of this enumeration
	 */
	public static TestGlobalPropertyId getRandomGlobalPropertyId(final RandomGenerator randomGenerator) {
		return TestGlobalPropertyId.values()[randomGenerator.nextInt(TestGlobalPropertyId.values().length)];
	}

	public static TestGlobalPropertyId getRandomMutableGlobalPropertyId(final RandomGenerator randomGenerator) {
		List<TestGlobalPropertyId> candidates = new ArrayList<>();
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			if (testGlobalPropertyId.getPropertyDefinition().propertyValuesAreMutable()) {
				candidates.add(testGlobalPropertyId);
			}
		}
		return candidates.get(randomGenerator.nextInt(candidates.size()));
	}

	/**
	 * Returns a randomly selected value that is compatible with this member's
	 * associated property definition.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getRandomPropertyValue(final RandomGenerator randomGenerator) {
		switch (this) {
		case GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE:
			Boolean b1 = randomGenerator.nextBoolean();
			return (T) b1;
		case GLOBAL_PROPERTY_2_INTEGER_MUTABLE:
			Integer i2 = randomGenerator.nextInt();
			return (T) i2;
		case GLOBAL_PROPERTY_3_DOUBLE_MUTABLE:
			Double d3 = randomGenerator.nextDouble();
			return (T) d3;
		case GLOBAL_PROPERTY_4_BOOLEAN_IMMUTABLE:
			Boolean b4 = randomGenerator.nextBoolean();
			return (T) b4;
		case GLOBAL_PROPERTY_5_INTEGER_IMMUTABLE:
			Integer i5 = randomGenerator.nextInt();
			return (T) i5;
		case GLOBAL_PROPERTY_6_DOUBLE_IMMUTABLE:
			Double d6 = randomGenerator.nextDouble();
			return (T) d6;
		default:
			throw new RuntimeException("unhandled case: " + this);

		}
	}

	private final PropertyDefinition propertyDefinition;

	private TestGlobalPropertyId(PropertyDefinition propertyDefinition) {
		this.propertyDefinition = propertyDefinition;
	}

	public PropertyDefinition getPropertyDefinition() {
		return propertyDefinition;
	}

	/**
	 * Returns a new {@link GlobalPropertyId} instance.
	 */
	public static GlobalPropertyId getUnknownGlobalPropertyId() {
		return new GlobalPropertyId() {
		};
	}

	public static List<TestGlobalPropertyId> getGlobalPropertyIds() {
		return Arrays.asList(TestGlobalPropertyId.values());
	}

	public static List<TestGlobalPropertyId> getShuffledGlobalPropertyIds(RandomGenerator randomGenerator) {
		List<TestGlobalPropertyId> result = getGlobalPropertyIds();
		Random random = new Random(randomGenerator.nextLong());
		Collections.shuffle(result, random);
		return result;
	}

}
