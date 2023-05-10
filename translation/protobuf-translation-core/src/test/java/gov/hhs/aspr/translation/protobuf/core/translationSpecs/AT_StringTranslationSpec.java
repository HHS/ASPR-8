package gov.hhs.aspr.translation.protobuf.core.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.google.protobuf.StringValue;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;

public class AT_StringTranslationSpec {

    @Test
    public void testConvertInputObject() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .build();

        StringTranslationSpec stringTranslationSpec = new StringTranslationSpec();
        stringTranslationSpec.init(protobufTranslationEngine);

        String expectedValue = "testString";
        StringValue inputValue = StringValue.of(expectedValue);

        String actualValue = stringTranslationSpec.convertInputObject(inputValue);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testConvertAppObject() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .build();

        StringTranslationSpec stringTranslationSpec = new StringTranslationSpec();
        stringTranslationSpec.init(protobufTranslationEngine);

        String appValue = "testString";
        StringValue expectedValue = StringValue.of(appValue);

        StringValue actualValue = stringTranslationSpec.convertAppObject(appValue);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void getAppObjectClass() {
        StringTranslationSpec stringTranslationSpec = new StringTranslationSpec();

        assertEquals(String.class, stringTranslationSpec.getAppObjectClass());
    }

    @Test
    public void getInputObjectClass() {
        StringTranslationSpec stringTranslationSpec = new StringTranslationSpec();

        assertEquals(StringValue.class, stringTranslationSpec.getInputObjectClass());
    }
}
