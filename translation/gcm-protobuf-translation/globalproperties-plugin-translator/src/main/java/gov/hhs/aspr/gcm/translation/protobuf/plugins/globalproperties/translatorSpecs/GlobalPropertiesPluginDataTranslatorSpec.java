package gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translatorSpecs;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorSpec;

import com.google.protobuf.Any;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.input.GlobalPropertiesPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyDefinitionInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyDefinitionMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyValueMapInput;
import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.util.properties.PropertyDefinition;

public class GlobalPropertiesPluginDataTranslatorSpec
        extends ProtobufTranslatorSpec<GlobalPropertiesPluginDataInput, GlobalPropertiesPluginData> {

    @Override
    protected GlobalPropertiesPluginData convertInputObject(GlobalPropertiesPluginDataInput inputObject) {
        GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();

        for (PropertyDefinitionMapInput propertyDefinitionMapInput : inputObject.getGlobalPropertyDefinitinionsList()) {
            GlobalPropertyId propertyId = this.translatorCore.getObjectFromAny(propertyDefinitionMapInput.getPropertyId());
            PropertyDefinition propertyDefinition = this.translatorCore
                    .convertObject(propertyDefinitionMapInput.getPropertyDefinition());

            builder.defineGlobalProperty(propertyId, propertyDefinition, propertyDefinitionMapInput.getTime());
        }

        for (PropertyValueMapInput propertyValueMapInput : inputObject.getGlobalPropertyValuesList()) {

            GlobalPropertyId propertyId = this.translatorCore.getObjectFromAny(propertyValueMapInput.getPropertyId());
            Object value = this.translatorCore.getObjectFromAny(propertyValueMapInput.getPropertyValue());

            builder.setGlobalPropertyValue(propertyId, value, propertyValueMapInput.getTime());
        }

        return builder.build();
    }

    @Override
    protected GlobalPropertiesPluginDataInput convertAppObject(GlobalPropertiesPluginData appObject) {
        GlobalPropertiesPluginDataInput.Builder builder = GlobalPropertiesPluginDataInput.newBuilder();

        for (GlobalPropertyId propertyId : appObject.getGlobalPropertyIds()) {
            PropertyDefinition propertyDefinition = appObject.getGlobalPropertyDefinition(propertyId);
            Object propertyValue = appObject.getGlobalPropertyValue(propertyId);

            PropertyDefinitionInput propertyDefinitionInput = this.translatorCore.convertObject(propertyDefinition);

            Any id = this.translatorCore.getAnyFromObject(propertyId);
            PropertyDefinitionMapInput propertyDefinitionMapInput = PropertyDefinitionMapInput
                    .newBuilder()
                    .setPropertyDefinition(propertyDefinitionInput)
                    .setPropertyId(id)
                    .setTime(appObject.getGlobalPropertyDefinitionTime(propertyId))
                    .build();

            builder.addGlobalPropertyDefinitinions(propertyDefinitionMapInput);

            if (propertyDefinition.getDefaultValue().isEmpty()) {
                Any propertyValueInput = this.translatorCore.getAnyFromObject(propertyValue);

                PropertyValueMapInput propertyValueMapInput = PropertyValueMapInput
                        .newBuilder()
                        .setPropertyId(id)
                        .setPropertyValue(propertyValueInput)
                        .setTime(appObject.getGlobalPropertyTime(propertyId))
                        .build();
                builder.addGlobalPropertyValues(propertyValueMapInput);
            }

        }

        return builder.build();
    }

    @Override
    public Class<GlobalPropertiesPluginData> getAppObjectClass() {
        return GlobalPropertiesPluginData.class;
    }

    @Override
    public Class<GlobalPropertiesPluginDataInput> getInputObjectClass() {
        return GlobalPropertiesPluginDataInput.class;
    }

}
