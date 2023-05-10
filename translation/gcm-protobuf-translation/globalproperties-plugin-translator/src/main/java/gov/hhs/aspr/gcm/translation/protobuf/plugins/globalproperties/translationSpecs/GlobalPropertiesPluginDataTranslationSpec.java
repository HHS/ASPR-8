package gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translationSpecs;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;

import com.google.protobuf.Any;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.input.GlobalPropertiesPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyDefinitionInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyDefinitionMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyValueMapInput;
import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.util.properties.PropertyDefinition;

public class GlobalPropertiesPluginDataTranslationSpec
        extends ProtobufTranslationSpec<GlobalPropertiesPluginDataInput, GlobalPropertiesPluginData> {

    @Override
    protected GlobalPropertiesPluginData convertInputObject(GlobalPropertiesPluginDataInput inputObject) {
        GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();

        for (PropertyDefinitionMapInput propertyDefinitionMapInput : inputObject.getGlobalPropertyDefinitinionsList()) {
            GlobalPropertyId propertyId = this.translationEngine
                    .getObjectFromAny(propertyDefinitionMapInput.getPropertyId());
            PropertyDefinition propertyDefinition = this.translationEngine
                    .convertObject(propertyDefinitionMapInput.getPropertyDefinition());

            builder.defineGlobalProperty(propertyId, propertyDefinition,
                    propertyDefinitionMapInput.getPropertyDefinitionTime());
        }

        for (PropertyValueMapInput propertyValueMapInput : inputObject.getGlobalPropertyValuesList()) {

            GlobalPropertyId propertyId = this.translationEngine
                    .getObjectFromAny(propertyValueMapInput.getPropertyId());
            Object value = this.translationEngine.getObjectFromAny(propertyValueMapInput.getPropertyValue());

            builder.setGlobalPropertyValue(propertyId, value, propertyValueMapInput.getPropertyValueTime());
        }

        return builder.build();
    }

    @Override
    protected GlobalPropertiesPluginDataInput convertAppObject(GlobalPropertiesPluginData appObject) {
        GlobalPropertiesPluginDataInput.Builder builder = GlobalPropertiesPluginDataInput.newBuilder();

        for (GlobalPropertyId propertyId : appObject.getGlobalPropertyIds()) {
            PropertyDefinition propertyDefinition = appObject.getGlobalPropertyDefinition(propertyId);
            Object propertyValue = appObject.getGlobalPropertyValue(propertyId);

            PropertyDefinitionInput propertyDefinitionInput = this.translationEngine.convertObject(propertyDefinition);

            Any id = this.translationEngine.getAnyFromObject(propertyId);
            PropertyDefinitionMapInput propertyDefinitionMapInput = PropertyDefinitionMapInput
                    .newBuilder()
                    .setPropertyDefinition(propertyDefinitionInput)
                    .setPropertyId(id)
                    .setPropertyDefinitionTime(appObject.getGlobalPropertyDefinitionTime(propertyId))
                    .setPropertyTrackingPolicy(true)
                    .build();

            builder.addGlobalPropertyDefinitinions(propertyDefinitionMapInput);

            if (propertyDefinition.getDefaultValue().isEmpty()) {
                Any propertyValueInput = this.translationEngine.getAnyFromObject(propertyValue);

                PropertyValueMapInput propertyValueMapInput = PropertyValueMapInput
                        .newBuilder()
                        .setPropertyId(id)
                        .setPropertyValue(propertyValueInput)
                        .setPropertyValueTime(appObject.getGlobalPropertyTime(propertyId))
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
