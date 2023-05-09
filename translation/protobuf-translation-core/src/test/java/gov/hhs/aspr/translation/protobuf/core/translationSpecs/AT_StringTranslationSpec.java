package gov.hhs.aspr.translation.protobuf.core.translationSpecs;

import com.google.protobuf.StringValue;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;

/**
 * TranslationSpec that defines how to convert from any Java String to a
 * Protobuf {@link StringValue} type and vice versa
 */
public class AT_StringTranslationSpec extends ProtobufTranslationSpec<StringValue, String> {

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
