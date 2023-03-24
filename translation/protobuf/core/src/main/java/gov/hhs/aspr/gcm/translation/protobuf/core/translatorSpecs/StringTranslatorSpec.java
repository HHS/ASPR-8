package gov.hhs.aspr.gcm.translation.protobuf.core.translatorSpecs;

import com.google.protobuf.StringValue;

import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;

public class StringTranslatorSpec extends AbstractTranslatorSpec<StringValue, String> {

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
