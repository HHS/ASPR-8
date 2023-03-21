package gov.hhs.aspr.gcm.translation.plugins.personproperties.translatorSpecs;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.personproperties.input.PersonPropertyIdInput;
import plugins.personproperties.support.PersonPropertyId;

public class PersonPropertyIdTranslatorSpec extends AObjectTranslatorSpec<PersonPropertyIdInput, PersonPropertyId> {

    @Override
    protected PersonPropertyId convertInputObject(PersonPropertyIdInput inputObject) {
        return this.translator.getObjectFromAny(inputObject.getId(), getAppObjectClass());
    }

    @Override
    protected PersonPropertyIdInput convertAppObject(PersonPropertyId simObject) {
        return PersonPropertyIdInput.newBuilder().setId(this.translator.getAnyFromObject(simObject))
                .build();
    }

    @Override
    public PersonPropertyIdInput getDefaultInstanceForInputObject() {
        return PersonPropertyIdInput.getDefaultInstance();
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
