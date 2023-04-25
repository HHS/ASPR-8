package gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyDefinitionInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyDefinitionMapInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.simobjects.PropertyDefinitionMap;

public class PropertyDefinitionMapTranslationSpec
        extends ProtobufTranslationSpec<PropertyDefinitionMapInput, PropertyDefinitionMap> {

    @Override
    protected PropertyDefinitionMap convertInputObject(PropertyDefinitionMapInput inputObject) {
        PropertyDefinitionMap propertyDefinitionMap = new PropertyDefinitionMap();

        propertyDefinitionMap.setPropertyId(this.translationEnine.getObjectFromAny(inputObject.getPropertyId()));
        propertyDefinitionMap
                .setPropertyDefinition(this.translationEnine.convertObject(inputObject.getPropertyDefinition()));

        return propertyDefinitionMap;
    }

    @Override
    protected PropertyDefinitionMapInput convertAppObject(PropertyDefinitionMap appObject) {
        PropertyDefinitionMapInput.Builder builder = PropertyDefinitionMapInput.newBuilder();

        builder.setPropertyId(this.translationEnine.getAnyFromObject(appObject.getPropertyId()));

        PropertyDefinitionInput definitionInput = this.translationEnine.convertObject(appObject.getPropertyDefinition());
        builder.setPropertyDefinition(definitionInput);

        return builder.build();
    }

    @Override
    public Class<PropertyDefinitionMap> getAppObjectClass() {
        return PropertyDefinitionMap.class;
    }

    @Override
    public Class<PropertyDefinitionMapInput> getInputObjectClass() {
        return PropertyDefinitionMapInput.class;
    }

}