package gov.hhs.aspr.translation.protobuf.core.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.google.protobuf.StringValue;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_StringTranslationSpec {

    @Test
    @UnitTestConstructor(target = StringTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new StringTranslationSpec());
    }

    @Test
    @UnitTestMethod(target = StringTranslationSpec.class, name = "convertInputObject", args = { StringValue.class })
    public void testConvertInputObject() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .build();

        StringTranslationSpec stringTranslationSpec = new StringTranslationSpec();
        stringTranslationSpec.init(protobufTranslationEngine);

        String expectedValue = "testString";
        StringValue inputValue = StringValue.of(expectedValue);

        String actualValue = stringTranslationSpec.convertInputObject(inputValue);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    @UnitTestMethod(target = StringTranslationSpec.class, name = "convertAppObject", args = { String.class })
    public void testConvertAppObject() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .build();

        StringTranslationSpec stringTranslationSpec = new StringTranslationSpec();
        stringTranslationSpec.init(protobufTranslationEngine);

        String appValue = "testString";
        StringValue expectedValue = StringValue.of(appValue);

        StringValue actualValue = stringTranslationSpec.convertAppObject(appValue);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    @UnitTestMethod(target = StringTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        StringTranslationSpec stringTranslationSpec = new StringTranslationSpec();

        assertEquals(String.class, stringTranslationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = StringTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        StringTranslationSpec stringTranslationSpec = new StringTranslationSpec();

        assertEquals(StringValue.class, stringTranslationSpec.getInputObjectClass());
    }
}
