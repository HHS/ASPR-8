package plugins.globalproperties.translators;

import com.google.protobuf.Descriptors.Descriptor;

import base.AbstractTranslator;
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'convertSimObject'");
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
