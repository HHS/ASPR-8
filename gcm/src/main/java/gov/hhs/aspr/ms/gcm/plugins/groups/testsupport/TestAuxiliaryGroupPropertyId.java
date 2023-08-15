package gov.hhs.aspr.ms.gcm.plugins.groups.testsupport;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyDefinition;

public enum TestAuxiliaryGroupPropertyId implements GroupPropertyId {

	GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK(
			TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_1, //
			PropertyDefinition	.builder()//
								.setType(Boolean.class)//
								.setDefaultValue(false)//
								.setPropertyValueMutability(true)//
								.build()), //
	GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK(
			TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_1, //
			PropertyDefinition	.builder()//
								.setType(Integer.class)//
								.setDefaultValue(0)//
								.setPropertyValueMutability(true)//
								.build() //
	), //
	GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK(
			TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_1, //
			PropertyDefinition	.builder()//
								.setType(Double.class)//
								.setDefaultValue(0.0)//
								.setPropertyValueMutability(true)//
								.build() //
	), //
	GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK(
			TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_2, //
			PropertyDefinition	.builder()//
								.setType(Boolean.class)//
								.setDefaultValue(false)//
								.setPropertyValueMutability(true)//
								.build() //
	), //

	GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK(
			TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_2, //
			PropertyDefinition	.builder()//
								.setType(Integer.class)//
								// .setDefaultValue(0)//
								.setPropertyValueMutability(true)//
								.build() //
	), //
	GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK(
			TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_2, //
			PropertyDefinition	.builder()//
								.setType(Double.class)//
								.setDefaultValue(0.0)//
								.setPropertyValueMutability(true)//
								.build() //
	), //
	GROUP_PROPERTY_3_1_BOOLEAN_IMMUTABLE_NO_TRACK(
			TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_3, //
			PropertyDefinition	.builder()//
								.setType(Boolean.class)//
								.setDefaultValue(false)//
								.setPropertyValueMutability(false)//
								.build() //
	), //
	GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK(
			TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_3, //
			PropertyDefinition	.builder()//
								.setType(Integer.class)//
								.setDefaultValue(0)//
								.setPropertyValueMutability(false)//
								.build() //
	), //
	GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK(
			TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_3, //
			PropertyDefinition	.builder()//
								.setType(Double.class)//
								.setDefaultValue(0.0)//
								.setPropertyValueMutability(false)//
								.build() //
	);//

	private final PropertyDefinition propertyDefinition;
	private final TestAuxiliaryGroupTypeId testAuxiliaryGroupTypeId;

	/**
	 * Returns the property definition associated with this member
	 */
	public PropertyDefinition getPropertyDefinition() {
		return propertyDefinition;
	}

	private TestAuxiliaryGroupPropertyId(TestAuxiliaryGroupTypeId testAuxiliaryGroupTypeId, PropertyDefinition propertyDefinition) {
		this.testAuxiliaryGroupTypeId = testAuxiliaryGroupTypeId;
		this.propertyDefinition = propertyDefinition;
	}

	/**
	 * Returns the TestGroupTypeId that should be the type associated with the
	 * property
	 */
	public TestAuxiliaryGroupTypeId getTestGroupTypeId() {
		return testAuxiliaryGroupTypeId;
	}

	/**
	 * Returns the TestAuxiliaryGroupPropertyId associated with the given
	 * TestAuxiliaryGroupTypeId
	 * 
	 * Preconditions: The TestAuxiliaryGroupTypeId should not be null
	 */
	public static Set<TestAuxiliaryGroupPropertyId> getTestGroupPropertyIds(TestAuxiliaryGroupTypeId testAuxiliaryGroupTypeId) {
		Set<TestAuxiliaryGroupPropertyId> result = new LinkedHashSet<>();
		for (TestAuxiliaryGroupPropertyId testGroupPropertyId : TestAuxiliaryGroupPropertyId.values()) {
			if (testGroupPropertyId.testAuxiliaryGroupTypeId == testAuxiliaryGroupTypeId) {
				result.add(testGroupPropertyId);
			}
		}
		return result;
	}

	/**
	 * Returns a unique GroupPropertyId instance that is not a member of this
	 * enumeration
	 */
	public static GroupPropertyId getUnknownGroupPropertyId() {
		return new GroupPropertyId() {
		};
	}

	/**
	 * Returns a randomly selected value that is compatible with this member's
	 * associated property definition.
	 */
	public Object getRandomPropertyValue(final RandomGenerator randomGenerator) {
		switch (this) {
		case GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK:
			return randomGenerator.nextBoolean();
		case GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK:
			return randomGenerator.nextInt();
		case GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK:
			return randomGenerator.nextDouble();
		case GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK:
			return randomGenerator.nextBoolean();
		case GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK:
			return randomGenerator.nextInt();
		case GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK:
			return randomGenerator.nextDouble();
		case GROUP_PROPERTY_3_1_BOOLEAN_IMMUTABLE_NO_TRACK:
			return randomGenerator.nextBoolean();
		case GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK:
			return randomGenerator.nextInt();
		case GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK:
			return randomGenerator.nextDouble();
		default:
			throw new RuntimeException("unhandled case: " + this);

		}
	}
}
