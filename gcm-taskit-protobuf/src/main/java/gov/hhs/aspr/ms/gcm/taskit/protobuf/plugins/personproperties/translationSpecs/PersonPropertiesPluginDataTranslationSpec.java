package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.personproperties.translationSpecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.personproperties.data.input.PersonPropertiesPluginDataInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.personproperties.support.input.PersonPropertyIdInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.personproperties.support.input.PersonPropertyTimeInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.personproperties.support.input.PersonPropertyTimeMapInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.personproperties.support.input.PersonPropertyValueInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.personproperties.support.input.PersonPropertyValueMapInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionMapInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.datamanagers.PersonPropertiesPluginData;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.support.PersonPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyDefinition;

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
                Object value = this.translationEngine.getObjectFromAny(personPropertyValueInput.getValue());
                builder.setPersonPropertyValue(personId, propertyId, value);
            }
        }

        for (PersonPropertyTimeMapInput personPropertyTimeMapInput : inputObject.getPersonPropertyTimesList()) {
            PersonPropertyId propertyId = this.translationEngine
                    .convertObject(personPropertyTimeMapInput.getPersonPropertyId());
            for (PersonPropertyTimeInput personPropertyTimeInput : personPropertyTimeMapInput
                    .getPropertyTimesList()) {
                PersonId personId = new PersonId(personPropertyTimeInput.getPId());

                builder.setPersonPropertyTime(personId, propertyId,
                        personPropertyTimeInput.getPropertyValueTime());
            }
        }

        return builder.build();
    }

    @Override
    protected PersonPropertiesPluginDataInput convertAppObject(PersonPropertiesPluginData appObject) {
        PersonPropertiesPluginDataInput.Builder builder = PersonPropertiesPluginDataInput.newBuilder();

        Map<PersonPropertyId, PropertyDefinition> personPropertyDefinitions = appObject.getPropertyDefinitions();
        Map<PersonPropertyId, Boolean> personPropertyTimeTrackingPolicies = appObject.getPropertyTrackingPolicies();
        Map<PersonPropertyId, Double> personPropertyDefinitionTimes = appObject.getPropertyDefinitionTimes();
        Map<PersonPropertyId, List<Object>> personPropertyValues = appObject.getPropertyValues();
        Map<PersonPropertyId, List<Double>> personPropertyTimes = appObject.getPropertyTimes();

        // Person Prop Defs
        for (PersonPropertyId personPropertyId : personPropertyDefinitions.keySet()) {
            PropertyDefinition propertyDefinition = appObject.getPersonPropertyDefinition(personPropertyId);

            PropertyDefinitionInput propertyDefinitionInput = this.translationEngine.convertObject(propertyDefinition);
            double propertyDefinitionTime = personPropertyDefinitionTimes.get(personPropertyId);
            boolean propertyTrackingPolicy = personPropertyTimeTrackingPolicies.get(personPropertyId);

            PropertyDefinitionMapInput propertyDefinitionMapInput = PropertyDefinitionMapInput
                    .newBuilder()
                    .setPropertyDefinition(propertyDefinitionInput)
                    .setPropertyId(this.translationEngine.getAnyFromObject(personPropertyId))
                    .setPropertyDefinitionTime(propertyDefinitionTime)
                    .setPropertyTrackingPolicy(propertyTrackingPolicy)
                    .build();

            builder.addPersonPropertyDefinitions(propertyDefinitionMapInput);
        }

        // Person Prop Values
        for (PersonPropertyId personPropertyId : personPropertyValues.keySet()) {
            List<PersonPropertyValueInput.Builder> personPropertyInputBuilders = new ArrayList<>();

            List<Object> propertyValues = personPropertyValues.get(personPropertyId);
            for (int i = 0; i < propertyValues.size(); i++) {
                personPropertyInputBuilders.add(null);
            }

            for (int i = 0; i < propertyValues.size(); i++) {
                if (propertyValues.get(i) != null) {
                    PersonPropertyValueInput.Builder personPropertyValueInputBuilder = PersonPropertyValueInput
                            .newBuilder()
                            .setPId(i)
                            .setValue(this.translationEngine.getAnyFromObject(propertyValues.get(i)));

                    personPropertyInputBuilders.set(i, personPropertyValueInputBuilder);
                }
            }

            PersonPropertyValueMapInput.Builder valueMapInputBuilder = PersonPropertyValueMapInput.newBuilder()
                    .setPersonPropertyId((PersonPropertyIdInput) this.translationEngine
                            .convertObjectAsSafeClass(personPropertyId, PersonPropertyId.class));

            for (PersonPropertyValueInput.Builder personPropertyValueInputBuilder : personPropertyInputBuilders) {
                if (personPropertyValueInputBuilder != null) {
                    valueMapInputBuilder.addPropertyValues(personPropertyValueInputBuilder.build());
                }
            }

            builder.addPersonPropertyValues(valueMapInputBuilder.build());
        }

        // Person Prop Times
        for (PersonPropertyId personPropertyId : personPropertyTimes.keySet()) {
            List<PersonPropertyTimeInput.Builder> personPropertyInputBuilders = new ArrayList<>();

            List<Double> propertyTimes = personPropertyTimes.get(personPropertyId);
            for (int i = 0; i < propertyTimes.size(); i++) {
                personPropertyInputBuilders.add(null);
            }

            for (int i = 0; i < propertyTimes.size(); i++) {
                if (propertyTimes.get(i) != null) {
                    PersonPropertyTimeInput.Builder personPropertyTimeInputBuilder = PersonPropertyTimeInput
                            .newBuilder()
                            .setPId(i)
                            .setPropertyValueTime(propertyTimes.get(i));

                    personPropertyInputBuilders.set(i, personPropertyTimeInputBuilder);
                }
            }

            PersonPropertyTimeMapInput.Builder timeMapInputBuilder = PersonPropertyTimeMapInput.newBuilder()
                    .setPersonPropertyId((PersonPropertyIdInput) this.translationEngine
                            .convertObjectAsSafeClass(personPropertyId, PersonPropertyId.class));

            for (PersonPropertyTimeInput.Builder personPropertyTimeInputBuilder : personPropertyInputBuilders) {
                if (personPropertyTimeInputBuilder != null) {
                    timeMapInputBuilder.addPropertyTimes(personPropertyTimeInputBuilder.build());
                }
            }

            builder.addPersonPropertyTimes(timeMapInputBuilder.build());
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
