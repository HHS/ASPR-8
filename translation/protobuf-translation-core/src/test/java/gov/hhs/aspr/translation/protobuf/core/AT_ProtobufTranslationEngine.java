package gov.hhs.aspr.translation.protobuf.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.google.protobuf.Any;
import com.google.protobuf.Int32Value;

public class AT_ProtobufTranslationEngine {

    @Test
    void testGetAnyFromObject() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine.builder().build();

        protobufTranslationEngine.init();

        Integer integer = 1500;
        Int32Value int32Value = Int32Value.of(integer);
        Any expectedAny = Any.pack(int32Value);

        Any actualAny = protobufTranslationEngine.getAnyFromObject(integer);

        assertEquals(expectedAny, actualAny);
    }

    @Test
    void testGetAnyFromObjectAsSafeClass() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine.builder().build();

        protobufTranslationEngine.init();

        Integer integer = 1500;
        Int32Value int32Value = Int32Value.of(integer);
        Any expectedAny = Any.pack(int32Value);

        Any actualAny = protobufTranslationEngine.getAnyFromObject(integer);

        assertEquals(expectedAny, actualAny);
    }

    @Test
    void testGetClassFromTypeUrl() {

    }

    @Test
    void testGetJsonParser() {

    }

    @Test
    void testGetJsonPrinter() {

    }

    @Test
    void testGetObjectFromAny() {

    }

    @Test
    void testReadInput() {

    }

    @Test
    void testWriteOutput() {

    }

    @Test
    void testBuilder() {

    }

    @Test
    void testBuild() {

    }

    @Test
    void testAddFieldToIncludeDefaultValue() {

    }

    @Test
    void testAddTranslationSpec() {

    }

    @Test
    void testSetIgnoringUnknownFields() {

    }

    @Test
    void testSetIncludingDefaultValueFields() {

    }
}
