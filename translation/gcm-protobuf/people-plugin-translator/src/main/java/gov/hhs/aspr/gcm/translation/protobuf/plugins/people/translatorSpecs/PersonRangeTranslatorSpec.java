package gov.hhs.aspr.gcm.translation.protobuf.plugins.people.translatorSpecs;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.input.PersonRangeInput;
import plugins.people.support.PersonRange;

public class PersonRangeTranslatorSpec extends ProtobufTranslatorSpec<PersonRangeInput, PersonRange> {

    @Override
    protected PersonRange convertInputObject(PersonRangeInput inputObject) {
      return new PersonRange(inputObject.getLowPersonId(), inputObject.getHighPersonId());
    }

    @Override
    protected PersonRangeInput convertAppObject(PersonRange simObject) {
       return PersonRangeInput.newBuilder().setLowPersonId(simObject.getLowPersonId()).setHighPersonId(simObject.getHighPersonId()).build();
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
