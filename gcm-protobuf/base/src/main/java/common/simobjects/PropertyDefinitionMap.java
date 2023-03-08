package common.simobjects;

import plugins.util.properties.PropertyDefinition;

public class PropertyDefinitionMap {
    private Object propertyId;
    private PropertyDefinition propertyDefinition;

    public Object getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Object propertyId) {
        this.propertyId = propertyId;
    }

    public PropertyDefinition getPropertyDefinition() {
        return propertyDefinition;
    }

    public void setPropertyDefinition(PropertyDefinition propertyDefinition) {
        this.propertyDefinition = propertyDefinition;
    }
}
