package gov.hhs.aspr.ms.taskit.protobuf.translationSpecs;

import com.google.protobuf.StringValue;

import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;

/**
 * TranslationSpec that defines how to convert from any Java String to a
 * Protobuf {@link StringValue} type and vice versa
 */
public class StringTranslationSpec extends ProtobufTranslationSpec<StringValue, String> {

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
