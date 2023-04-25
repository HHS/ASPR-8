package gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.input.PersonPropertyIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.personproperties.support.PersonPropertyId;

public class PersonPropertyIdTranslatorSpec extends ProtobufTranslationSpec<PersonPropertyIdInput, PersonPropertyId> {

    @Override
    protected PersonPropertyId convertInputObject(PersonPropertyIdInput inputObject) {
        return this.translatorCore.getObjectFromAny(inputObject.getId());
    }

    @Override
    protected PersonPropertyIdInput convertAppObject(PersonPropertyId appObject) {
        return PersonPropertyIdInput.newBuilder().setId(this.translatorCore.getAnyFromObject(appObject))
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
