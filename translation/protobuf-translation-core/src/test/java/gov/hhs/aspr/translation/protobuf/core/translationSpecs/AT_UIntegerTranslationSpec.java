package gov.hhs.aspr.translation.protobuf.core.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.google.protobuf.UInt32Value;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;

public class AT_UIntegerTranslationSpec {

    @Test
    public void testConvertInputObject() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .build();

        UIntegerTranslationSpec uIntegerTranslationSpec = new UIntegerTranslationSpec();
        uIntegerTranslationSpec.init(protobufTranslationEngine);

        Integer expectedValue = 10;
        UInt32Value inputValue = UInt32Value.of(expectedValue);

        Integer actualValue = uIntegerTranslationSpec.convertInputObject(inputValue);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testConvertAppObject() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .build();

        UIntegerTranslationSpec uIntegerTranslationSpec = new UIntegerTranslationSpec();
        uIntegerTranslationSpec.init(protobufTranslationEngine);

        Integer appValue = 100;
        UInt32Value expectedValue = UInt32Value.of(appValue);

        UInt32Value actualValue = uIntegerTranslationSpec.convertAppObject(appValue);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void getAppObjectClass() {
        UIntegerTranslationSpec uIntegerTranslationSpec = new UIntegerTranslationSpec();

        assertEquals(Integer.class, uIntegerTranslationSpec.getAppObjectClass());
    }

    @Test
    public void getInputObjectClass() {
        UIntegerTranslationSpec uIntegerTranslationSpec = new UIntegerTranslationSpec();

        assertEquals(UInt32Value.class, uIntegerTranslationSpec.getInputObjectClass());
    }
}