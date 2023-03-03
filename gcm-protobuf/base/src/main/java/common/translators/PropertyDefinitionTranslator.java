package common.translators;

import com.google.protobuf.Descriptors.Descriptor;

import base.AbstractTranslator;
import common.PropertyDefinitionInput;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.TimeTrackingPolicy;

public class PropertyDefinitionTranslator extends AbstractTranslator<PropertyDefinitionInput, PropertyDefinition> {

    @Override
    protected PropertyDefinition convertInputObject(PropertyDefinitionInput inputObject) {
        PropertyDefinition.Builder builder = PropertyDefinition.builder();

        builder.setPropertyValueMutability(inputObject.getPropertyValuesAreMutable());
        builder.setTimeTrackingPolicy(TimeTrackingPolicy.valueOf(inputObject.getTimeTrackingPolicy().toString()));

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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'convertSimObject'");
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