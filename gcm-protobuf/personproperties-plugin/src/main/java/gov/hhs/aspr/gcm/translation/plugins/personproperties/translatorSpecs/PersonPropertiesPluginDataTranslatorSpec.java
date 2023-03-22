package gov.hhs.aspr.gcm.translation.plugins.personproperties.translatorSpecs;

import java.util.List;

import gov.hhs.aspr.gcm.translation.core.AbstractTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.people.input.PersonIdInput;
import gov.hhs.aspr.gcm.translation.plugins.personproperties.input.PersonPropertiesPluginDataInput;
import gov.hhs.aspr.gcm.translation.plugins.personproperties.input.PersonPropertyValueMapInput;
import gov.hhs.aspr.gcm.translation.plugins.properties.input.PropertyDefinitionInput;
import gov.hhs.aspr.gcm.translation.plugins.properties.input.PropertyDefinitionMapInput;
import gov.hhs.aspr.gcm.translation.plugins.properties.input.PropertyValueMapInput;
import plugins.people.support.PersonId;
import plugins.personproperties.PersonPropertiesPluginData;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.support.PersonPropertyInitialization;
import plugins.util.properties.PropertyDefinition;

public class PersonPropertiesPluginDataTranslatorSpec
        extends AbstractTranslatorSpec<PersonPropertiesPluginDataInput, PersonPropertiesPluginData> {

    @Override
    protected PersonPropertiesPluginData convertInputObject(PersonPropertiesPluginDataInput inputObject) {
        PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();

        for (PropertyDefinitionMapInput propertyDefinitionMapInput : inputObject.getPersonPropertyDefinitionsList()) {
            PersonPropertyId propertyId = this.translator.getObjectFromAny(propertyDefinitionMapInput.getPropertyId(),
                    PersonPropertyId.class);
            PropertyDefinition propertyDefinition = this.translator
                    .convertInputObject(propertyDefinitionMapInput.getPropertyDefinition());

            builder.definePersonProperty(propertyId, propertyDefinition);
        }

        for (PersonPropertyValueMapInput personPropertyValueMapInput : inputObject.getPersonPropertyValuesList()) {

            PersonId personId = this.translator.convertInputObject(personPropertyValueMapInput.getPersonId());
            for (PropertyValueMapInput propertyValueMapInput : personPropertyValueMapInput.getPropertyValueMapList()) {
                PersonPropertyId propertyId = this.translator.getObjectFromAny(propertyValueMapInput.getPropertyId(),
                        PersonPropertyId.class);
                Object value = this.translator.getObjectFromAny(propertyValueMapInput.getPropertyValue());

                builder.setPersonPropertyValue(personId, propertyId, value);
            }

        }

        return builder.build();
    }

    @Override
    protected PersonPropertiesPluginDataInput convertAppObject(PersonPropertiesPluginData simObject) {
        PersonPropertiesPluginDataInput.Builder builder = PersonPropertiesPluginDataInput.newBuilder();

        for (PersonPropertyId propertyId : simObject.getPersonPropertyIds()) {
            PropertyDefinition propertyDefinition = simObject.getPersonPropertyDefinition(propertyId);

            PropertyDefinitionInput propertyDefinitionInput = this.translator.convertSimObject(propertyDefinition);

            PropertyDefinitionMapInput propertyDefinitionMapInput = PropertyDefinitionMapInput
                    .newBuilder()
                    .setPropertyDefinition(propertyDefinitionInput)
                    .setPropertyId(this.translator.getAnyFromObject(propertyId, PersonPropertyId.class))
                    .build();

            builder.addPersonPropertyDefinitions(propertyDefinitionMapInput);
        }

        for (int i = 0; i < simObject.getPersonCount(); i++) {
            List<PersonPropertyInitialization> personPropertiesValues = simObject.getPropertyValues(i);
            if (!personPropertiesValues.isEmpty()) {
                PersonIdInput personIdInput = this.translator.convertSimObject(new PersonId(i));
                PersonPropertyValueMapInput.Builder personPropertyValueMapBuilder = PersonPropertyValueMapInput
                        .newBuilder().setPersonId(personIdInput);

                for (PersonPropertyInitialization personPropertyInitialization : personPropertiesValues) {
                    PropertyValueMapInput propertyValueMapInput = PropertyValueMapInput.newBuilder()
                            .setPropertyValue(this.translator.getAnyFromObject(
                                    personPropertyInitialization.getValue()))
                            .setPropertyId(
                                    this.translator.getAnyFromObject(personPropertyInitialization.getPersonPropertyId(),
                                            PersonPropertyId.class))
                            .build();

                    personPropertyValueMapBuilder.addPropertyValueMap(propertyValueMapInput);
                }

                builder.addPersonPropertyValues(personPropertyValueMapBuilder.build());
            }
        }

        return builder.build();
    }

    @Override
    public PersonPropertiesPluginDataInput getDefaultInstanceForInputObject() {
        return PersonPropertiesPluginDataInput.getDefaultInstance();
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
