package gov.hhs.aspr.translation.protobuf.core.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.google.protobuf.FloatValue;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;

public class AT_FloatTranslationSpec {

    @Test
    public void testConvertInputObject() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .build();

        FloatTranslationSpec floatTranslationSpec = new FloatTranslationSpec();
        floatTranslationSpec.init(protobufTranslationEngine);

        Float expectedValue = 10.0f;
        FloatValue inputValue = FloatValue.of(expectedValue);

        Float actualValue = floatTranslationSpec.convertInputObject(inputValue);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testConvertAppObject() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .build();

        FloatTranslationSpec floatTranslationSpec = new FloatTranslationSpec();
        floatTranslationSpec.init(protobufTranslationEngine);

        Float appValue = 10.01f;
        FloatValue expectedValue = FloatValue.of(appValue);

        FloatValue actualValue = floatTranslationSpec.convertAppObject(appValue);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void getAppObjectClass() {
        FloatTranslationSpec floatTranslationSpec = new FloatTranslationSpec();

        assertEquals(Float.class, floatTranslationSpec.getAppObjectClass());
    }

    @Test
    public void getInputObjectClass() {
        FloatTranslationSpec floatTranslationSpec = new FloatTranslationSpec();

        assertEquals(FloatValue.class, floatTranslationSpec.getInputObjectClass());
    }
}