package gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.simobjects;

import java.util.Objects;

public class PropertyValueMap {
    Object propertyId;
    Object propertyValue;

    public Object getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Object propertyId) {
        this.propertyId = propertyId;
    }

    public Object getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(Object propertyValue) {
        this.propertyValue = propertyValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(propertyId, propertyValue);
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
        PropertyValueMap other = (PropertyValueMap) obj;
        return Objects.equals(propertyId, other.propertyId) && Objects.equals(propertyValue, other.propertyValue);
    }

	
}
