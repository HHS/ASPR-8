package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.translationSpecs;

import com.google.protobuf.Any;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyDefinition;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain PropertyDefinitionInput} and
 * {@linkplain PropertyDefinition}
 */
public class PropertyDefinitionTranslationSpec
        extends ProtobufTranslationSpec<PropertyDefinitionInput, PropertyDefinition> {

    @Override
    protected PropertyDefinition convertInputObject(PropertyDefinitionInput inputObject) {
        PropertyDefinition.Builder builder = PropertyDefinition.builder();

        builder.setPropertyValueMutability(inputObject.getPropertyValuesAreMutable());

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
                throw new RuntimeException("Unable to find class for type: " + type, e);
            }
        }

        return builder.build();
    }

    @Override
    protected PropertyDefinitionInput convertAppObject(PropertyDefinition appObject) {
        PropertyDefinitionInput.Builder builder = PropertyDefinitionInput.newBuilder();
        if (appObject.getDefaultValue().isPresent()) {
            builder.setDefaultValue((Any) this.translationEngine
                    .convertObjectAsUnsafeClass(appObject.getDefaultValue().get(), Any.class));
        } else {
            builder.setType(appObject.getType().getName());
        }
        builder.setPropertyValuesAreMutable(appObject.propertyValuesAreMutable());

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
