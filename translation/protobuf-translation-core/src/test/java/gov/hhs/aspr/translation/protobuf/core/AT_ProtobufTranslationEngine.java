package gov.hhs.aspr.translation.protobuf.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.google.protobuf.Any;
import com.google.protobuf.Int32Value;

import gov.hhs.aspr.translation.core.testsupport.testobject.app.TestAppChildObject;
import gov.hhs.aspr.translation.core.testsupport.testobject.app.TestAppObject;
import gov.hhs.aspr.translation.protobuf.core.testsupport.TestObjectUtil;
import gov.hhs.aspr.translation.protobuf.core.testsupport.testcomplexobject.input.TestComplexInputObject;
import gov.hhs.aspr.translation.protobuf.core.testsupport.testcomplexobject.translationSpecs.TestProtobufComplexObjectTranslationSpec;
import gov.hhs.aspr.translation.protobuf.core.testsupport.testobject.input.TestInputObject;
import gov.hhs.aspr.translation.protobuf.core.testsupport.testobject.translationSpecs.TestProtobufObjectTranslationSpec;

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
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .addTranslationSpec(new TestProtobufObjectTranslationSpec())
                .addTranslationSpec(new TestProtobufComplexObjectTranslationSpec())
                .build();

        protobufTranslationEngine.init();

        TestAppObject testAppObject = TestObjectUtil.generateTestAppObject();
        TestAppChildObject testAppChildObject = TestObjectUtil.getChildAppFromApp(testAppObject);

        TestInputObject expectedInputObject = TestObjectUtil.getInputFromApp(testAppChildObject);
        Any expectedAny = Any.pack(expectedInputObject);

        Any actualAny = protobufTranslationEngine.getAnyFromObjectAsSafeClass(testAppChildObject, TestAppObject.class);

        assertEquals(expectedAny, actualAny);
    }

    @Test
    void testGetObjectFromAny() {

    }

    @Test
    void testGetClassFromTypeUrl() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .addTranslationSpec(new TestProtobufObjectTranslationSpec())
                .addTranslationSpec(new TestProtobufComplexObjectTranslationSpec())
                .build();

        protobufTranslationEngine.init();

        Class<TestInputObject> testInputObjectClass = TestInputObject.class;
        Class<TestComplexInputObject> testComplexInputObjectClass = TestComplexInputObject.class;

        String testInputObjectTypeUrl = TestInputObject.getDescriptor().getFullName();
        String testComplexInputObjectTypeUrl = TestComplexInputObject.getDescriptor().getFullName();

        assertEquals(testInputObjectClass, protobufTranslationEngine.getClassFromTypeUrl(testInputObjectTypeUrl));
        assertEquals(testComplexInputObjectClass, protobufTranslationEngine.getClassFromTypeUrl(testComplexInputObjectTypeUrl));
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
