package gov.hhs.aspr.ms.taskit.protobuf.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.google.protobuf.UInt32Value;

import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_UIntegerTranslationSpec {

    @Test
    @UnitTestConstructor(target = UIntegerTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new UIntegerTranslationSpec());
    }

    @Test
    @UnitTestForCoverage
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
    @UnitTestForCoverage
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