package gov.hhs.aspr.gcm.translation.plugins.people.translators;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.people.input.PersonIdInput;
import plugins.people.support.PersonId;

public class PersonIdTranslator extends AObjectTranslatorSpec<PersonIdInput, PersonId> {

    @Override
    protected PersonId convertInputObject(PersonIdInput inputObject) {
        return new PersonId(inputObject.getId());
    }

    @Override
    protected PersonIdInput convertSimObject(PersonId simObject) {
        return PersonIdInput.newBuilder().setId(simObject.getValue()).build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return PersonIdInput.getDescriptor();
    }

    @Override
    public PersonIdInput getDefaultInstanceForInputObject() {
        return PersonIdInput.getDefaultInstance();
    }

    @Override
    public Class<PersonId> getSimObjectClass() {
        return PersonId.class;
    }

    @Override
    public Class<PersonIdInput> getInputObjectClass() {
        return PersonIdInput.class;
    }

}
