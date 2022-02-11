package manual.demo.identifiers;

import manual.demo.datatypes.PopulationDescription;
import plugins.globals.support.GlobalPropertyId;
import plugins.properties.support.PropertyDefinition;

public enum GlobalProperty implements GlobalPropertyId {

	POPULATION_DESCRIPTION(PropertyDefinition.builder().setType(PopulationDescription.class).build()), 
	
	ALPHA(PropertyDefinition.builder().setType(Double.class).setDefaultValue(0.0).setPropertyValueMutability(true).build());

	private final PropertyDefinition propertyDefinition;

	private GlobalProperty(PropertyDefinition propertyDefinition) {
		this.propertyDefinition = propertyDefinition;
	}

	public PropertyDefinition getPropertyDefinition() {
		return propertyDefinition;
	}

}
