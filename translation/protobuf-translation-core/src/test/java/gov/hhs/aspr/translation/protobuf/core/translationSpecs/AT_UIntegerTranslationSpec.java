package gov.hhs.aspr.translation.protobuf.core.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.google.protobuf.UInt32Value;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.annotations.UnitTestMethodForCoverage;

public class AT_UIntegerTranslationSpec {

    @Test
    @UnitTestConstructor(target = UIntegerTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new UIntegerTranslationSpec());
    }

    @Test
    @UnitTestMethodForCoverage
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
    @UnitTestMethodForCoverage
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
    @UnitTestMethod(target = UIntegerTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        UIntegerTranslationSpec uIntegerTranslationSpec = new UIntegerTranslationSpec();

        assertEquals(Integer.class, uIntegerTranslationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = UIntegerTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        UIntegerTranslationSpec uIntegerTranslationSpec = new UIntegerTranslationSpec();

        assertEquals(UInt32Value.class, uIntegerTranslationSpec.getInputObjectClass());
    }
}