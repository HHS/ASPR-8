package gov.hhs.aspr.translation.protobuf.core.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.google.protobuf.DoubleValue;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.annotations.UnitTestForCoverage;

public class AT_DoubleTranslationSpec {

    @Test
    @UnitTestConstructor(target = DoubleTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new DoubleTranslationSpec());
    }

    @Test
    @UnitTestForCoverage
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
    @UnitTestForCoverage
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
    @UnitTestMethod(target = DoubleTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        DoubleTranslationSpec doubleTranslationSpec = new DoubleTranslationSpec();

        assertEquals(Double.class, doubleTranslationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = DoubleTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        DoubleTranslationSpec doubleTranslationSpec = new DoubleTranslationSpec();

        assertEquals(DoubleValue.class, doubleTranslationSpec.getInputObjectClass());
    }
}
