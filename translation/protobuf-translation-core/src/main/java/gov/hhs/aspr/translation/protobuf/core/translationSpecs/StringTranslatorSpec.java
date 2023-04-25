package gov.hhs.aspr.translation.protobuf.core.translationSpecs;

import com.google.protobuf.StringValue;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;

public class StringTranslatorSpec extends ProtobufTranslationSpec<StringValue, String> {

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
