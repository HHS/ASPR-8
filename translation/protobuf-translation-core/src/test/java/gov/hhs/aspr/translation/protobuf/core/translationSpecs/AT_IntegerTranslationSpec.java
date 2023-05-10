package gov.hhs.aspr.translation.protobuf.core.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.google.protobuf.Int32Value;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_IntegerTranslationSpec {

    @Test
    @UnitTestConstructor(target = IntegerTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new IntegerTranslationSpec());
    }

    @Test
    @UnitTestMethod(target = IntegerTranslationSpec.class, name = "convertInputObject", args = { Int32Value.class })
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
    @UnitTestMethod(target = IntegerTranslationSpec.class, name = "convertAppObject", args = { Integer.class })
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
    @UnitTestMethod(target = IntegerTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        IntegerTranslationSpec integerTranslationSpec = new IntegerTranslationSpec();

        assertEquals(Integer.class, integerTranslationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = IntegerTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        IntegerTranslationSpec integerTranslationSpec = new IntegerTranslationSpec();

        assertEquals(Int32Value.class, integerTranslationSpec.getInputObjectClass());
    }
}
