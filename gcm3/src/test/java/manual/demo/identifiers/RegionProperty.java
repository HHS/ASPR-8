package manual.demo.identifiers;

import plugins.properties.support.PropertyDefinition;
import plugins.regions.support.RegionPropertyId;

public enum RegionProperty implements RegionPropertyId {

	LAT(PropertyDefinition.builder().setType(Double.class).build()),

	LON(PropertyDefinition.builder().setType(Double.class).build()),

	FLAG(PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).build());

	private final PropertyDefinition propertyDefinition;

	private RegionProperty(PropertyDefinition propertyDefinition) {
		this.propertyDefinition = propertyDefinition;
	}

	public PropertyDefinition getPropertyDefinition() {
		return propertyDefinition;
	}

}
