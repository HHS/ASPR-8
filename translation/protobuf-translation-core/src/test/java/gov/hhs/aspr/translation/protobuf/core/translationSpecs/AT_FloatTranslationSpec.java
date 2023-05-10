package gov.hhs.aspr.translation.protobuf.core.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.google.protobuf.FloatValue;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.annotations.UnitTestMethodForCoverage;

public class AT_FloatTranslationSpec {

    @Test
    @UnitTestConstructor(target = FloatTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new FloatTranslationSpec());
    }

    @Test
    @UnitTestMethodForCoverage
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
    @UnitTestMethodForCoverage
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
    @UnitTestMethod(target = FloatTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        FloatTranslationSpec floatTranslationSpec = new FloatTranslationSpec();

        assertEquals(Float.class, floatTranslationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = FloatTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        FloatTranslationSpec floatTranslationSpec = new FloatTranslationSpec();

        assertEquals(FloatValue.class, floatTranslationSpec.getInputObjectClass());
    }
}