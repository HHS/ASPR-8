package plugins.personproperties.testsupport;

import org.apache.commons.math3.random.RandomGenerator;

import plugins.personproperties.support.PersonPropertyId;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.TimeTrackingPolicy;

/**
 * Enumeration that identifies person property definitions
 */
public enum TestPersonPropertyId implements PersonPropertyId {
	PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK(
			PropertyDefinition	.builder()//
								.setType(Boolean.class)//
								.setDefaultValue(false)//
								.setPropertyValueMutability(true)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
								.build()), //
	PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK(
			PropertyDefinition	.builder()//
								.setType(Integer.class)//
								.setDefaultValue(0)//
								.setPropertyValueMutability(true)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
								.build() //
	), //
	PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK(
			PropertyDefinition	.builder()//
								.setType(Double.class)//
								.setDefaultValue(0.0)//
								.setPropertyValueMutability(true)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
								.build() //
	), //
	PERSON_PROPERTY_4_BOOLEAN_MUTABLE_TRACK(
			PropertyDefinition	.builder()//
								.setType(Boolean.class)//
								.setDefaultValue(false)//
								.setPropertyValueMutability(true)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)//
								.build() //
	), //
	PERSON_PROPERTY_5_INTEGER_MUTABLE_TRACK(
			PropertyDefinition	.builder()//
								.setType(Integer.class)//
								.setDefaultValue(0)//
								.setPropertyValueMutability(true)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)//
								.build() //
	), //
	PERSON_PROPERTY_6_DOUBLE_MUTABLE_TRACK(
			PropertyDefinition	.builder()//
								.setType(Double.class)//
								.setDefaultValue(0.0)//
								.setPropertyValueMutability(true)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)//
								.build() //
	), //
	PERSON_PROPERTY_7_BOOLEAN_IMMUTABLE_NO_TRACK(
			PropertyDefinition	.builder()//
								.setType(Boolean.class)//
								.setDefaultValue(false)//
								.setPropertyValueMutability(false)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
								.build() //
	), //
	PERSON_PROPERTY_8_INTEGER_IMMUTABLE_NO_TRACK(
			PropertyDefinition	.builder()//
								.setType(Integer.class)//
								.setDefaultValue(0)//
								.setPropertyValueMutability(false)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
								.build() //
	), //
	PERSON_PROPERTY_9_DOUBLE_IMMUTABLE_NO_TRACK(
			PropertyDefinition	.builder()//
								.setType(Double.class)//
								.setDefaultValue(0.0)//
								.setPropertyValueMutability(false)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
								.build() //
	);//

	/**
	 * Returns a randomly selected member of this enumeration
	 */
	public static TestPersonPropertyId getRandomPersonPropertyId(final RandomGenerator randomGenerator) {
		return TestPersonPropertyId.values()[randomGenerator.nextInt(TestPersonPropertyId.values().length)];
	}

	/**
	 * Returns a randomly selected value that is compatible with this member's
	 * associated property definition.
	 * 
	 */
	public Object getRandomPropertyValue(final RandomGenerator randomGenerator) {
		switch (this) {
		case PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK:
			return randomGenerator.nextBoolean();
		case PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK:
			return randomGenerator.nextInt();
		case PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK:
			return randomGenerator.nextDouble();
		case PERSON_PROPERTY_4_BOOLEAN_MUTABLE_TRACK:
			return randomGenerator.nextBoolean();
		case PERSON_PROPERTY_5_INTEGER_MUTABLE_TRACK:
			return randomGenerator.nextInt();
		case PERSON_PROPERTY_6_DOUBLE_MUTABLE_TRACK:
			return randomGenerator.nextDouble();
		case PERSON_PROPERTY_7_BOOLEAN_IMMUTABLE_NO_TRACK:
			return randomGenerator.nextBoolean();
		case PERSON_PROPERTY_8_INTEGER_IMMUTABLE_NO_TRACK:
			return randomGenerator.nextInt();
		case PERSON_PROPERTY_9_DOUBLE_IMMUTABLE_NO_TRACK:
			return randomGenerator.nextDouble();
		default:
			throw new RuntimeException("unhandled case: " + this);

		}
	}

	private final PropertyDefinition propertyDefinition;

	private TestPersonPropertyId(PropertyDefinition propertyDefinition) {
		this.propertyDefinition = propertyDefinition;
	}

	public PropertyDefinition getPropertyDefinition() {
		return propertyDefinition;
	}

	/**
	 * Returns a new {@link PersonPropertyId} instance.
	 */
	public static PersonPropertyId getUnknownPersonPropertyId() {
		return new PersonPropertyId() {
		};
	}
}