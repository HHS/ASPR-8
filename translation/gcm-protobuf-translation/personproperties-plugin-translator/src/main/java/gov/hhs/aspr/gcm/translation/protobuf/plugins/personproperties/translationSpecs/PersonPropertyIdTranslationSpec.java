package gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.input.PersonPropertyIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.personproperties.support.PersonPropertyId;

public class PersonPropertyIdTranslationSpec extends ProtobufTranslationSpec<PersonPropertyIdInput, PersonPropertyId> {

    @Override
    protected PersonPropertyId convertInputObject(PersonPropertyIdInput inputObject) {
        return this.translationEnine.getObjectFromAny(inputObject.getId());
    }

    @Override
    protected PersonPropertyIdInput convertAppObject(PersonPropertyId appObject) {
        return PersonPropertyIdInput.newBuilder().setId(this.translationEnine.getAnyFromObject(appObject))
                .build();
    }

    @Override
    public Class<PersonPropertyId> getAppObjectClass() {
        return PersonPropertyId.class;
    }

    @Override
    public Class<PersonPropertyIdInput> getInputObjectClass() {
        return PersonPropertyIdInput.class;
    }

}