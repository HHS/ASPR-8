package gov.hhs.aspr.translation.protobuf.core.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.google.protobuf.Int32Value;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;

public class AT_IntegerTranslationSpec {

    @Test
    public void testConvertInputObject() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .build();

        IntegerTranslationSpec integerTranslationSpec = new IntegerTranslationSpec();
        integerTranslationSpec.init(protobufTranslationEngine);

        Integer expectedValue = 10;
        Int32Value inputValue = Int32Value.of(expectedValue);

        Integer actualValue = integerTranslationSpec.convertInputObject(inputValue);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testConvertAppObject() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .build();

        IntegerTranslationSpec integerTranslationSpec = new IntegerTranslationSpec();
        integerTranslationSpec.init(protobufTranslationEngine);

        Integer appValue = 10;
        Int32Value expectedValue = Int32Value.of(appValue);

        Int32Value actualValue = integerTranslationSpec.convertAppObject(appValue);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void getAppObjectClass() {
        IntegerTranslationSpec integerTranslationSpec = new IntegerTranslationSpec();

        assertEquals(Integer.class, integerTranslationSpec.getAppObjectClass());
    }

    @Test
    public void getInputObjectClass() {
        IntegerTranslationSpec integerTranslationSpec = new IntegerTranslationSpec();

        assertEquals(Int32Value.class, integerTranslationSpec.getInputObjectClass());
    }
}
