package plugins.globalproperties.translators;

import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.Any;
import com.google.protobuf.Descriptors.Descriptor;

import base.AbstractTranslator;
import common.PropertyDefinitionInput;
import common.PropertyDefinitionMap;
import common.PropertyValueMap;
import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.globalproperties.GlobalPropertiesPluginDataInput;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.globalproperties.support.SimpleGlobalPropertyId;
import plugins.util.properties.PropertyDefinition;

public class GlobalPropertiesPluginDataTranslator
        extends AbstractTranslator<GlobalPropertiesPluginDataInput, GlobalPropertiesPluginData> {

    @Override
    protected GlobalPropertiesPluginData convertInputObject(GlobalPropertiesPluginDataInput inputObject) {
        GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();

        for (PropertyDefinitionMap propertyDefinitionMap : inputObject.getGlobalPropertyDefinitinionsList()) {
            GlobalPropertyId propertyId = new SimpleGlobalPropertyId(
                    this.translator.getObjectFromAny(propertyDefinitionMap.getPropertyId()));
            PropertyDefinition propertyDefinition = (PropertyDefinition) this.translator
                    .convertInputObject(propertyDefinitionMap.getPropertyDefinition());

            builder.defineGlobalProperty(propertyId, propertyDefinition);
        }

        for (PropertyValueMap propertyValueMap : inputObject.getGlobalPropertyValuesList()) {
            GlobalPropertyId propertyId = new SimpleGlobalPropertyId(
                    this.translator.getObjectFromAny(propertyValueMap.getPropertyId()));
            Object value = this.translator.getObjectFromAny(propertyValueMap.getPropertyValue());

            builder.setGlobalPropertyValue(propertyId, value);
        }

        return builder.build();
    }

    @Override
    protected GlobalPropertiesPluginDataInput convertSimObject(GlobalPropertiesPluginData simObject) {
        GlobalPropertiesPluginDataInput.Builder builder = GlobalPropertiesPluginDataInput.newBuilder();

        List<PropertyDefinitionMap> propertyDefinitions = new ArrayList<>();
        List<PropertyValueMap> propertyValues = new ArrayList<>();

        for (GlobalPropertyId propertyId : simObject.getGlobalPropertyIds()) {
            SimpleGlobalPropertyId globalPropertyId = (SimpleGlobalPropertyId) propertyId;
            PropertyDefinition propertyDefinition = simObject.getGlobalPropertyDefinition(propertyId);
            Object value = simObject.getGlobalPropertyValue(propertyId);

            PropertyDefinitionMap.Builder propertyDefBuilder = PropertyDefinitionMap.newBuilder();
            PropertyValueMap.Builder propertyValueBuilder = PropertyValueMap.newBuilder();

            Any anyPropId = this.translator.getAnyFromObject(globalPropertyId.getValue());
            PropertyDefinitionInput propertyDefinitionInput = this.translator.convertSimObject(propertyDefinition);

            if (propertyDefinition.getDefaultValue().isEmpty()) {
                Any anyValue = this.translator.getAnyFromObject(value);
                propertyValueBuilder.setPropertyId(anyPropId).setPropertyValue(anyValue);
                propertyValues.add(propertyValueBuilder.build());
            }

            propertyDefBuilder.setPropertyId(anyPropId).setPropertyDefinition(propertyDefinitionInput);

            propertyDefinitions.add(propertyDefBuilder.build());
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
