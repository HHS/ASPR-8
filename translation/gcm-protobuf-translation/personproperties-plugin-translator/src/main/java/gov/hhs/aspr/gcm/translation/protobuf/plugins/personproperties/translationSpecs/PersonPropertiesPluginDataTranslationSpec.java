package gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translationSpecs;

import java.util.List;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.input.PersonIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.input.PersonPropertiesPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.input.PersonPropertyValueMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyDefinitionInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyDefinitionMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyValueMapInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.people.support.PersonId;
import plugins.personproperties.PersonPropertiesPluginData;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.support.PersonPropertyInitialization;
import plugins.util.properties.PropertyDefinition;

public class PersonPropertiesPluginDataTranslationSpec
        extends ProtobufTranslationSpec<PersonPropertiesPluginDataInput, PersonPropertiesPluginData> {

    @Override
    protected PersonPropertiesPluginData convertInputObject(PersonPropertiesPluginDataInput inputObject) {
        PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();

        for (PropertyDefinitionMapInput propertyDefinitionMapInput : inputObject.getPersonPropertyDefinitionsList()) {
            PersonPropertyId propertyId = this.translationEnine.getObjectFromAny(propertyDefinitionMapInput.getPropertyId());
            PropertyDefinition propertyDefinition = this.translationEnine
                    .convertObject(propertyDefinitionMapInput.getPropertyDefinition());

            builder.definePersonProperty(propertyId, propertyDefinition);
        }

        for (PersonPropertyValueMapInput personPropertyValueMapInput : inputObject.getPersonPropertyValuesList()) {

            PersonId personId = this.translationEnine.convertObject(personPropertyValueMapInput.getPersonId());
            builder.addPerson(personId);
            
            for (PropertyValueMapInput propertyValueMapInput : personPropertyValueMapInput.getPropertyValueMapList()) {
                PersonPropertyId propertyId = this.translationEnine.getObjectFromAny(propertyValueMapInput.getPropertyId());
                Object value = this.translationEnine.getObjectFromAny(propertyValueMapInput.getPropertyValue());

                builder.setPersonPropertyValue(personId, propertyId, value);
            }

        }

        return builder.build();
    }

    @Override
    protected PersonPropertiesPluginDataInput convertAppObject(PersonPropertiesPluginData appObject) {
        PersonPropertiesPluginDataInput.Builder builder = PersonPropertiesPluginDataInput.newBuilder();

        for (PersonPropertyId propertyId : appObject.getPersonPropertyIds()) {
            PropertyDefinition propertyDefinition = appObject.getPersonPropertyDefinition(propertyId);

            PropertyDefinitionInput propertyDefinitionInput = this.translationEnine.convertObject(propertyDefinition);

            PropertyDefinitionMapInput propertyDefinitionMapInput = PropertyDefinitionMapInput
                    .newBuilder()
                    .setPropertyDefinition(propertyDefinitionInput)
                    .setPropertyId(this.translationEnine.getAnyFromObject(propertyId))
                    .build();

            builder.addPersonPropertyDefinitions(propertyDefinitionMapInput);
        }

        for (int i = 0; i <= appObject.getMaxPersonIndex(); i++) {
            if (appObject.personExists(i)) {
                List<PersonPropertyInitialization> personPropertiesValues = appObject.getPropertyValues(i);
                PersonIdInput personIdInput = this.translationEnine.convertObject(new PersonId(i));
                PersonPropertyValueMapInput.Builder personPropertyValueMapBuilder = PersonPropertyValueMapInput
                        .newBuilder().setPersonId(personIdInput);

                for (PersonPropertyInitialization personPropertyInitialization : personPropertiesValues) {
                    PropertyValueMapInput propertyValueMapInput = PropertyValueMapInput.newBuilder()
                            .setPropertyValue(this.translationEnine.getAnyFromObject(
                                    personPropertyInitialization.getValue()))
                            .setPropertyId(
                                    this.translationEnine.getAnyFromObject(personPropertyInitialization.getPersonPropertyId()))
                            .build();

                    personPropertyValueMapBuilder.addPropertyValueMap(propertyValueMapInput);
                }

                builder.addPersonPropertyValues(personPropertyValueMapBuilder.build());
            }
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