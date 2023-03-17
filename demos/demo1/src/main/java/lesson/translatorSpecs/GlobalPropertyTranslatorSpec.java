package lesson.translatorSpecs;

import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.ProtocolMessageEnum;

import gov.hhs.aspr.gcm.translation.core.AEnumTranslatorSpec;
import lesson.input.GlobalPropertyInput;
import lesson.plugins.model.GlobalProperty;

public class GlobalPropertyTranslatorSpec extends AEnumTranslatorSpec<GlobalPropertyInput, GlobalProperty> {

    @Override
    protected GlobalProperty convertInputObject(GlobalPropertyInput inputObject) {
        return GlobalProperty.valueOf(inputObject.name());
    }

    @Override
    protected GlobalPropertyInput convertSimObject(GlobalProperty simObject) {
        return GlobalPropertyInput.valueOf(simObject.name());
    }

    @Override
    public EnumDescriptor getDescriptorForInputObject() {
        return GlobalPropertyInput.getDescriptor();
    }

    @Override
    public EnumInstance getEnumInstance() {
        return new EnumInstance() {

            @Override
            public ProtocolMessageEnum getFromString(String arg0) {
                return GlobalPropertyInput.valueOf(arg0);
            }

        };
    }

    @Override
    public Class<GlobalPropertyInput> getInputObjectClass() {
        return GlobalPropertyInput.class;
    }

    @Override
    public Class<GlobalProperty> getSimObjectClass() {
        return GlobalProperty.class;
    }

}
