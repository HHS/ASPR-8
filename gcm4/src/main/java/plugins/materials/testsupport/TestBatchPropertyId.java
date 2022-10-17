package plugins.materials.testsupport;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import plugins.materials.support.BatchPropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.TimeTrackingPolicy;

/**
 * A test support enumeration that contains a variety of batch property id
 * values with corresponding property definitions and associations to Test
 * Material Ids. Supports random selection, random property value generation and
 * generation of unique, unknown batch property ids.
 * 
 * @author Shawn Hatch
 *
 */
public enum TestBatchPropertyId implements BatchPropertyId {

	BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK(
			TestMaterialId.MATERIAL_1, //
			PropertyDefinition	.builder()//
								.setType(Boolean.class)//
								.setDefaultValue(false)//
								.setPropertyValueMutability(false)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
								.build()), //
	BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK(
			TestMaterialId.MATERIAL_1, //
			PropertyDefinition	.builder()//
								.setType(Integer.class)//
								//.setDefaultValue(0)//no default value
								.setPropertyValueMutability(true)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
								.build() //
	), //
	BATCH_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK(
			TestMaterialId.MATERIAL_1, //
			PropertyDefinition	.builder()//
								.setType(Double.class)//
								//.setDefaultValue(0.0)//no default
								.setPropertyValueMutability(true)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
								.build() //
	), //
	BATCH_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK(
			TestMaterialId.MATERIAL_2, //
			PropertyDefinition	.builder()//
								.setType(Boolean.class)//
								.setDefaultValue(false)//
								.setPropertyValueMutability(true)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)//
								.build() //
	), //
	BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK(
			TestMaterialId.MATERIAL_2, //
			PropertyDefinition	.builder()//
								.setType(Integer.class)//
								.setDefaultValue(0)//
								.setPropertyValueMutability(false)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)//
								.build() //
	), //
	BATCH_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK(
			TestMaterialId.MATERIAL_2, //
			PropertyDefinition	.builder()//
								.setType(Double.class)//
								.setDefaultValue(0.0)//
								.setPropertyValueMutability(true)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)//
								.build() //
	), //
	BATCH_PROPERTY_3_1_BOOLEAN_MUTABLE_NO_TRACK(
			TestMaterialId.MATERIAL_3, //
			PropertyDefinition	.builder()//
								.setType(Boolean.class)//
								.setDefaultValue(false)//
								.setPropertyValueMutability(true)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
								.build() //
	), //
	BATCH_PROPERTY_3_2_INTEGER_MUTABLE_NO_TRACK(
			TestMaterialId.MATERIAL_3, //
			PropertyDefinition	.builder()//
								.setType(Integer.class)//
								.setDefaultValue(0)//
								.setPropertyValueMutability(true)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
								.build() //
	), //
	BATCH_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK(
			TestMaterialId.MATERIAL_3, //
			PropertyDefinition	.builder()//
								.setType(Double.class)//
								.setDefaultValue(0.0)//
								.setPropertyValueMutability(false)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
								.build() //
	);//

	private final PropertyDefinition propertyDefinition;
	private final TestMaterialId testMaterialId;

	/**
	 * Returns the property definition associated with this member
	 */
	public PropertyDefinition getPropertyDefinition() {
		return propertyDefinition;
	}

	private TestBatchPropertyId(TestMaterialId testMaterialId, PropertyDefinition propertyDefinition) {
		this.testMaterialId = testMaterialId;
		this.propertyDefinition = propertyDefinition;
	}

	/**
	 * Returns the TestMaterialId that should be associated with the property
	 */
	public TestMaterialId getTestMaterialId() {
		return testMaterialId;
	}

	/**
	 * Returns the TestBatchPropertyIds associated with the given TestMaterialId
	 * 
	 * Preconditions: The TestMaterialId should not be null
	 */
	public static Set<TestBatchPropertyId> getTestBatchPropertyIds(TestMaterialId testMaterialId) {
		Set<TestBatchPropertyId> result = new LinkedHashSet<>();
		for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.values()) {
			if (testBatchPropertyId.testMaterialId == testMaterialId) {
				result.add(testBatchPropertyId);
			}
		}
		return result;
	}

	/**
	 * Returns a unique BatchPropertyId instance that is not a member of this
	 * enumeration
	 */
	public static BatchPropertyId getUnknownBatchPropertyId() {
		return new BatchPropertyId() {
		};

	}

	/**
	 * Returns a TestBatchPropertyId that is associated with the given material
	 * id and whose associated property definition is marked as mutable.
	 */
	public static TestBatchPropertyId getRandomMutableBatchPropertyId(final TestMaterialId testMaterialId, final RandomGenerator randomGenerator) {
		Set<TestBatchPropertyId> set = getTestBatchPropertyIds(testMaterialId);
		int count = 1;
		TestBatchPropertyId selectedTestBatchPropertyId = null;
		for (TestBatchPropertyId testBatchPropertyId : set) {
			if (testBatchPropertyId.propertyDefinition.propertyValuesAreMutable()) {
				if (randomGenerator.nextDouble() < (1.0 / count)) {
					selectedTestBatchPropertyId = testBatchPropertyId;
				}
			}
		}
		return selectedTestBatchPropertyId;

	}

	/**
	 * Returns a randomly selected value that is compatible with this member's
	 * associated property definition.
	 * 
	 */
	public Object getRandomPropertyValue(final RandomGenerator randomGenerator) {
		switch (this) {
		case BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK:
			return randomGenerator.nextBoolean();
		case BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK:
			return randomGenerator.nextInt();
		case BATCH_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK:
			return randomGenerator.nextDouble();
		case BATCH_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK:
			return randomGenerator.nextBoolean();
		case BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK:
			return randomGenerator.nextInt();
		case BATCH_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK:
			return randomGenerator.nextDouble();
		case BATCH_PROPERTY_3_1_BOOLEAN_MUTABLE_NO_TRACK:
			return randomGenerator.nextBoolean();
		case BATCH_PROPERTY_3_2_INTEGER_MUTABLE_NO_TRACK:
			return randomGenerator.nextInt();
		case BATCH_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK:
			return randomGenerator.nextDouble();
		default:
			
			throw new RuntimeException("unhandled case: " + this);

		}
	}
}
