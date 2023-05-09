package gov.hhs.aspr.translation.protobuf.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.Any;
import com.google.protobuf.Int32Value;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.ProtocolMessageEnum;

import gov.hhs.aspr.translation.core.CoreTranslationError;
import gov.hhs.aspr.translation.core.testsupport.TestResourceHelper;
import gov.hhs.aspr.translation.core.testsupport.testobject.app.TestAppChildObject;
import gov.hhs.aspr.translation.core.testsupport.testobject.app.TestAppObject;
import gov.hhs.aspr.translation.protobuf.core.testsupport.TestObjectUtil;
import gov.hhs.aspr.translation.protobuf.core.testsupport.testClasses.BadMessageBadArguements;
import gov.hhs.aspr.translation.protobuf.core.testsupport.testClasses.BadMessageIllegalAccess;
import gov.hhs.aspr.translation.protobuf.core.testsupport.testClasses.BadMessageNoMethod;
import gov.hhs.aspr.translation.protobuf.core.testsupport.testClasses.BadMessageNonStaticMethod;
import gov.hhs.aspr.translation.protobuf.core.testsupport.testcomplexobject.input.TestComplexInputObject;
import gov.hhs.aspr.translation.protobuf.core.testsupport.testcomplexobject.translationSpecs.TestProtobufComplexObjectTranslationSpec;
import gov.hhs.aspr.translation.protobuf.core.testsupport.testobject.input.TestInputEnum;
import gov.hhs.aspr.translation.protobuf.core.testsupport.testobject.input.TestInputObject;
import gov.hhs.aspr.translation.protobuf.core.testsupport.testobject.translationSpecs.TestProtobufObjectTranslationSpec;
import util.errors.ContractException;

public class AT_ProtobufTranslationEngine {
    Path basePath = TestResourceHelper.getResourceDir(this.getClass());
    Path filePath = TestResourceHelper.makeTestOutputDir(basePath);

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

        // preconditions

        // no translationSpec was prodived for the parent class
        ContractException contractException = assertThrows(ContractException.class, () -> {
            ProtobufTranslationEngine protobufTranslationEngine2 = ProtobufTranslationEngine
                    .builder()
                    .addTranslationSpec(new TestProtobufComplexObjectTranslationSpec())
                    .build();

            protobufTranslationEngine2.init();

            TestAppObject testAppObject2 = TestObjectUtil.generateTestAppObject();
            TestAppChildObject testAppChildObject2 = TestObjectUtil.getChildAppFromApp(testAppObject2);

            protobufTranslationEngine2.getAnyFromObjectAsSafeClass(testAppChildObject2, TestAppObject.class);
        });

