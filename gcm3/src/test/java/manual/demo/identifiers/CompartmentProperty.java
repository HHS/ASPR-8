package manual.demo.identifiers;

import plugins.compartments.support.CompartmentPropertyId;
import plugins.properties.support.PropertyDefinition;

public enum CompartmentProperty implements CompartmentPropertyId {
	WEIGHT_THRESHOLD(Compartment.INFECTED, PropertyDefinition.builder().setType(Double.class).setDefaultValue(0d).build());

	private final Compartment compartment;
	private final PropertyDefinition propertyDefinition;

	private CompartmentProperty(Compartment compartment, PropertyDefinition propertyDefinition) {
		this.compartment = compartment;
		this.propertyDefinition = propertyDefinition;
	}

	public Compartment getCompartment() {
		return compartment;
	}

	public PropertyDefinition getPropertyDefinition() {
		return propertyDefinition;
	}
}
