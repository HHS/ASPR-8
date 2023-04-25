package gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyValueMapInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.simobjects.PropertyValueMap;

public class PropertyValueMapTranslatorSpec extends ProtobufTranslationSpec<PropertyValueMapInput, PropertyValueMap> {

    @Override
    protected PropertyValueMap convertInputObject(PropertyValueMapInput inputObject) {
        PropertyValueMap propertyValueMap = new PropertyValueMap();

        propertyValueMap.setPropertyId(this.translatorCore.getObjectFromAny(inputObject.getPropertyId()));
        propertyValueMap.setPropertyValue(this.translatorCore.getObjectFromAny(inputObject.getPropertyValue()));
        return propertyValueMap;
    }

    @Override
    protected PropertyValueMapInput convertAppObject(PropertyValueMap appObject) {
        PropertyValueMapInput.Builder builder = PropertyValueMapInput.newBuilder();

        builder.setPropertyId(this.translatorCore.getAnyFromObject(appObject.getPropertyId()));
        builder.setPropertyValue(this.translatorCore.getAnyFromObject(appObject.getPropertyValue()));

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
