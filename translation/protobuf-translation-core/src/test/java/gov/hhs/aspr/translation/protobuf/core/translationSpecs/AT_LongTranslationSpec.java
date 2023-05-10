package gov.hhs.aspr.translation.protobuf.core.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.google.protobuf.Int64Value;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;

public class AT_LongTranslationSpec {

    @Test
    public void testConvertInputObject() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .build();

        LongTranslationSpec longTranslationSpec = new LongTranslationSpec();
        longTranslationSpec.init(protobufTranslationEngine);

        Long expectedValue = 100L;
        Int64Value inputValue = Int64Value.of(expectedValue);

        Long actualValue = longTranslationSpec.convertInputObject(inputValue);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testConvertAppObject() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .build();

        LongTranslationSpec longTranslationSpec = new LongTranslationSpec();
        longTranslationSpec.init(protobufTranslationEngine);

        Long appValue = 1000L;
        Int64Value expectedValue = Int64Value.of(appValue);

        Int64Value actualValue = longTranslationSpec.convertAppObject(appValue);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void getAppObjectClass() {
        LongTranslationSpec longTranslationSpec = new LongTranslationSpec();

        assertEquals(Long.class, longTranslationSpec.getAppObjectClass());
    }

    @Test
    public void getInputObjectClass() {
        LongTranslationSpec longTranslationSpec = new LongTranslationSpec();

        assertEquals(Int64Value.class, longTranslationSpec.getInputObjectClass());
    }
}
