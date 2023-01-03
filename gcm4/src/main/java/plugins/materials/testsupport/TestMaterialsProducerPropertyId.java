package plugins.materials.testsupport;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.TimeTrackingPolicy;

/**
 * A test support enumeration that contains a variety of materials producer
 * property id values with corresponding property definitions. Supports random
 * selection, random property value generation and generation of unique, unknown
 * batch property ids.
 * 
 *
 */
public enum TestMaterialsProducerPropertyId implements MaterialsProducerPropertyId {

	MATERIALS_PRODUCER_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK(

			PropertyDefinition	.builder()//
								.setType(Boolean.class)//
								.setDefaultValue(false)//
								.setPropertyValueMutability(true)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
								.build()), //
	MATERIALS_PRODUCER_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK(

			PropertyDefinition	.builder()//
								.setType(Integer.class)//
								//.setDefaultValue(0)//no default value
								.setPropertyValueMutability(true)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
								.build() //
	), //
	MATERIALS_PRODUCER_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK(

			PropertyDefinition	.builder()//
								.setType(Double.class)//
								.setDefaultValue(0.0)//
								.setPropertyValueMutability(true)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
								.build() //
	), //
	MATERIALS_PRODUCER_PROPERTY_4_BOOLEAN_MUTABLE_TRACK(

			PropertyDefinition	.builder()//
								.setType(Boolean.class)//
								.setDefaultValue(false)//
								.setPropertyValueMutability(true)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)//
								.build() //
	), //
	MATERIALS_PRODUCER_PROPERTY_5_INTEGER_MUTABLE_TRACK(

			PropertyDefinition	.builder()//
								.setType(Integer.class)//
								.setDefaultValue(0)//
								.setPropertyValueMutability(true)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)//
								.build() //
	), //
	MATERIALS_PRODUCER_PROPERTY_6_DOUBLE_MUTABLE_TRACK(

			PropertyDefinition	.builder()//
								.setType(Double.class)//
								.setDefaultValue(0.0)//
								.setPropertyValueMutability(true)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)//
								.build() //
	), //
	MATERIALS_PRODUCER_PROPERTY_7_BOOLEAN_IMMUTABLE_NO_TRACK(

			PropertyDefinition	.builder()//
								.setType(Boolean.class)//
								.setDefaultValue(false)//
								.setPropertyValueMutability(false)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
								.build() //
	), //
	MATERIALS_PRODUCER_PROPERTY_8_INTEGER_IMMUTABLE_NO_TRACK(

			PropertyDefinition	.builder()//
								.setType(Integer.class)//
								.setDefaultValue(0)//
								.setPropertyValueMutability(false)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
								.build() //
	), //
	MATERIALS_PRODUCER_PROPERTY_9_DOUBLE_IMMUTABLE_NO_TRACK(

			PropertyDefinition	.builder()//
								.setType(Double.class)//
								.setDefaultValue(0.0)//
								.setPropertyValueMutability(false)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
								.build() //
	);//

	private final PropertyDefinition propertyDefinition;

	/**
	 * Returns the property definition associated with this member. The property
	 * definition will contain a default value.
	 */
	public PropertyDefinition getPropertyDefinition() {
		return propertyDefinition;
	}

	private TestMaterialsProducerPropertyId(PropertyDefinition propertyDefinition) {
		this.propertyDefinition = propertyDefinition;
	}

	/**
	 * Returns a unique MaterialsProducerPropertyId instance that is not a
	 * member of this enumeration
	 */
	public static MaterialsProducerPropertyId getUnknownMaterialsProducerPropertyId() {
		return new MaterialsProducerPropertyId() {
		};
	}

	/**
	 * Returns a randomly selected value that is compatible with this member's
	 * associated property definition.
	 * 
	 */
	public Object getRandomPropertyValue(final RandomGenerator randomGenerator) {
		switch (this) {
		case MATERIALS_PRODUCER_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK:
			return randomGenerator.nextBoolean();
		case MATERIALS_PRODUCER_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK:
			return randomGenerator.nextInt();
		case MATERIALS_PRODUCER_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK:
			return randomGenerator.nextDouble();
		case MATERIALS_PRODUCER_PROPERTY_4_BOOLEAN_MUTABLE_TRACK:
			return randomGenerator.nextBoolean();
		case MATERIALS_PRODUCER_PROPERTY_5_INTEGER_MUTABLE_TRACK:
			return randomGenerator.nextInt();
		case MATERIALS_PRODUCER_PROPERTY_6_DOUBLE_MUTABLE_TRACK:
			return randomGenerator.nextDouble();
		case MATERIALS_PRODUCER_PROPERTY_7_BOOLEAN_IMMUTABLE_NO_TRACK:
			return randomGenerator.nextBoolean();
		case MATERIALS_PRODUCER_PROPERTY_8_INTEGER_IMMUTABLE_NO_TRACK:
			return randomGenerator.nextInt();
		case MATERIALS_PRODUCER_PROPERTY_9_DOUBLE_IMMUTABLE_NO_TRACK:
			return randomGenerator.nextDouble();
		default:
			
			throw new RuntimeException("unhandled case: " + this);

		}
	}

	/**
	 * Returns a randomly selected member of this enumeration.
	 */
	public static TestMaterialsProducerPropertyId getRandomMaterialsProducerPropertyId(final RandomGenerator randomGenerator) {
		return TestMaterialsProducerPropertyId.values()[randomGenerator.nextInt(TestMaterialsProducerPropertyId.values().length)];
	}

	/**
	 * Returns a randomly selected member of this enumeration whose associated
	 * property definition is marked as mutable.
	 */
	public static TestMaterialsProducerPropertyId getRandomMutableMaterialsProducerPropertyId(final RandomGenerator randomGenerator) {
		return TestMaterialsProducerPropertyId.values()[randomGenerator.nextInt(6)];
	}

	/**
	 * Returns the number of members in this enumeration.
	 */
	public static int size() {
		return TestMaterialsProducerPropertyId.values().length;
	}
	
	/**
	 * Returns the test property ids associated with a default value
	 */
	public static List<TestMaterialsProducerPropertyId> getPropertiesWithDefaultValues(){
		List<TestMaterialsProducerPropertyId> result = new ArrayList<>();
		
		for(TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
			if(testMaterialsProducerPropertyId.getPropertyDefinition().getDefaultValue().isPresent()) {
				result.add(testMaterialsProducerPropertyId);
			}
		}
		
		return result;
	}
	
	/**
	 * Returns the test property ids associated without a default value
	 */
	public static List<TestMaterialsProducerPropertyId> getPropertiesWithoutDefaultValues(){
		List<TestMaterialsProducerPropertyId> result = new ArrayList<>();
		
		for(TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
			if(testMaterialsProducerPropertyId.getPropertyDefinition().getDefaultValue().isEmpty()) {
				result.add(testMaterialsProducerPropertyId);
			}
		}
		
		return result;
	}
}
