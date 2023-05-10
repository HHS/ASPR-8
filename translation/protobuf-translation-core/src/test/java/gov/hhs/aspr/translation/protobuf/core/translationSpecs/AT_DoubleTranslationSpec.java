package gov.hhs.aspr.translation.protobuf.core.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.google.protobuf.DoubleValue;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;

public class AT_DoubleTranslationSpec {

    @Test
    public void testConvertInputObject() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .build();

        DoubleTranslationSpec doubleTranslationSpec = new DoubleTranslationSpec();
        doubleTranslationSpec.init(protobufTranslationEngine);

        Double expectedValue = 100.0;
        DoubleValue inputValue = DoubleValue.of(expectedValue);

        Double actualValue = doubleTranslationSpec.convertInputObject(inputValue);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testConvertAppObject() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .build();

        DoubleTranslationSpec doubleTranslationSpec = new DoubleTranslationSpec();
        doubleTranslationSpec.init(protobufTranslationEngine);

        Double appValue = 100.0;
        DoubleValue expectedValue = DoubleValue.of(appValue);

        DoubleValue actualValue = doubleTranslationSpec.convertAppObject(appValue);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void getAppObjectClass() {
        DoubleTranslationSpec doubleTranslationSpec = new DoubleTranslationSpec();

        assertEquals(Double.class, doubleTranslationSpec.getAppObjectClass());
    }

    @Test
    public void getInputObjectClass() {
        DoubleTranslationSpec doubleTranslationSpec = new DoubleTranslationSpec();

        assertEquals(DoubleValue.class, doubleTranslationSpec.getInputObjectClass());
    }
}
