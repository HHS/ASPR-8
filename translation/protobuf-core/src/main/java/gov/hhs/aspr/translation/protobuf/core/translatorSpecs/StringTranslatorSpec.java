package gov.hhs.aspr.translation.protobuf.core.translatorSpecs;

import com.google.protobuf.StringValue;

import gov.hhs.aspr.translation.protobuf.core.AbstractProtobufTranslatorSpec;

public class StringTranslatorSpec extends AbstractProtobufTranslatorSpec<StringValue, String> {

    @Override
    protected String convertInputObject(StringValue inputObject) {
        return inputObject.getValue();
    }

    @Override
    protected StringValue convertAppObject(String simObject) {
        return StringValue.of(simObject);
    }

    @Override
    public StringValue getDefaultInstanceForInputObject() {
        return StringValue.getDefaultInstance();
    }

    @Override
    public Class<String> getAppObjectClass() {
        return String.class;
    }

    @Override
    public Class<StringValue> getInputObjectClass() {
        return StringValue.class;
    }
}
