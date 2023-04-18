package gov.hhs.aspr.gcm.translation.protobuf.plugins.people.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.input.PersonIdInput;
import gov.hhs.aspr.translation.protobuf.core.AbstractProtobufTranslatorSpec;
import plugins.people.support.PersonId;

public class PersonIdTranslatorSpec extends AbstractProtobufTranslatorSpec<PersonIdInput, PersonId> {

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
