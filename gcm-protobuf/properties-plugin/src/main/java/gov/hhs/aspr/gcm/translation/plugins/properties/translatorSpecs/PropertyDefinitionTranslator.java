package gov.hhs.aspr.gcm.translation.plugins.properties.translatorSpecs;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.properties.input.PropertyDefinitionInput;
import plugins.util.properties.PropertyDefinition;

public class PropertyDefinitionTranslator extends AObjectTranslatorSpec<PropertyDefinitionInput, PropertyDefinition> {

    @Override
    protected PropertyDefinition convertInputObject(PropertyDefinitionInput inputObject) {
        PropertyDefinition.Builder builder = PropertyDefinition.builder();

        builder.setPropertyValueMutability(inputObject.getPropertyValuesAreMutable());
        builder.setTimeTrackingPolicy(this.translator.convertInputEnum(inputObject.getTimeTrackingPolicy()));

        if (inputObject.hasDefaultValue()) {
            Object defaultValue = this.translator.getObjectFromAny(inputObject.getDefaultValue());
            builder.setDefaultValue(defaultValue);
            builder.setType(defaultValue.getClass());
        } else {
            String type = inputObject.getType();
            Class<?> classType;
            try {
                classType = Class.forName(type);
                builder.setType(classType);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException("Unable to find class for type: " + type);
            }
        }

        return builder.build();
    }

    @Override
    protected PropertyDefinitionInput convertSimObject(PropertyDefinition simObject) {
        PropertyDefinitionInput.Builder builder = PropertyDefinitionInput.newBuilder();
        if (simObject.getDefaultValue().isPresent()) {
            builder.setDefaultValue(this.translator.getAnyFromObject(simObject.getDefaultValue().get()));
        } else {
            builder.setType(simObject.getType().getName());
        }
        builder.setPropertyValuesAreMutable(simObject.propertyValuesAreMutable())
                .setTimeTrackingPolicy(this.translator.convertSimObject(simObject.getTimeTrackingPolicy()));

        return builder.build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return PropertyDefinitionInput.getDescriptor();
    }

    @Override
    public PropertyDefinitionInput getDefaultInstanceForInputObject() {
        return PropertyDefinitionInput.getDefaultInstance();
    }

    @Override
    public Class<PropertyDefinition> getSimObjectClass() {
        return PropertyDefinition.class;
    }

    @Override
    public Class<PropertyDefinitionInput> getInputObjectClass() {
        return PropertyDefinitionInput.class;
    }

}
