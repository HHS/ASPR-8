package lesson.translatorSpecs;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import lesson.input.PersonPropertyInput;
import lesson.plugins.model.PersonProperty;

public class PersonPropertyTranslatorSpec extends ProtobufTranslationSpec<PersonPropertyInput, PersonProperty> {

    @Override
    protected PersonProperty convertInputObject(PersonPropertyInput inputObject) {
        return PersonProperty.valueOf(inputObject.name());
    }

    @Override
    protected PersonPropertyInput convertAppObject(PersonProperty simObject) {
        return PersonPropertyInput.valueOf(simObject.name());
    }

    @Override
    public Class<PersonPropertyInput> getInputObjectClass() {
        return PersonPropertyInput.class;
    }

    @Override
    public Class<PersonProperty> getAppObjectClass() {
        return PersonProperty.class;
    }

}
