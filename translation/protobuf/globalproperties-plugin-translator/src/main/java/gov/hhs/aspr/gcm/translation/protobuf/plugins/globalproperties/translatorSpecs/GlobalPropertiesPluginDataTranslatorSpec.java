package gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractProtobufTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.input.GlobalPropertiesPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyDefinitionInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyDefinitionMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyValueMapInput;
import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.util.properties.PropertyDefinition;

public class GlobalPropertiesPluginDataTranslatorSpec
        extends AbstractProtobufTranslatorSpec<GlobalPropertiesPluginDataInput, GlobalPropertiesPluginData> {

    @Override
    protected GlobalPropertiesPluginData convertInputObject(GlobalPropertiesPluginDataInput inputObject) {
        GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();

        for (PropertyDefinitionMapInput propertyDefinitionMapInput : inputObject.getGlobalPropertyDefinitinionsList()) {
            GlobalPropertyId propertyId = this.translator.getObjectFromAny(propertyDefinitionMapInput.getPropertyId(),
                    GlobalPropertyId.class);
            PropertyDefinition propertyDefinition = this.translator
                    .convertInputObject(propertyDefinitionMapInput.getPropertyDefinition());

            builder.defineGlobalProperty(propertyId, propertyDefinition, propertyDefinitionMapInput.getTime());
        }

        for (PropertyValueMapInput propertyValueMapInput : inputObject.getGlobalPropertyValuesList()) {

            GlobalPropertyId propertyId = this.translator.getObjectFromAny(propertyValueMapInput.getPropertyId(),
                    GlobalPropertyId.class);
            Object value = this.translator.getObjectFromAny(propertyValueMapInput.getPropertyValue());

            builder.setGlobalPropertyValue(propertyId, value, propertyValueMapInput.getTime());
        }

        return builder.build();
    }

    @Override
    protected GlobalPropertiesPluginDataInput convertAppObject(GlobalPropertiesPluginData simObject) {
        GlobalPropertiesPluginDataInput.Builder builder = GlobalPropertiesPluginDataInput.newBuilder();

        for (GlobalPropertyId propertyId : simObject.getGlobalPropertyIds()) {
            PropertyDefinition propertyDefinition = simObject.getGlobalPropertyDefinition(propertyId);
            Object propertyValue = simObject.getGlobalPropertyValue(propertyId);

            PropertyDefinitionInput propertyDefinitionInput = this.translator.convertSimObject(propertyDefinition);

            PropertyDefinitionMapInput propertyDefinitionMapInput = PropertyDefinitionMapInput
                    .newBuilder()
                    .setPropertyDefinition(propertyDefinitionInput)
                    .setPropertyId(this.translator.getAnyFromObject(propertyId, GlobalPropertyId.class))
                    .setTime(simObject.getGlobalPropertyDefinitionTime(propertyId))
                    .build();

            builder.addGlobalPropertyDefinitinions(propertyDefinitionMapInput);

            if (propertyDefinition.getDefaultValue().isEmpty()) {

                PropertyValueMapInput propertyValueMapInput = PropertyValueMapInput
                        .newBuilder()
                        .setPropertyId(this.translator.getAnyFromObject(propertyId, GlobalPropertyId.class))
                        .setPropertyValue(this.translator.getAnyFromObject(propertyValue))
                        .setTime(simObject.getGlobalPropertyTime(propertyId))
                        .build();
                builder.addGlobalPropertyValues(propertyValueMapInput);
            }

        }

        return builder.build();
    }

    @Override
    public GlobalPropertiesPluginDataInput getDefaultInstanceForInputObject() {
        return GlobalPropertiesPluginDataInput.getDefaultInstance();
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
