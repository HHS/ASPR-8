package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.globalproperties.translationSpecs;

import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;

import com.google.protobuf.Any;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.globalproperties.data.input.GlobalPropertiesPluginDataInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionMapInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyValueMapInput;
import plugins.globalproperties.datamanagers.GlobalPropertiesPluginData;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.util.properties.PropertyDefinition;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain GlobalPropertiesPluginDataInput} and
 * {@linkplain GlobalPropertiesPluginData}
 */
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

        for (GlobalPropertyId globalPropertyId : appObject.getGlobalPropertyDefinitions().keySet()) {
            PropertyDefinition propertyDefinition = appObject.getGlobalPropertyDefinition(globalPropertyId);

            PropertyDefinitionInput propertyDefinitionInput = this.translationEngine.convertObject(propertyDefinition);

            PropertyDefinitionMapInput propertyDefinitionMapInput = PropertyDefinitionMapInput
                    .newBuilder()
                    .setPropertyDefinition(propertyDefinitionInput)
                    .setPropertyId(this.translationEngine.getAnyFromObject(globalPropertyId))
                    .setPropertyDefinitionTime(appObject.getGlobalPropertyDefinitionTime(globalPropertyId))
                    .setPropertyTrackingPolicy(true)
                    .build();

            builder.addGlobalPropertyDefinitinions(propertyDefinitionMapInput);
        }

        for (GlobalPropertyId globalPropertyId : appObject.getGlobalPropertyValues().keySet()) {
            Any propertyValueInput = this.translationEngine
                    .getAnyFromObject(appObject.getGlobalPropertyValue(globalPropertyId).get());

            PropertyValueMapInput.Builder propertyValueMapInputBuilder = PropertyValueMapInput
                    .newBuilder()
                    .setPropertyId(this.translationEngine.getAnyFromObject(globalPropertyId))
                    .setPropertyValue(propertyValueInput)
                    .setPropertyValueTime(appObject.getGlobalPropertyTime(globalPropertyId).get());

            builder.addGlobalPropertyValues(propertyValueMapInputBuilder.build());
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
