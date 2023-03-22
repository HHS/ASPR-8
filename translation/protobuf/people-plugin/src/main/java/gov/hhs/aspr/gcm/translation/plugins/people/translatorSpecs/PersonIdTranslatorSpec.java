package gov.hhs.aspr.gcm.translation.plugins.people.translatorSpecs;

import gov.hhs.aspr.gcm.translation.core.AbstractTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.people.input.PersonIdInput;
import plugins.people.support.PersonId;

public class PersonIdTranslatorSpec extends AbstractTranslatorSpec<PersonIdInput, PersonId> {

    @Override
    protected PersonId convertInputObject(PersonIdInput inputObject) {
        return new PersonId(inputObject.getId());
    }

    @Override
    protected PersonIdInput convertAppObject(PersonId simObject) {
        return PersonIdInput.newBuilder().setId(simObject.getValue()).build();
    }

    @Override
    public PersonIdInput getDefaultInstanceForInputObject() {
        return PersonIdInput.getDefaultInstance();
    }

    @Override
    public Class<PersonId> getAppObjectClass() {
        return PersonId.class;
    }

    @Override
    public Class<PersonIdInput> getInputObjectClass() {
        return PersonIdInput.class;
    }

}