        assertEquals(CoreTranslationError.UNKNOWN_TRANSLATION_SPEC, contractException.getErrorType());
    }

    @Test
    void testGetObjectFromAny() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .addTranslationSpec(new TestProtobufObjectTranslationSpec())
                .addTranslationSpec(new TestProtobufComplexObjectTranslationSpec())
                .build();

        protobufTranslationEngine.init();

        TestAppObject expectedObject = TestObjectUtil.generateTestAppObject();

        TestInputObject expectedInputObject = TestObjectUtil.getInputFromApp(expectedObject);
        Any any = Any.pack(expectedInputObject);

        Object actualObject = protobufTranslationEngine.getObjectFromAny(any);

        assertTrue(actualObject.getClass() == TestAppObject.class);
        assertEquals(expectedObject, actualObject);
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
        assertEquals(testComplexInputObjectClass,
                protobufTranslationEngine.getClassFromTypeUrl(testComplexInputObjectTypeUrl));

        // preconditions
        // no typeUrl was provided and/or malformed typeUrl
        ContractException contractException = assertThrows(ContractException.class, () -> {
            ProtobufTranslationEngine protobufTranslationEngine2 = ProtobufTranslationEngine
                    .builder()
                    .addTranslationSpec(new TestProtobufComplexObjectTranslationSpec())
                    .build();

            protobufTranslationEngine2.init();

            protobufTranslationEngine2.getClassFromTypeUrl("badUrl");
        });

        assertEquals(ProtobufCoreTranslationError.UNKNOWN_TYPE_URL, contractException.getErrorType());
    }

    @Test
    public void testDebugPrint() throws IOException {
        String fileName = "debugPrintFromEngine_1-testOutput.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);

        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .addTranslationSpec(new TestProtobufObjectTranslationSpec())
                .addTranslationSpec(new TestProtobufComplexObjectTranslationSpec())
                .build();

        protobufTranslationEngine.init();
        protobufTranslationEngine.setDebug(true);

        TestAppObject expectedAppObject = TestObjectUtil.generateTestAppObject();

        FileWriter fileWriter = new FileWriter(filePath.resolve(fileName).toFile());
        FileReader fileReader = new FileReader(filePath.resolve(fileName).toFile());

        protobufTranslationEngine.writeOutput(fileWriter, expectedAppObject, Optional.empty());
        TestAppObject actualAppObject = protobufTranslationEngine.readInput(fileReader, TestInputObject.class);
        assertEquals(expectedAppObject, actualAppObject);
    }

    @Test
    public void testParseJson() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .addTranslationSpec(new TestProtobufObjectTranslationSpec())
                .addTranslationSpec(new TestProtobufComplexObjectTranslationSpec())
                .setIgnoringUnknownFields(false)
                .build();

        protobufTranslationEngine.init();

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("unknownProperty", "unknownValue");

        assertThrows(RuntimeException.class, () -> {
            protobufTranslationEngine.parseJson(jsonObject, TestInputObject.class);
        });
    }

    @Test
    public void testGetBuilderForMessage() {

        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .addTranslationSpec(new TestProtobufObjectTranslationSpec())
                .addTranslationSpec(new TestProtobufComplexObjectTranslationSpec())
                .build();

        protobufTranslationEngine.init();

        // preconditions
        /*
         * Note on these preconditions: Because of the type enforced on readInput()
         * ensuring that the passed in classRef is a child of Message.class, these
         * preconditions should never be encountered. But for coverage purposes, are
         * included here.
         */
        // class ref does not contain a newBuilder method
        assertThrows(RuntimeException.class, () -> {
            protobufTranslationEngine.getBuilderForMessage(BadMessageNoMethod.class);
        });

        // class has a newBuilder method but it is not static
        assertThrows(RuntimeException.class, () -> {
            protobufTranslationEngine.getBuilderForMessage(BadMessageNonStaticMethod.class);
        });

        // class has a static newBuilder method but it takes arguements
        assertThrows(RuntimeException.class, () -> {
            protobufTranslationEngine.getBuilderForMessage(BadMessageBadArguements.class);
        });

        // class has a newBuilder method but it is not accessible
        assertThrows(RuntimeException.class, () -> {
            protobufTranslationEngine.getBuilderForMessage(BadMessageIllegalAccess.class);
        });

    }

    @Test
    void testReadInput() throws IOException {
        String fileName = "readInputFromEngine_1-testOutput.json";
        String fileName2 = "readInputFromEngine_2-testOutput.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);
        TestResourceHelper.createTestOutputFile(filePath, fileName2);

        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .addTranslationSpec(new TestProtobufObjectTranslationSpec())
                .addTranslationSpec(new TestProtobufComplexObjectTranslationSpec())
                .build();

        protobufTranslationEngine.init();

        TestAppObject expectedAppObject = TestObjectUtil.generateTestAppObject();

        FileWriter fileWriter = new FileWriter(filePath.resolve(fileName).toFile());
        FileReader fileReader = new FileReader(filePath.resolve(fileName).toFile());

        FileWriter fileWriter2 = new FileWriter(filePath.resolve(fileName2).toFile());
        FileReader fileReader2 = new FileReader(filePath.resolve(fileName2).toFile());

        protobufTranslationEngine.writeOutput(fileWriter, expectedAppObject, Optional.empty());
        TestAppObject actualAppObject = protobufTranslationEngine.readInput(fileReader, TestInputObject.class);
        assertEquals(expectedAppObject, actualAppObject);

        protobufTranslationEngine.writeOutput(fileWriter2, TestObjectUtil.getChildAppFromApp(expectedAppObject),
                Optional.of(TestAppObject.class));
        TestAppObject actualAppChildObject = protobufTranslationEngine.readInput(fileReader2, TestInputObject.class);
        assertEquals(expectedAppObject, actualAppChildObject);

        // preconditions
        // input class is not a Message class
        ContractException contractException = assertThrows(ContractException.class, () -> {
            protobufTranslationEngine.readInput(fileReader2, TestAppObject.class);
        });

        assertEquals(ProtobufCoreTranslationError.INVALID_READ_INPUT_CLASS_REF, contractException.getErrorType());

        // precondition for the Runtime exceptions are convered by the test:
        // testGetBuilderForMessage() and testParseJson()
    }

    @Test
    void testWriteOutput() throws IOException {
        String fileName = "writeOutputFromEngine_1-testOutput.json";
        String fileName2 = "writeOutputFromEngine_2-testOutput.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);
        TestResourceHelper.createTestOutputFile(filePath, fileName2);

        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .addTranslationSpec(new TestProtobufObjectTranslationSpec())
                .addTranslationSpec(new TestProtobufComplexObjectTranslationSpec())
                // .addTranslationSpec(new TestProtobufEnumTranslationSpec())
                .build();

        protobufTranslationEngine.init();

        TestAppObject expectedAppObject = TestObjectUtil.generateTestAppObject();

        FileWriter fileWriter = new FileWriter(filePath.resolve(fileName).toFile());
        FileReader fileReader = new FileReader(filePath.resolve(fileName).toFile());

        FileWriter fileWriter2 = new FileWriter(filePath.resolve(fileName2).toFile());
        FileReader fileReader2 = new FileReader(filePath.resolve(fileName2).toFile());

        protobufTranslationEngine.writeOutput(fileWriter, expectedAppObject, Optional.empty());
        TestAppObject actualAppObject = protobufTranslationEngine.readInput(fileReader, TestInputObject.class);
        assertEquals(expectedAppObject, actualAppObject);

        protobufTranslationEngine.writeOutput(fileWriter2, TestObjectUtil.getChildAppFromApp(expectedAppObject),
                Optional.of(TestAppObject.class));
        TestAppObject actualAppChildObject = protobufTranslationEngine.readInput(fileReader2, TestInputObject.class);
        assertEquals(expectedAppObject, actualAppChildObject);

        // this test is just for coverage, but this method should never be directly
        // called
        TestInputObject inputObject = TestObjectUtil.generateTestInputObject();
        protobufTranslationEngine.writeOutput(fileWriter2, inputObject, Optional.empty());
        actualAppObject = protobufTranslationEngine.readInput(fileReader2, TestInputObject.class);
        assertEquals(TestObjectUtil.getAppFromInput(inputObject), actualAppObject);

        // preconditions
        // IO error occurs
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> {
            FileWriter fileWriter3 = new FileWriter(filePath.resolve(fileName).toFile());
            // close the file reader
            fileWriter3.close();
            protobufTranslationEngine.writeOutput(fileWriter3, expectedAppObject, Optional.empty());
        });

        assertTrue(runtimeException.getCause() instanceof IOException);

        filePath.resolve(fileName).toFile().setWritable(true);
    }

    @Test
    void testBuilder() {
        assertNotNull(ProtobufTranslationEngine.builder());
    }

    @Test
    void testBuild() {

        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine.builder().build();

        assertNotNull(protobufTranslationEngine);

        // parser and printer do not have equals contracts, so no way to check for
        // equality
        // the use cases for them are adequately tested in: testReadInput and
        // testWriteOutput
    }

    @Test
    public void testGetDefaultMessage() {
        ProtobufTranslationEngine.Builder pBuilder = ProtobufTranslationEngine.builder();

        /*
         * Note: because this method is only ever called if the classRef is an instance
         * of Message.class, this method should never throw an exception. This test is
         * here exclusively for test coverage.
         */
        assertThrows(RuntimeException.class, () -> {
            pBuilder.getDefaultMessage(Message.class);
        });
    }

    @Test
    public void testGetDefaultEnum() {
        ProtobufTranslationEngine.Builder pBuilder = ProtobufTranslationEngine.builder();

        /*
         * Note: because this method is only ever called if the classRef is an instance
         * of ProtocolMessageEnum.class, this method should never throw an exception.
         * This test is here exclusively for test coverage.
         */
        assertThrows(RuntimeException.class, () -> {
            pBuilder.getDefaultEnum(ProtocolMessageEnum.class);
        });
    }

    @Test
    public void testPopulate() {
        ProtobufTranslationEngine.Builder pBuilder = ProtobufTranslationEngine.builder();

        // Protobuf Message
        pBuilder.populate(TestInputObject.class);

        // Protobuf Enum
        pBuilder.populate(TestInputEnum.class);

        // precondition
        // if class is neither a Message nor a ProtocolMessageEnum
        ContractException contractException = assertThrows(ContractException.class, () -> {
            pBuilder.populate(TestAppObject.class);
        });

        assertEquals(ProtobufCoreTranslationError.INVALID_INPUT_CLASS, contractException.getErrorType());

    }

    @Test
    void testAddFieldToIncludeDefaultValue() throws InvalidProtocolBufferException {

        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .addFieldToIncludeDefaultValue(TestInputObject.getDescriptor().findFieldByName("integer"))
                .build();

        TestInputObject expectedInputObject = TestObjectUtil.generateTestInputObject()
                .toBuilder()
                .setInteger(0)
                .setBool(false)
                .setString("")
                .build();

        String message = protobufTranslationEngine.getJsonPrinter().print(expectedInputObject);

        JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();

        assertTrue(jsonObject.has("integer"));
        assertFalse(jsonObject.has("string"));
        assertFalse(jsonObject.has("bool"));

        assertTrue(jsonObject.get("integer").isJsonPrimitive());
        assertEquals(0, jsonObject.get("integer").getAsInt());
    }

    @Test
    void testAddTranslationSpec() {
        /*
         * this test will only test the difference between the ProtobufTranslationEngine
         * and the TranslationEngine, which is only the populateMethod
         */

        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .addTranslationSpec(new TestProtobufObjectTranslationSpec())
                .addTranslationSpec(new TestProtobufComplexObjectTranslationSpec())
                .build();

        protobufTranslationEngine.init();

        assertDoesNotThrow(() -> {
            protobufTranslationEngine.getClassFromTypeUrl(TestInputObject.getDescriptor().getFullName());
            protobufTranslationEngine.getClassFromTypeUrl(TestComplexInputObject.getDescriptor().getFullName());
        });

        // precondition
        // the precondition for this is that the inputClass is not a Message nor a
        // ProtocolMessageEnum, and is tested in the testPopulate() test
    }

    @Test
    void testSetIgnoringUnknownFields() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .setIgnoringUnknownFields(true)
                .build();

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("bool", false);
        jsonObject.addProperty("unknownField", "unknownField");

        assertDoesNotThrow(() -> {
            protobufTranslationEngine.getJsonParser().merge(jsonObject.toString(), TestInputObject.newBuilder());
        });

        ProtobufTranslationEngine protobufTranslationEngine2 = ProtobufTranslationEngine
                .builder()
                .setIgnoringUnknownFields(false)
                .build();

        assertThrows(InvalidProtocolBufferException.class, () -> {
            protobufTranslationEngine2.getJsonParser().merge(jsonObject.toString(), TestInputObject.newBuilder());
        });

        assertDoesNotThrow(() -> {
            jsonObject.remove("unknownField");
            protobufTranslationEngine.getJsonParser().merge(jsonObject.toString(), TestInputObject.newBuilder());
        });
    }

    @Test
    void testSetIncludingDefaultValueFields() throws InvalidProtocolBufferException {

        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .setIncludingDefaultValueFields(true)
                .build();

        TestInputObject expectedInputObject = TestObjectUtil.generateTestInputObject()
                .toBuilder()
                .setInteger(0)
                .setBool(false)
                .setString("")
                .build();

        String message = protobufTranslationEngine.getJsonPrinter().print(expectedInputObject);

        JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();

        assertTrue(jsonObject.has("integer"));
        assertTrue(jsonObject.has("string"));
        assertTrue(jsonObject.has("bool"));

        assertTrue(jsonObject.get("integer").isJsonPrimitive());
        assertTrue(jsonObject.get("string").isJsonPrimitive());
        assertTrue(jsonObject.get("bool").isJsonPrimitive());

        assertEquals(0, jsonObject.get("integer").getAsInt());
        assertEquals(false, jsonObject.get("bool").getAsBoolean());
        assertEquals("", jsonObject.get("string").getAsString());

        protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .setIncludingDefaultValueFields(false)
                .build();

        message = protobufTranslationEngine.getJsonPrinter().print(expectedInputObject);

        jsonObject = JsonParser.parseString(message).getAsJsonObject();
        assertFalse(jsonObject.has("integer"));
        assertFalse(jsonObject.has("string"));
        assertFalse(jsonObject.has("bool"));
    }
}
