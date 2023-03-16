package lesson.translatorSpecs;

import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.ProtocolMessageEnum;

import gov.hhs.aspr.gcm.translation.core.AEnumTranslatorSpec;
import lesson.input.PersonPropertyInput;
import lesson.plugins.model.PersonProperty;

public class PersonPropertyTranslatorSpec extends AEnumTranslatorSpec<PersonPropertyInput, PersonProperty> {

    @Override
    protected PersonProperty convertInputObject(PersonPropertyInput inputObject) {
        return PersonProperty.valueOf(inputObject.name());
    }

    @Override
    protected PersonPropertyInput convertSimObject(PersonProperty simObject) {
        return PersonPropertyInput.valueOf(simObject.name());
    }

    @Override
    public EnumDescriptor getDescriptorForInputObject() {
        return PersonPropertyInput.getDescriptor();
    }

    @Override
    public EnumInstance getEnumInstance() {
        return new EnumInstance() {

            @Override
            public ProtocolMessageEnum getFromString(String arg0) {
                return PersonPropertyInput.valueOf(arg0);
            }

        };
    }

    @Override
    public Class<PersonPropertyInput> getInputObjectClass() {
        return PersonPropertyInput.class;
    }

    @Override
    public Class<PersonProperty> getSimObjectClass() {
        return 
        PersonProperty.class;
    }

}
