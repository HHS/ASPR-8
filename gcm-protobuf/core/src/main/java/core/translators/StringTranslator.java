package core.translators;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.StringValue;

import core.AbstractTranslator;

public class StringTranslator extends AbstractTranslator<StringValue, String> {

    @Override
    protected String convertInputObject(StringValue inputObject) {
        return inputObject.getValue();
    }

    @Override
    protected StringValue convertSimObject(String simObject) {
        return StringValue.of(simObject);
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return StringValue.getDescriptor();
    }

    @Override
    public StringValue getDefaultInstanceForInputObject() {
        return StringValue.getDefaultInstance();
    }

    @Override
    public Class<String> getSimObjectClass() {
        return String.class;
    }

    @Override
    public Class<StringValue> getInputObjectClass() {
        return StringValue.class;
    }
}
