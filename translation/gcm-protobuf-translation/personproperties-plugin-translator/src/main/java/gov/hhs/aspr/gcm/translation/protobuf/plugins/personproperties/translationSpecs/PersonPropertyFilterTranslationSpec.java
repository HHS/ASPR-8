package gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translationSpecs;

import com.google.protobuf.Any;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.input.EqualityInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.input.PersonPropertyFilterInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.input.PersonPropertyIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.partitions.support.Equality;
import plugins.personproperties.support.PersonPropertyFilter;
import plugins.personproperties.support.PersonPropertyId;

public class PersonPropertyFilterTranslationSpec
        extends ProtobufTranslationSpec<PersonPropertyFilterInput, PersonPropertyFilter> {

    @Override
    protected PersonPropertyFilter convertInputObject(PersonPropertyFilterInput inputObject) {
        PersonPropertyId personPropertyId = this.translationEngine.convertObject(inputObject.getPersonPropertyId());
        Equality equality = this.translationEngine.convertObject(inputObject.getEquality());
        Object personPropertyValue = this.translationEngine.getObjectFromAny(inputObject.getPersonPropertyValue());

        return new PersonPropertyFilter(personPropertyId, equality, personPropertyValue);
    }

    @Override
    protected PersonPropertyFilterInput convertAppObject(PersonPropertyFilter appObject) {
        PersonPropertyIdInput personPropertyIdInput = this.translationEngine
                .convertObjectAsSafeClass(appObject.getPersonPropertyId(), PersonPropertyId.class);
        EqualityInput equalityInput = this.translationEngine.convertObjectAsSafeClass(appObject.getEquality(), Equality.class);
        Any personPropertyValue = this.translationEngine.getAnyFromObject(appObject.getPersonPropertyValue());

        return PersonPropertyFilterInput.newBuilder()
                .setPersonPropertyId(personPropertyIdInput)
                .setEquality(equalityInput)
                .setPersonPropertyValue(personPropertyValue)
                .build();
    }

    @Override
    public Class<PersonPropertyFilter> getAppObjectClass() {
        return PersonPropertyFilter.class;
    }

    @Override
    public Class<PersonPropertyFilterInput> getInputObjectClass() {
        return PersonPropertyFilterInput.class;
    }

}
