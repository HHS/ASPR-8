package gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.translationSpecs;

import com.google.protobuf.Any;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyDefinitionInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.util.properties.PropertyDefinition;

public class PropertyDefinitionTranslationSpec extends ProtobufTranslationSpec<PropertyDefinitionInput, PropertyDefinition> {

    @Override
    protected PropertyDefinition convertInputObject(PropertyDefinitionInput inputObject) {
        PropertyDefinition.Builder builder = PropertyDefinition.builder();

        builder.setPropertyValueMutability(inputObject.getPropertyValuesAreMutable());
        builder.setTimeTrackingPolicy(this.translationEngine.convertObject(inputObject.getTimeTrackingPolicy()));

        if (inputObject.hasDefaultValue()) {
            Object defaultValue = this.translationEngine.convertObject(inputObject.getDefaultValue());
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
    protected PropertyDefinitionInput convertAppObject(PropertyDefinition appObject) {
        PropertyDefinitionInput.Builder builder = PropertyDefinitionInput.newBuilder();
        if (appObject.getDefaultValue().isPresent()) {
            builder.setDefaultValue((Any) this.translationEngine.convertObjectAsUnsafeClass(appObject.getDefaultValue().get(), Any.class));
        } else {
            builder.setType(appObject.getType().getName());
        }
        builder.setPropertyValuesAreMutable(appObject.propertyValuesAreMutable())
                .setTimeTrackingPolicy(this.translationEngine.convertObject(appObject.getTimeTrackingPolicy()));

        return builder.build();
    }

    @Override
    public Class<PropertyDefinition> getAppObjectClass() {
        return PropertyDefinition.class;
    }

    @Override
    public Class<PropertyDefinitionInput> getInputObjectClass() {
        return PropertyDefinitionInput.class;
    }

}
