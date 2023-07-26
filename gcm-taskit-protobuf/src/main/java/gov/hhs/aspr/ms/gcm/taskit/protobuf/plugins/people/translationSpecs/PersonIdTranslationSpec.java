package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.support.input.PersonIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.people.support.PersonId;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain PersonIdInput} and
 * {@linkplain PersonId}
 */
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
