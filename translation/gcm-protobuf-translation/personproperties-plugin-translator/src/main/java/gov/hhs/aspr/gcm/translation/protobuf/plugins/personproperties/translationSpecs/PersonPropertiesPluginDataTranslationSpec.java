package gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translationSpecs;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.FastMath;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.input.PersonPropertiesPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.input.PersonPropertyIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.input.PersonPropertyValueInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.input.PersonPropertyValueMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyDefinitionInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyDefinitionMapInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.people.support.PersonId;
import plugins.personproperties.PersonPropertiesPluginData;
import plugins.personproperties.support.PersonPropertyId;
import plugins.util.properties.PropertyDefinition;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain PersonPropertiesPluginDataInput} and
 * {@linkplain PersonPropertiesPluginData}
 */
public class PersonPropertiesPluginDataTranslationSpec
        extends ProtobufTranslationSpec<PersonPropertiesPluginDataInput, PersonPropertiesPluginData> {

    @Override
    protected PersonPropertiesPluginData convertInputObject(PersonPropertiesPluginDataInput inputObject) {
        PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();

        for (PropertyDefinitionMapInput propertyDefinitionMapInput : inputObject.getPersonPropertyDefinitionsList()) {
            PersonPropertyId propertyId = this.translationEngine
                    .getObjectFromAny(propertyDefinitionMapInput.getPropertyId());
            PropertyDefinition propertyDefinition = this.translationEngine
                    .convertObject(propertyDefinitionMapInput.getPropertyDefinition());

            builder.definePersonProperty(propertyId, propertyDefinition,
                    propertyDefinitionMapInput.getPropertyDefinitionTime(),
                    propertyDefinitionMapInput.getPropertyTrackingPolicy());
        }

        for (PersonPropertyValueMapInput personPropertyValueMapInput : inputObject.getPersonPropertyValuesList()) {
            PersonPropertyId propertyId = this.translationEngine
                    .convertObject(personPropertyValueMapInput.getPersonPropertyId());
            for (PersonPropertyValueInput personPropertyValueInput : personPropertyValueMapInput
                    .getPropertyValuesList()) {
                PersonId personId = new PersonId(personPropertyValueInput.getPId());
                if (personPropertyValueInput.hasValue()) {
                    Object value = this.translationEngine.getObjectFromAny(personPropertyValueInput.getValue());

                    builder.setPersonPropertyValue(personId, propertyId, value);
                }

                if (personPropertyValueInput.hasPropertyValueTime()) {
                    builder.setPersonPropertyTime(personId, propertyId,
                            personPropertyValueInput.getPropertyValueTime());
                }
            }
        }

        return builder.build();
    }

    @Override
    protected PersonPropertiesPluginDataInput convertAppObject(PersonPropertiesPluginData appObject) {
        PersonPropertiesPluginDataInput.Builder builder = PersonPropertiesPluginDataInput.newBuilder();

        for (PersonPropertyId propertyId : appObject.getPersonPropertyIds()) {
            PropertyDefinition propertyDefinition = appObject.getPersonPropertyDefinition(propertyId);

            PropertyDefinitionInput propertyDefinitionInput = this.translationEngine.convertObject(propertyDefinition);
            double propertyDefinitionTime = appObject.getPropertyDefinitionTime(propertyId);
            boolean propertyTrackingPolicy = appObject.propertyAssignmentTimesTracked(propertyId);

            PropertyDefinitionMapInput propertyDefinitionMapInput = PropertyDefinitionMapInput
                    .newBuilder()
                    .setPropertyDefinition(propertyDefinitionInput)
                    .setPropertyId(this.translationEngine.getAnyFromObject(propertyId))
                    .setPropertyDefinitionTime(propertyDefinitionTime)
                    .setPropertyTrackingPolicy(propertyTrackingPolicy)
                    .build();

            builder.addPersonPropertyDefinitions(propertyDefinitionMapInput);

            List<PersonPropertyValueInput.Builder> personPropertyValueInputBuilders = new ArrayList<>();

            List<Object> propertyValues = appObject.getPropertyValues(propertyId);
            List<Double> propertyTimes = appObject.getPropertyTimes(propertyId);

            int maxPersonId = FastMath.max(propertyValues.size(), propertyTimes.size());

            // prepopulate nulls based on max personId
            for (int i = 0; i < maxPersonId; i++) {
                personPropertyValueInputBuilders.add(null);
            }

            for (int i = 0; i < propertyValues.size(); i++) {
                if (propertyValues.get(i) != null) {
                    PersonPropertyValueInput.Builder personPropertyValueInputBuilder = PersonPropertyValueInput
                            .newBuilder();

                    personPropertyValueInputBuilder.setPId(i)
                            .setValue(this.translationEngine.getAnyFromObject(propertyValues.get(i)));

                    personPropertyValueInputBuilders.set(i, personPropertyValueInputBuilder);
                }
            }

            if (appObject.propertyAssignmentTimesTracked(propertyId)) {
                for (int i = 0; i < propertyTimes.size(); i++) {
                    if (propertyTimes.get(i) != null) {
                        PersonPropertyValueInput.Builder personPropertyValueInputBuilder = PersonPropertyValueInput
                                .newBuilder();
                        // check for and use existing builder, if there is one
                        if (personPropertyValueInputBuilders.get(i) != null) {
                            personPropertyValueInputBuilder = personPropertyValueInputBuilders.get(i);

                            personPropertyValueInputBuilder.setPropertyValueTime(propertyTimes.get(i));
                        } else {
                            personPropertyValueInputBuilder.setPId(i)
                                    .setPropertyValueTime(propertyTimes.get(i));
                        }

                        personPropertyValueInputBuilders.set(i, personPropertyValueInputBuilder);
                    }
                }
            }

            PersonPropertyValueMapInput.Builder valueMapInputBuilder = PersonPropertyValueMapInput.newBuilder()
                    .setPersonPropertyId((PersonPropertyIdInput) this.translationEngine
                            .convertObjectAsSafeClass(propertyId, PersonPropertyId.class));

            for (PersonPropertyValueInput.Builder personPropertyValueInputBuilder : personPropertyValueInputBuilders) {
                if (personPropertyValueInputBuilder != null) {
                    valueMapInputBuilder.addPropertyValues(personPropertyValueInputBuilder.build());
                }
            }

            builder.addPersonPropertyValues(valueMapInputBuilder.build());

        }

        return builder.build();
    }

    @Override
    public Class<PersonPropertiesPluginData> getAppObjectClass() {
        return PersonPropertiesPluginData.class;
    }

    @Override
    public Class<PersonPropertiesPluginDataInput> getInputObjectClass() {
        return PersonPropertiesPluginDataInput.class;
    }

}
