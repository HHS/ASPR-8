package gov.hhs.aspr.gcm.translation.plugins.properties.translatorSpecs;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.properties.input.PropertyValueMapInput;
import gov.hhs.aspr.gcm.translation.plugins.properties.simobjects.PropertyValueMap;

public class PropertyValueMapTranslatorSpec extends AObjectTranslatorSpec<PropertyValueMapInput, PropertyValueMap> {

    @Override
    protected PropertyValueMap convertInputObject(PropertyValueMapInput inputObject) {
        PropertyValueMap propertyValueMap = new PropertyValueMap();

        propertyValueMap.setPropertyId(this.translator.getObjectFromAny(inputObject.getPropertyId()));
        propertyValueMap.setPropertyValue(this.translator.getObjectFromAny(inputObject.getPropertyValue()));
        return propertyValueMap;
    }

    @Override
    protected PropertyValueMapInput convertAppObject(PropertyValueMap simObject) {
        PropertyValueMapInput.Builder builder = PropertyValueMapInput.newBuilder();

        builder.setPropertyId(this.translator.getAnyFromObject(simObject.getPropertyId()));
        builder.setPropertyValue(this.translator.getAnyFromObject(simObject.getPropertyValue()));

        return builder.build();
    }

    @Override
    public PropertyValueMapInput getDefaultInstanceForInputObject() {
        return PropertyValueMapInput.getDefaultInstance();
    }

    @Override
    public Class<PropertyValueMap> getAppObjectClass() {
        return PropertyValueMap.class;
    }

    @Override
    public Class<PropertyValueMapInput> getInputObjectClass() {
        return PropertyValueMapInput.class;
    }

}
