package gov.hhs.aspr.gcm.translation.protobuf.plugins.people.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractProtobufTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.input.PersonRangeInput;
import plugins.people.support.PersonRange;

public class PersonRangeTranslatorSpec extends AbstractProtobufTranslatorSpec<PersonRangeInput, PersonRange> {

    @Override
    protected PersonRange convertInputObject(PersonRangeInput inputObject) {
      return new PersonRange(inputObject.getLowPersonId(), inputObject.getHighPersonId());
    }

    @Override
    protected PersonRangeInput convertAppObject(PersonRange simObject) {
       return PersonRangeInput.newBuilder().setLowPersonId(simObject.getLowPersonId()).setHighPersonId(simObject.getHighPersonId()).build();
    }

    @Override
    public PersonRangeInput getDefaultInstanceForInputObject() {
      return PersonRangeInput.getDefaultInstance();
    }

    @Override
    public Class<PersonRange> getAppObjectClass() {
       return PersonRange.class;
    }

    @Override
    public Class<PersonRangeInput> getInputObjectClass() {
       return PersonRangeInput.class;
    }
    
}
