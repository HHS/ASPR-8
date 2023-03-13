package gov.hhs.aspr.gcm.gcmprotobuf.properties.translators;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.gcmprotobuf.core.AbstractTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.properties.simobjects.PropertyValueMap;
import plugins.properties.input.PropertyValueMapInput;

public class PropertyValueMapTranslator extends AbstractTranslator<PropertyValueMapInput, PropertyValueMap> {

    @Override
    protected PropertyValueMap convertInputObject(PropertyValueMapInput inputObject) {
        PropertyValueMap propertyValueMap = new PropertyValueMap();

        propertyValueMap.setPropertyId(this.translator.getObjectFromAny(inputObject.getPropertyId()));
        propertyValueMap.setPropertyValue(this.translator.getObjectFromAny(inputObject.getPropertyValue()));
        return propertyValueMap;
    }

    @Override
    protected PropertyValueMapInput convertSimObject(PropertyValueMap simObject) {
        PropertyValueMapInput.Builder builder = PropertyValueMapInput.newBuilder();

        builder.setPropertyId(this.translator.getAnyFromObject(simObject.getPropertyId()));
        builder.setPropertyValue(this.translator.getAnyFromObject(simObject.getPropertyValue()));

        return builder.build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return PropertyValueMapInput.getDescriptor();
    }

    @Override
    public PropertyValueMapInput getDefaultInstanceForInputObject() {
        return PropertyValueMapInput.getDefaultInstance();
    }

    @Override
    public Class<PropertyValueMap> getSimObjectClass() {
        return PropertyValueMap.class;
    }

    @Override
    public Class<PropertyValueMapInput> getInputObjectClass() {
        return PropertyValueMapInput.class;
    }

}
