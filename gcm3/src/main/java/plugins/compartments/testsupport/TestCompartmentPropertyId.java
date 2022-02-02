package plugins.compartments.testsupport;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import plugins.compartments.support.CompartmentPropertyId;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.TimeTrackingPolicy;

public enum TestCompartmentPropertyId implements CompartmentPropertyId {
	COMPARTMENT_PROPERTY_1_1(//
			TestCompartmentId.COMPARTMENT_1, //
			PropertyDefinition	.builder()//
								.setType(Boolean.class)//
								.setDefaultValue(false)//
								.setPropertyValueMutability(true)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
								.build()), //
	COMPARTMENT_PROPERTY_1_2(//
			TestCompartmentId.COMPARTMENT_1, //
			PropertyDefinition	.builder()//
								.setType(Integer.class)//
								.setDefaultValue(0)//
								.setPropertyValueMutability(true)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
								.build()), //
	COMPARTMENT_PROPERTY_1_3(//
			TestCompartmentId.COMPARTMENT_1, //
			PropertyDefinition	.builder()//
								.setType(Double.class)//
								.setDefaultValue(1.2D)//
								.setPropertyValueMutability(true)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
								.build()), //
	COMPARTMENT_PROPERTY_2_1(//
			TestCompartmentId.COMPARTMENT_2, //
			PropertyDefinition	.builder()//
								.setType(Boolean.class)//
								.setDefaultValue(false)//
								.setPropertyValueMutability(false)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
								.build()), //

	COMPARTMENT_PROPERTY_2_2(//
			TestCompartmentId.COMPARTMENT_2, //
			PropertyDefinition	.builder()//
								.setType(Integer.class)//
								.setDefaultValue(7)//
								.setPropertyValueMutability(false)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
								.build()), //

	COMPARTMENT_PROPERTY_3_1(//
			TestCompartmentId.COMPARTMENT_3, //
			PropertyDefinition	.builder()//
								.setType(Boolean.class)//
								.setDefaultValue(true)//
								.setPropertyValueMutability(true)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
								.build()), //

	COMPARTMENT_PROPERTY_3_2(//
			TestCompartmentId.COMPARTMENT_3, //
			PropertyDefinition	.builder()//
								.setType(Integer.class)//
								.setDefaultValue(13)//
								.setPropertyValueMutability(true)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)//
								.build()), //
	COMPARTMENT_PROPERTY_3_3(//
			TestCompartmentId.COMPARTMENT_3, //
			PropertyDefinition	.builder()//
								.setType(Double.class)//
								.setDefaultValue(6.775)//
								.setPropertyValueMutability(true)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)//
								.build()), //
	COMPARTMENT_PROPERTY_3_4(//
			TestCompartmentId.COMPARTMENT_3, //
			PropertyDefinition	.builder()//
								.setType(Boolean.class)//
								.setDefaultValue(false)//
								.setPropertyValueMutability(true)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
								.build()), //

	COMPARTMENT_PROPERTY_4_1(//
			TestCompartmentId.COMPARTMENT_4, //
			PropertyDefinition	.builder()//
								.setType(Boolean.class)//
								.setDefaultValue(false)//
								.setPropertyValueMutability(true)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
								.build()), //

	COMPARTMENT_PROPERTY_5_1(//
			TestCompartmentId.COMPARTMENT_5, //
			PropertyDefinition	.builder()//
								.setType(Integer.class)//
								.setDefaultValue(45)//
								.setPropertyValueMutability(true)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
								.build()), //

	COMPARTMENT_PROPERTY_5_2(//
			TestCompartmentId.COMPARTMENT_5, //
			PropertyDefinition	.builder()//
								.setType(Double.class)//
								.setDefaultValue(5.423)//
								.setPropertyValueMutability(true)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
								.build()), //

	COMPARTMENT_PROPERTY_5_3(//
			TestCompartmentId.COMPARTMENT_5, //
			PropertyDefinition	.builder()//
								.setType(Long.class)//
								.setDefaultValue(2346345345L)//
								.setPropertyValueMutability(true)//
								.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
								.build()), //
	;

	private final TestCompartmentId testCompartmentId;
	private final PropertyDefinition propertyDefinition;

	public TestCompartmentId getTestCompartmentId() {
		return testCompartmentId;
	}

	public PropertyDefinition getPropertyDefinition() {
		return propertyDefinition;
	}

	private TestCompartmentPropertyId(TestCompartmentId testCompartmentId, PropertyDefinition propertyDefinition) {
		this.testCompartmentId = testCompartmentId;
		this.propertyDefinition = propertyDefinition;
	}

	public static CompartmentPropertyId getUnknownCompartmentPropertyId() {
		return new CompartmentPropertyId() {
		};
	}

	public Object getRandomPropertyValue(final RandomGenerator randomGenerator) {
		switch(this) {
		case COMPARTMENT_PROPERTY_1_1:
			return randomGenerator.nextBoolean();
		case COMPARTMENT_PROPERTY_1_2:
			return randomGenerator.nextInt();			
		case COMPARTMENT_PROPERTY_1_3:
			return randomGenerator.nextDouble();			
		case COMPARTMENT_PROPERTY_2_1:
			return randomGenerator.nextBoolean();
		case COMPARTMENT_PROPERTY_2_2:
			return randomGenerator.nextInt();
		case COMPARTMENT_PROPERTY_3_1:
			return randomGenerator.nextBoolean();
		case COMPARTMENT_PROPERTY_3_2:
			return randomGenerator.nextInt();
		case COMPARTMENT_PROPERTY_3_3:
			return randomGenerator.nextDouble();
		case COMPARTMENT_PROPERTY_3_4:
			return randomGenerator.nextBoolean();
		case COMPARTMENT_PROPERTY_4_1:
			return randomGenerator.nextBoolean();
		case COMPARTMENT_PROPERTY_5_1:
			return randomGenerator.nextInt();
		case COMPARTMENT_PROPERTY_5_2:
			return randomGenerator.nextDouble();
		case COMPARTMENT_PROPERTY_5_3:
			return randomGenerator.nextLong();
		default:
			throw new RuntimeException("unhandled case: " + this); 		
		}
	}

	public static List<TestCompartmentPropertyId> getTestCompartmentPropertyIds(TestCompartmentId testCompartmentId) {

		List<TestCompartmentPropertyId> result = new ArrayList<>();
		for (TestCompartmentPropertyId testCompartmentPropertyId : TestCompartmentPropertyId.values()) {
			if (testCompartmentPropertyId.testCompartmentId == testCompartmentId) {
				result.add(testCompartmentPropertyId);
			}
		}
		return result;

	}
}
