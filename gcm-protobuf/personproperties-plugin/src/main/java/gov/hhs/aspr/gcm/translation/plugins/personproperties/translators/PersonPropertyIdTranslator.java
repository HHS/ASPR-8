package gov.hhs.aspr.gcm.translation.plugins.personproperties.translators;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.translation.core.AbstractTranslator;
import gov.hhs.aspr.gcm.translation.plugins.personproperties.input.PersonPropertyIdInput;
import plugins.personproperties.support.PersonPropertyId;

public class PersonPropertyIdTranslator extends AbstractTranslator<PersonPropertyIdInput, PersonPropertyId> {

    @Override
    protected PersonPropertyId convertInputObject(PersonPropertyIdInput inputObject) {
        return this.translator.getObjectFromAny(inputObject.getId(), getSimObjectClass());
    }

    @Override
    protected PersonPropertyIdInput convertSimObject(PersonPropertyId simObject) {
        return PersonPropertyIdInput.newBuilder().setId(this.translator.getAnyFromObject(simObject))
                .build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return PersonPropertyIdInput.getDescriptor();
    }

    @Override
    public PersonPropertyIdInput getDefaultInstanceForInputObject() {
        return PersonPropertyIdInput.getDefaultInstance();
    }

    @Override
    public Class<PersonPropertyId> getSimObjectClass() {
        return PersonPropertyId.class;
    }

    @Override
    public Class<PersonPropertyIdInput> getInputObjectClass() {
        return PersonPropertyIdInput.class;
    }

}
