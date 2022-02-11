package manual.demo.identifiers;

import manual.demo.datatypes.Sex;
import plugins.personproperties.support.PersonPropertyId;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.TimeTrackingPolicy;

public enum PersonProperty implements PersonPropertyId {

	HEIGHT(PropertyDefinition.builder().setType(Double.class).setDefaultValue(0.0).build()), AGE(PropertyDefinition.builder().setType(Integer.class).setDefaultValue(0).build()), WEIGHT(PropertyDefinition.builder().setType(Float.class).setDefaultValue(0.0F).build()), SEX(PropertyDefinition.builder().setType(Sex.class).setDefaultValue(Sex.FEMALE).build()), IMMUNE(PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(Boolean.FALSE).build()), SHADY(PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(Boolean.FALSE).build()), UNCTUOUS(PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(Boolean.FALSE).build()), WEIRD(PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(Boolean.FALSE).setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME).build());

	private final PropertyDefinition propertyDefinition;

	private PersonProperty(PropertyDefinition propertyDefinition) {
		this.propertyDefinition = propertyDefinition;
	}

	public PropertyDefinition getPropertyDefinition() {
		return propertyDefinition;
	}
}
