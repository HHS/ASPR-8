package gov.hhs.aspr.gcm.translation.protobuf.plugins.people.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.input.PersonIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.people.support.PersonId;

public class PersonIdTranslationSpec extends ProtobufTranslationSpec<PersonIdInput, PersonId> {

    @Override
    protected PersonId convertInputObject(PersonIdInput inputObject) {
        return new PersonId(inputObject.getId());
    }

    @Override
    protected PersonIdInput convertAppObject(PersonId appObject) {
        return PersonIdInput.newBuilder().setId(appObject.getValue()).build();
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