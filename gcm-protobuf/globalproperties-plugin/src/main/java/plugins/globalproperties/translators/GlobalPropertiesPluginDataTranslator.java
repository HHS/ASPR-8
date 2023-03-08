package plugins.globalproperties.translators;

import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.Descriptors.Descriptor;

import base.AbstractTranslator;
import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.globalproperties.GlobalPropertiesPluginDataInput;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.globalproperties.support.SimpleGlobalPropertyId;
import plugins.properties.input.PropertyDefinitionMapInput;
import plugins.properties.input.PropertyValueMapInput;
import plugins.properties.simobjects.PropertyDefinitionMap;
import plugins.properties.simobjects.PropertyValueMap;
import plugins.util.properties.PropertyDefinition;

public class GlobalPropertiesPluginDataTranslator
        extends AbstractTranslator<GlobalPropertiesPluginDataInput, GlobalPropertiesPluginData> {

    @Override
    protected GlobalPropertiesPluginData convertInputObject(GlobalPropertiesPluginDataInput inputObject) {
        GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();

        for (PropertyDefinitionMapInput propertyDefinitionMapInput : inputObject.getGlobalPropertyDefinitinionsList()) {

            PropertyDefinitionMap propertyDefinitionMap = this.translator
                    .convertInputObject(propertyDefinitionMapInput);
            GlobalPropertyId propertyId = new SimpleGlobalPropertyId(propertyDefinitionMap.getPropertyId());
            PropertyDefinition propertyDefinition = propertyDefinitionMap.getPropertyDefinition();

            builder.defineGlobalProperty(propertyId, propertyDefinition);
        }

        for (PropertyValueMapInput propertyValueMapInput : inputObject.getGlobalPropertyValuesList()) {

            PropertyValueMap propertyValueMap = this.translator.convertInputObject(propertyValueMapInput);
            GlobalPropertyId propertyId = new SimpleGlobalPropertyId(propertyValueMap.getPropertyId());
            Object value = propertyValueMap.getPropertyValue();

            builder.setGlobalPropertyValue(propertyId, value);
        }

        return builder.build();
    }

    @Override
    protected GlobalPropertiesPluginDataInput convertSimObject(GlobalPropertiesPluginData simObject) {
        GlobalPropertiesPluginDataInput.Builder builder = GlobalPropertiesPluginDataInput.newBuilder();

        List<PropertyDefinitionMapInput> propertyDefinitions = new ArrayList<>();
        List<PropertyValueMapInput> propertyValues = new ArrayList<>();

        for (GlobalPropertyId propertyId : simObject.getGlobalPropertyIds()) {
            SimpleGlobalPropertyId globalPropertyId = (SimpleGlobalPropertyId) propertyId;
            PropertyDefinition propertyDefinition = simObject.getGlobalPropertyDefinition(propertyId);
            Object propertyValue = simObject.getGlobalPropertyValue(propertyId);

            PropertyDefinitionMap propertyDefinitionMap = new PropertyDefinitionMap();

            propertyDefinitionMap.setPropertyId(globalPropertyId.getValue());
            propertyDefinitionMap.setPropertyDefinition(propertyDefinition);

            propertyDefinitions.add(this.translator
                    .convertSimObject(propertyDefinitionMap));

            if (propertyDefinition.getDefaultValue().isEmpty()) {
                PropertyValueMap propertyValueMap = new PropertyValueMap();

                propertyValueMap.setPropertyId(globalPropertyId.getValue());
                propertyValueMap.setPropertyValue(propertyValue);

                propertyValues.add(this.translator.convertSimObject(propertyValueMap));
            }

        }

        builder.addAllGlobalPropertyDefinitinions(propertyDefinitions).addAllGlobalPropertyValues(propertyValues);

        return builder.build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return GlobalPropertiesPluginDataInput.getDescriptor();
    }

    @Override
    public GlobalPropertiesPluginDataInput getDefaultInstanceForInputObject() {
        return GlobalPropertiesPluginDataInput.getDefaultInstance();
    }

    @Override
    public Class<GlobalPropertiesPluginData> getSimObjectClass() {
        return GlobalPropertiesPluginData.class;
    }

    @Override
    public Class<GlobalPropertiesPluginDataInput> getInputObjectClass() {
        return GlobalPropertiesPluginDataInput.class;
    }

}
