package gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyValueMapInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.simobjects.PropertyValueMap;

public class PropertyValueMapTranslatorSpec extends ProtobufTranslatorSpec<PropertyValueMapInput, PropertyValueMap> {

    @Override
    protected PropertyValueMap convertInputObject(PropertyValueMapInput inputObject) {
        PropertyValueMap propertyValueMap = new PropertyValueMap();

        propertyValueMap.setPropertyId(this.translatorCore.getObjectFromAny(inputObject.getPropertyId()));
        propertyValueMap.setPropertyValue(this.translatorCore.getObjectFromAny(inputObject.getPropertyValue()));
        return propertyValueMap;
    }

    @Override
    protected PropertyValueMapInput convertAppObject(PropertyValueMap simObject) {
        PropertyValueMapInput.Builder builder = PropertyValueMapInput.newBuilder();

        builder.setPropertyId(this.translatorCore.getAnyFromObject(simObject.getPropertyId()));
        builder.setPropertyValue(this.translatorCore.getAnyFromObject(simObject.getPropertyValue()));

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
