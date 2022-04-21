package plugins.regions.testsupport;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import plugins.regions.support.RegionPropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.TimeTrackingPolicy;

/**
 * Enumeration that identifies region property definitions
 */
public enum TestRegionPropertyId implements RegionPropertyId {
	REGION_PROPERTY_1_BOOLEAN_MUTABLE(
			PropertyDefinition	.builder()//
								.setType(Boolean.class)//
								.setDefaultValue(false)//
								.setPropertyValueMutability(true)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)//
								.build()), //
	REGION_PROPERTY_2_INTEGER_MUTABLE(
			PropertyDefinition	.builder()//
								.setType(Integer.class)//
								.setDefaultValue(0)//
								.setPropertyValueMutability(true)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)//
								.build()//
	), //
	REGION_PROPERTY_3_DOUBLE_MUTABLE(
			PropertyDefinition	.builder()//
								.setType(Double.class)//
								.setDefaultValue(0.0)//
								.setPropertyValueMutability(true)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)//
								.build()//
	), //	
	REGION_PROPERTY_4_BOOLEAN_IMMUTABLE(
			PropertyDefinition	.builder()//
								.setType(Boolean.class)//
								.setDefaultValue(false)//
								.setPropertyValueMutability(false)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)//
								.build()//
	), //
	REGION_PROPERTY_5_INTEGER_IMMUTABLE(
			PropertyDefinition	.builder()//
								.setType(Integer.class)//
								.setDefaultValue(0)//
								.setPropertyValueMutability(false)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)//
								.build()//
	), //
	REGION_PROPERTY_6_DOUBLE_IMMUTABLE(
			PropertyDefinition	.builder()//
								.setType(Double.class)//
								.setDefaultValue(0.0)//
								.setPropertyValueMutability(false)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)//
								.build()//
	), //
	
	;

	private final PropertyDefinition propertyDefinition;

	private TestRegionPropertyId(PropertyDefinition propertyDefinition) {
		this.propertyDefinition = propertyDefinition;
	}

	public PropertyDefinition getPropertyDefinition() {
		return propertyDefinition;
	}

	/**
	 * Returns a randomly selected member of this enumeration
	 */
	public static TestRegionPropertyId getRandomRegionPropertyId(final RandomGenerator randomGenerator) {
		return TestRegionPropertyId.values()[randomGenerator.nextInt(TestRegionPropertyId.values().length)];
	}
	
	public static TestRegionPropertyId getRandomMutableRegionPropertyId(final RandomGenerator randomGenerator) {
		List<TestRegionPropertyId> candidates = new ArrayList<>();
		for(TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			if(testRegionPropertyId.getPropertyDefinition().propertyValuesAreMutable()) {
				candidates.add(testRegionPropertyId);
			}
		}
		return candidates.get(randomGenerator.nextInt(candidates.size()));
	}

	/**
	 * Return the size of this enum
	 */
	public static int size() {
		return values().length;
	}

	/**
	 * Returns a new {@link RegionPropertyId} instance.
	 */
	public static RegionPropertyId getUnknownRegionPropertyId() {
		return new RegionPropertyId() {
		};
	}
	
	/**
	 * Returns a randomly selected value that is compatible with this member's
	 * associated property definition.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <T> T getRandomPropertyValue(final RandomGenerator randomGenerator) {
		switch (this) {
		case REGION_PROPERTY_1_BOOLEAN_MUTABLE:
			Boolean b1 = randomGenerator.nextBoolean();
			return (T) b1;
		case REGION_PROPERTY_2_INTEGER_MUTABLE:
			Integer i2 = randomGenerator.nextInt();
			return (T) i2;
		case REGION_PROPERTY_3_DOUBLE_MUTABLE:
			Double d3 = randomGenerator.nextDouble();
			return (T) d3;
		case REGION_PROPERTY_4_BOOLEAN_IMMUTABLE:
			Boolean b4 = randomGenerator.nextBoolean();
			return (T) b4;
		case REGION_PROPERTY_5_INTEGER_IMMUTABLE:
			Integer i5 = randomGenerator.nextInt();
			return (T) i5;
		case REGION_PROPERTY_6_DOUBLE_IMMUTABLE:
			Double d6 = randomGenerator.nextDouble();
			return (T) d6;
		default:
			throw new RuntimeException("unhandled case: " + this);

		}
	}

}