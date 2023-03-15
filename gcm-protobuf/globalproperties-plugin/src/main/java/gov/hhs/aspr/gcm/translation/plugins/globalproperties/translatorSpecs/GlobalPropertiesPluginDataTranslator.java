package gov.hhs.aspr.gcm.translation.plugins.globalproperties.translatorSpecs;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;
import plugins.globalproperties.GlobalPropertiesPluginData;
import gov.hhs.aspr.gcm.translation.plugins.globalproperties.input.GlobalPropertiesPluginDataInput;
import plugins.globalproperties.support.GlobalPropertyId;
import gov.hhs.aspr.gcm.translation.plugins.properties.input.PropertyDefinitionInput;
import gov.hhs.aspr.gcm.translation.plugins.properties.input.PropertyDefinitionMapInput;
import gov.hhs.aspr.gcm.translation.plugins.properties.input.PropertyValueMapInput;
import plugins.util.properties.PropertyDefinition;

public class GlobalPropertiesPluginDataTranslator
        extends AObjectTranslatorSpec<GlobalPropertiesPluginDataInput, GlobalPropertiesPluginData> {

    @Override
    protected GlobalPropertiesPluginData convertInputObject(GlobalPropertiesPluginDataInput inputObject) {
        GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();

        for (PropertyDefinitionMapInput propertyDefinitionMapInput : inputObject.getGlobalPropertyDefinitinionsList()) {
            GlobalPropertyId propertyId = this.translator.getObjectFromAny(propertyDefinitionMapInput.getPropertyId(),
                    GlobalPropertyId.class);
            PropertyDefinition propertyDefinition = this.translator
                    .convertInputObject(propertyDefinitionMapInput.getPropertyDefinition());

            builder.defineGlobalProperty(propertyId, propertyDefinition);
        }

        for (PropertyValueMapInput propertyValueMapInput : inputObject.getGlobalPropertyValuesList()) {

            GlobalPropertyId propertyId = this.translator.getObjectFromAny(propertyValueMapInput.getPropertyId(),
                    GlobalPropertyId.class);
            Object value = this.translator.getObjectFromAny(propertyValueMapInput.getPropertyValue());

            builder.setGlobalPropertyValue(propertyId, value);
        }

        return builder.build();
    }

    @Override
    protected GlobalPropertiesPluginDataInput convertSimObject(GlobalPropertiesPluginData simObject) {
        GlobalPropertiesPluginDataInput.Builder builder = GlobalPropertiesPluginDataInput.newBuilder();

        for (GlobalPropertyId propertyId : simObject.getGlobalPropertyIds()) {
            PropertyDefinition propertyDefinition = simObject.getGlobalPropertyDefinition(propertyId);
            Object propertyValue = simObject.getGlobalPropertyValue(propertyId);

            PropertyDefinitionInput propertyDefinitionInput = this.translator.convertSimObject(propertyDefinition);

            PropertyDefinitionMapInput propertyDefinitionMapInput = PropertyDefinitionMapInput
                    .newBuilder()
                    .setPropertyDefinition(propertyDefinitionInput)
                    .setPropertyId(this.translator.getAnyFromObject(propertyId, GlobalPropertyId.class))
                    .build();

            builder.addGlobalPropertyDefinitinions(propertyDefinitionMapInput);

            if (propertyDefinition.getDefaultValue().isEmpty()) {

                PropertyValueMapInput propertyValueMapInput = PropertyValueMapInput
                        .newBuilder()
                        .setPropertyId(this.translator.getAnyFromObject(propertyId, GlobalPropertyId.class))
                        .setPropertyValue(this.translator.getAnyFromObject(propertyValue))
                        .build();
                builder.addGlobalPropertyValues(propertyValueMapInput);
            }

        }

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