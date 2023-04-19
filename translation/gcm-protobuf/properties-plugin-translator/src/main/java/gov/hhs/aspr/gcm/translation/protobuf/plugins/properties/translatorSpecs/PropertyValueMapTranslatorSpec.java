package gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyValueMapInput;
import gov.hhs.aspr.translation.protobuf.core.AbstractProtobufTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.simobjects.PropertyValueMap;

public class PropertyValueMapTranslatorSpec extends AbstractProtobufTranslatorSpec<PropertyValueMapInput, PropertyValueMap> {

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
    public Class<PropertyValueMap> getAppObjectClass() {
        return PropertyValueMap.class;
    }

    @Override
    public Class<PropertyValueMapInput> getInputObjectClass() {
        return PropertyValueMapInput.class;
    }

}
