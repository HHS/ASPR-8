package testsupport.translators;

import com.google.protobuf.Descriptors.Descriptor;

import base.AbstractTranslator;
import common.PropertyValueMap;
import testsupport.simobjects.PropertyValueMapSimObject;

public class PropertyValueMapTranslator extends AbstractTranslator<PropertyValueMap, PropertyValueMapSimObject> {

    @Override
    protected PropertyValueMapSimObject convertInputObject(PropertyValueMap inputObject) {
        PropertyValueMapSimObject simObject = new PropertyValueMapSimObject();
        simObject.setKey(this.translator.getObjectFromAny(inputObject.getPropertyId()));
        simObject.setValue(this.translator.getObjectFromAny(inputObject.getPropertyValue()));

        return simObject;
    }

    @Override
    protected PropertyValueMap convertSimObject(PropertyValueMapSimObject simObject) {
        PropertyValueMap.Builder builder = PropertyValueMap.newBuilder();

        builder.setPropertyId(this.translator.getAnyFromObject(simObject.getKey()));
        builder.setPropertyValue(this.translator.getAnyFromObject(simObject.getValue()));

        return builder.build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return PropertyValueMap.getDescriptor();
    }

    @Override
    public PropertyValueMap getDefaultInstanceForInputObject() {
        return PropertyValueMap.getDefaultInstance();
    }

    @Override
    public Class<PropertyValueMapSimObject> getSimObjectClass() {
        return PropertyValueMapSimObject.class;
    }

    @Override
    public Class<PropertyValueMap> getInputObjectClass() {
        return PropertyValueMap.class;
    }

}
