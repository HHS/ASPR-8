package gov.hhs.aspr.ms.taskit.protobuf.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.google.protobuf.StringValue;

import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_StringTranslationSpec {

    @Test
    @UnitTestConstructor(target = StringTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new StringTranslationSpec());
    }

    @Test
    @UnitTestForCoverage
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
    @UnitTestForCoverage
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
