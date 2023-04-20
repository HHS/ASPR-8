package gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.simobjects;

import java.util.Objects;

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

    @Override
    public int hashCode() {
        return Objects.hash(propertyId, propertyDefinition);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PropertyDefinitionMap other = (PropertyDefinitionMap) obj;
        return Objects.equals(propertyId, other.propertyId)
                && Objects.equals(propertyDefinition, other.propertyDefinition);
    }

}
