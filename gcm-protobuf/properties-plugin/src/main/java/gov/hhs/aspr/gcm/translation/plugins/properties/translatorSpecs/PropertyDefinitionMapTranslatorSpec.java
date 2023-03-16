package gov.hhs.aspr.gcm.translation.plugins.properties.translatorSpecs;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.properties.simobjects.PropertyDefinitionMap;
import gov.hhs.aspr.gcm.translation.plugins.properties.input.PropertyDefinitionInput;
import gov.hhs.aspr.gcm.translation.plugins.properties.input.PropertyDefinitionMapInput;

public class PropertyDefinitionMapTranslatorSpec
        extends AObjectTranslatorSpec<PropertyDefinitionMapInput, PropertyDefinitionMap> {

    @Override
    protected PropertyDefinitionMap convertInputObject(PropertyDefinitionMapInput inputObject) {
        PropertyDefinitionMap propertyDefinitionMap = new PropertyDefinitionMap();

        propertyDefinitionMap.setPropertyId(this.translator.getObjectFromAny(inputObject.getPropertyId()));
        propertyDefinitionMap
                .setPropertyDefinition(this.translator.convertInputObject(inputObject.getPropertyDefinition()));

        return propertyDefinitionMap;
    }

    @Override
    protected PropertyDefinitionMapInput convertSimObject(PropertyDefinitionMap simObject) {
        PropertyDefinitionMapInput.Builder builder = PropertyDefinitionMapInput.newBuilder();

        builder.setPropertyId(this.translator.getAnyFromObject(simObject.getPropertyId()));

        PropertyDefinitionInput definitionInput = this.translator.convertSimObject(simObject.getPropertyDefinition());
        builder.setPropertyDefinition(definitionInput);

        return builder.build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return PropertyDefinitionMapInput.getDescriptor();
    }

    @Override
    public PropertyDefinitionMapInput getDefaultInstanceForInputObject() {
        return PropertyDefinitionMapInput.getDefaultInstance();
    }

    @Override
    public Class<PropertyDefinitionMap> getSimObjectClass() {
        return PropertyDefinitionMap.class;
    }

    @Override
    public Class<PropertyDefinitionMapInput> getInputObjectClass() {
        return PropertyDefinitionMapInput.class;
    }

}
