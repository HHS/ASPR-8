package gov.hhs.aspr.gcm.translation.plugins.people.translatorSpecs;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.people.input.PeoplePluginDataInput;
import gov.hhs.aspr.gcm.translation.plugins.people.input.PersonIdInput;
import plugins.people.PeoplePluginData;
import plugins.people.support.PersonId;

public class PeoplePluginDataTranslatorSpec
        extends AObjectTranslatorSpec<PeoplePluginDataInput, PeoplePluginData> {

    @Override
    protected PeoplePluginData convertInputObject(PeoplePluginDataInput inputObject) {
        PeoplePluginData.Builder builder = PeoplePluginData.builder();

        for (PersonIdInput personIdInput : inputObject.getPersonIdsList()) {
            PersonId personId = this.translator.convertInputObject(personIdInput);
            builder.addPersonId(personId);
        }

        return builder.build();
    }

    @Override
    protected PeoplePluginDataInput convertAppObject(PeoplePluginData simObject) {
        PeoplePluginDataInput.Builder builder = PeoplePluginDataInput.newBuilder();

        for (PersonId personId : simObject.getPersonIds()) {
            if (personId != null) {
                PersonIdInput personIdInput = this.translator.convertSimObject(personId);
                builder.addPersonIds(personIdInput);
            }
        }

        return builder.build();
    }

    @Override
    public PeoplePluginDataInput getDefaultInstanceForInputObject() {
        return PeoplePluginDataInput.getDefaultInstance();
    }

    @Override
    public Class<PeoplePluginData> getAppObjectClass() {
        return PeoplePluginData.class;
    }

    @Override
    public Class<PeoplePluginDataInput> getInputObjectClass() {
        return PeoplePluginDataInput.class;
    }

}
