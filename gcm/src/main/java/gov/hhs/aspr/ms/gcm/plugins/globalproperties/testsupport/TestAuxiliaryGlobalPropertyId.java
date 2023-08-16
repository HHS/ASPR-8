package gov.hhs.aspr.ms.gcm.plugins.globalproperties.testsupport;

import org.apache.commons.math3.random.RandomGenerator;

import gov.hhs.aspr.ms.gcm.plugins.globalproperties.support.GlobalPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyDefinition;

/**
 * Enumeration that provides a variety of global property definitions for
 * testing purposes
 */
public enum TestAuxiliaryGlobalPropertyId implements GlobalPropertyId {

	GLOBAL_AUX_PROPERTY_1_BOOLEAN_MUTABLE(
			PropertyDefinition.builder()//
					.setType(Boolean.class)//
					.setDefaultValue(false)//
					.setPropertyValueMutability(true)//
					.build() //
	), //
	GLOBAL_AUX_PROPERTY_2_INTEGER_MUTABLE(
			PropertyDefinition.builder()//
					.setType(Integer.class)//
					.setDefaultValue(0)//
					.setPropertyValueMutability(true)//
					.build() //
	), //
	GLOBAL_AUX_PROPERTY_3_DOUBLE_MUTABLE(
			PropertyDefinition.builder()//
					.setType(Double.class)//
					.setDefaultValue(0.0)//
					.setPropertyValueMutability(true)//
					.build() //
	), //
	GLOBAL_AUX_PROPERTY_4_BOOLEAN_IMMUTABLE(
			PropertyDefinition.builder()//
					.setType(Boolean.class)//
					.setDefaultValue(false)//
					.setPropertyValueMutability(false)//
					.build() //
	), //
	GLOBAL_AUX_PROPERTY_5_INTEGER_IMMUTABLE(
			PropertyDefinition.builder()//
					.setType(Integer.class)//
					.setDefaultValue(0)//
					.setPropertyValueMutability(false)//
					.build() //
	), //
	GLOBAL_AUX_PROPERTY_6_DOUBLE_IMMUTABLE(
			PropertyDefinition.builder()//
					.setType(Double.class)//
					.setDefaultValue(0.0)//
					.setPropertyValueMutability(false)//
					.build() //
	);//

	/**
	 * Returns a randomly selected member of this enumeration
	 */
	public static TestAuxiliaryGlobalPropertyId getRandomGlobalPropertyId(final RandomGenerator randomGenerator) {
		return TestAuxiliaryGlobalPropertyId.values()[randomGenerator
				.nextInt(TestAuxiliaryGlobalPropertyId.values().length)];
	}

	/**
	 * Returns a randomly selected value that is compatible with this member's
	 * associated property definition.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getRandomPropertyValue(final RandomGenerator randomGenerator) {
		switch (this) {
			case GLOBAL_AUX_PROPERTY_1_BOOLEAN_MUTABLE:
				Boolean b1 = randomGenerator.nextBoolean();
				return (T) b1;
			case GLOBAL_AUX_PROPERTY_2_INTEGER_MUTABLE:
				Integer i2 = randomGenerator.nextInt();
				return (T) i2;
			case GLOBAL_AUX_PROPERTY_3_DOUBLE_MUTABLE:
				Double d3 = randomGenerator.nextDouble();
				return (T) d3;
			case GLOBAL_AUX_PROPERTY_4_BOOLEAN_IMMUTABLE:
				Boolean b4 = randomGenerator.nextBoolean();
				return (T) b4;
			case GLOBAL_AUX_PROPERTY_5_INTEGER_IMMUTABLE:
				Integer i5 = randomGenerator.nextInt();
				return (T) i5;
			case GLOBAL_AUX_PROPERTY_6_DOUBLE_IMMUTABLE:
				Double d6 = randomGenerator.nextDouble();
				return (T) d6;
			default:
				throw new RuntimeException("unhandled case: " + this);

		}
	}

	private final PropertyDefinition propertyDefinition;

	private TestAuxiliaryGlobalPropertyId(PropertyDefinition propertyDefinition) {
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

}
