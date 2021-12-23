package manual.pubsub.globalcomponents;

import plugins.globals.support.GlobalPropertyId;
import plugins.properties.support.PropertyDefinition;

public enum GlobalProperty implements GlobalPropertyId {

	X(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),

	Y(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),

	MAX(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(false).setType(Integer.class).build()),

	POPULATION_SIZE(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(false).setType(Integer.class).build()),

	;

	// X1(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X2(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X3(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X4(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X5(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X6(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X7(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X8(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X9(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X10(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X11(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X12(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X13(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X14(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X15(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X16(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X17(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X18(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X19(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X20(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X21(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X22(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X23(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X24(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X25(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X26(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X27(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X28(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X29(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X30(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X31(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X32(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X33(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X34(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X35(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X36(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X37(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X38(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X39(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),
	// X40(PropertyDefinition.builder().setDefaultValue(0).setPropertyValueMutability(true).setType(Integer.class).build()),

	private final PropertyDefinition propertyDefinition;

	private GlobalProperty(PropertyDefinition propertyDefinition) {
		this.propertyDefinition = propertyDefinition;
	}

	public PropertyDefinition getPropertyDefinition() {
		return propertyDefinition;
	}

}
