package gov.hhs.aspr.translation.protobuf.core.translatorSpecs;

import com.google.protobuf.StringValue;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorSpec;

public class StringTranslatorSpec extends ProtobufTranslatorSpec<StringValue, String> {

    @Override
    protected String convertInputObject(StringValue inputObject) {
        return inputObject.getValue();
    }

    @Override
    protected StringValue convertAppObject(String appObject) {
        return StringValue.of(appObject);
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
