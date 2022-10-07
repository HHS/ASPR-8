package temp.filtereventtests;

import org.apache.commons.math3.random.RandomGenerator;

import plugins.personproperties.support.PersonPropertyId;
import plugins.util.properties.PropertyDefinition;

public enum PersonPropertyIdentifier implements PersonPropertyId {

	PROP_INT_1(PropertyDefinition.builder().setType(Integer.class).setDefaultValue(1).build()),
	PROP_INT_2(PropertyDefinition.builder().setType(Integer.class).setDefaultValue(2).build()),
	PROP_INT_3(PropertyDefinition.builder().setType(Integer.class).setDefaultValue(3).build()),
	PROP_INT_4(PropertyDefinition.builder().setType(Integer.class).setDefaultValue(4).build()),

	PROP_DOUBLE_5(PropertyDefinition.builder().setType(Double.class).setDefaultValue(5.0).build()),
	PROP_DOUBLE_6(PropertyDefinition.builder().setType(Double.class).setDefaultValue(6.0).build()),
	PROP_DOUBLE_7(PropertyDefinition.builder().setType(Double.class).setDefaultValue(7.0).build()),
	PROP_DOUBLE_8(PropertyDefinition.builder().setType(Double.class).setDefaultValue(8.0).build()),

	PROP_BOOLEAN_9(PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).build()),
	PROP_BOOLEAN_10(PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).build()),
	PROP_BOOLEAN_11(PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).build()),
	PROP_BOOLEAN_12(PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).build()),
	PROP_BOOLEAN_13(PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).build()),
	PROP_BOOLEAN_14(PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).build()),
	PROP_BOOLEAN_15(PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).build()),
	PROP_BOOLEAN_16(PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).build()),
	PROP_BOOLEAN_17(PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).build()),
	PROP_BOOLEAN_18(PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).build()),
	PROP_BOOLEAN_19(PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).build()),

	;

	private final PropertyDefinition propertyDefinition;

	private PersonPropertyIdentifier(PropertyDefinition propertyDefinition) {
		this.propertyDefinition = propertyDefinition;
	}

	public PropertyDefinition getPropertyDefinition() {
		return propertyDefinition;
	}

	/**
	 * Returns a randomly selected value that is compatible with this member's
	 * associated property definition.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <T> T getRandomPropertyValue(final RandomGenerator randomGenerator) {
		switch (this) {
		case PROP_INT_1, PROP_INT_2, PROP_INT_3, PROP_INT_4:
			Integer i = randomGenerator.nextInt();
			return (T) i;
		case PROP_DOUBLE_5, PROP_DOUBLE_6, PROP_DOUBLE_7, PROP_DOUBLE_8:
			Double d = randomGenerator.nextDouble();
			return (T) d;
		default:
			Boolean b = randomGenerator.nextBoolean();
			return (T) b;
		}
	}

}
