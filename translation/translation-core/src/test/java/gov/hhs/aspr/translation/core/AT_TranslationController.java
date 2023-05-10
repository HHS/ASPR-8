package gov.hhs.aspr.translation.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.translation.core.testsupport.TestObjectUtil;
import gov.hhs.aspr.translation.core.testsupport.TestResourceHelper;
import gov.hhs.aspr.translation.core.testsupport.TestTranslationEngine;
import gov.hhs.aspr.translation.core.testsupport.testcomplexobject.TestComplexObjectTranslator;
import gov.hhs.aspr.translation.core.testsupport.testcomplexobject.TestComplexObjectTranslatorId;
import gov.hhs.aspr.translation.core.testsupport.testcomplexobject.app.TestComplexAppObject;
import gov.hhs.aspr.translation.core.testsupport.testobject.TestObjectTranslator;
import gov.hhs.aspr.translation.core.testsupport.testobject.TestObjectTranslatorId;
import gov.hhs.aspr.translation.core.testsupport.testobject.app.TestAppObject;
import gov.hhs.aspr.translation.core.testsupport.testobject.input.TestInputObject;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.graph.MutableGraph;

public class AT_TranslationController {

    Path basePath = TestResourceHelper.getResourceDir(this.getClass());
    Path filePath = TestResourceHelper.makeTestOutputDir(basePath);

    @Test
    @UnitTestForCoverage
    public void testValidateTranslationEngine() {
        TranslationController translationController = TranslationController
                .builder()
                .setTranslationEngineBuilder(TestTranslationEngine.builder())
                .buildWithoutInitAndChecks();

        // preconditions
        ContractException contractException = assertThrows(ContractException.class, () -> {
            translationController.validateTranslationEngine();
        });

        assertEquals(CoreTranslationError.NULL_TRANSLATION_ENGINE, contractException.getErrorType());

        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> {
            translationController.buildTranslationEngine();
            translationController.validateTranslationEngine();
        });

        assertEquals("TranslationEngine has been built but has not been initialized.", runtimeException.getMessage());

    }

    @Test
    @UnitTestMethod(target = TranslationController.class, name = "readInput", args = {})
    public void testReadInput() {
        String fileName = "readInput-testOutput.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);

        TranslationController translationController = TranslationController.builder()
                .addInputFilePath(filePath.resolve(fileName), TestInputObject.class)
                .addOutputFilePath(filePath.resolve(fileName), TestAppObject.class)
                .addTranslator(TestObjectTranslator.getTranslator())
                .addTranslator(TestComplexObjectTranslator.getTranslator())
                .setTranslationEngineBuilder(TestTranslationEngine.builder())
                .build();

        TestAppObject expectedAppObject = TestObjectUtil.generateTestAppObject();

        translationController.writeOutput(expectedAppObject);

        translationController.readInput();

        assertTrue(translationController.getObjects().size() == 1);

        TestAppObject actualTestAppObject = translationController.getFirstObject(TestAppObject.class);

        assertEquals(expectedAppObject, actualTestAppObject);

        // preconditions

        // invalid file path
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> {
            translationController.readInput(filePath.resolve("badPath"), TestInputObject.class);
        });

        assertTrue(runtimeException.getCause() instanceof IOException);
    }

    @Test
    @UnitTestForCoverage
    public void testMakeFileWriter() {
        String fileName = "MakeFileWriter-testOutput.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);

        TranslationController translationController = TranslationController.builder()
                .setTranslationEngineBuilder(TestTranslationEngine.builder())
                .build();

        FileWriter actuaFileWriter = translationController.makeFileWriter(filePath.resolve(fileName));

        assertNotNull(actuaFileWriter);
        // preconditions

        // if the filePath is invalid
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> {
            translationController.makeFileWriter(filePath);
        });

        assertTrue(runtimeException.getCause() instanceof IOException);
    }

    @Test
    @UnitTestForCoverage
    public void testGetOutputPath() {
        String fileName = "GetOutputPath_1-testOutput.json";
        String fileName2 = "GetOutputPath_2-testOutput.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);
        TestResourceHelper.createTestOutputFile(filePath, fileName2);

        TranslationController translationController = TranslationController.builder()
                .setTranslationEngineBuilder(TestTranslationEngine.builder())
                .addOutputFilePath(filePath.resolve(fileName), TestAppObject.class)
                .addOutputFilePath(filePath.resolve(fileName2), Object.class, 1)
                .addParentChildClassRelationship(TestAppObject.class, Object.class)
                .build();

        Pair<Path, Optional<Class<TestAppObject>>> expectedPair1 = new Pair<>(filePath.resolve(fileName),
                Optional.empty());
        Pair<Path, Optional<Class<Object>>> expectedPair2 = new Pair<>(filePath.resolve(fileName2),
                Optional.of(Object.class));

        Pair<Path, Optional<Class<TestAppObject>>> actualPair1 = translationController
                .getOutputPath(TestAppObject.class, 0);
        Pair<Path, Optional<Class<Object>>> actualPair2 = translationController.getOutputPath(TestAppObject.class, 1);

        assertEquals(expectedPair1, actualPair1);
        assertEquals(expectedPair2, actualPair2);
        // preconditions

        // if the class scenarioId pair does not exist and there is no parent child
        // class relationship
        ContractException contractException = assertThrows(ContractException.class, () -> {
            translationController.getOutputPath(TestInputObject.class, 1);
        });

        assertEquals(CoreTranslationError.INVALID_OUTPUT_CLASSREF, contractException.getErrorType());

        // if the class and scenarioID pair does not exist AND there is a parent child
        // class relationship AND the parentClass scenarioId pair does not exists
        contractException = assertThrows(ContractException.class, () -> {
            translationController.getOutputPath(TestAppObject.class, 4);
        });

        assertEquals(CoreTranslationError.INVALID_OUTPUT_CLASSREF, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = TranslationController.class, name = "writeOutput", args = { List.class })
    public void testWriteOutput_List() {
        String fileName = "WriteOutput_List_1-testOutput.json";
        String fileName2 = "WriteOutput_List_2-testOutput.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);
        TestResourceHelper.createTestOutputFile(filePath, fileName2);

        TranslationController translationController = TranslationController.builder()
                .addOutputFilePath(filePath.resolve(fileName), TestAppObject.class)
                .addOutputFilePath(filePath.resolve(fileName2), TestComplexAppObject.class)
                .addTranslator(TestObjectTranslator.getTranslator())
                .addTranslator(TestComplexObjectTranslator.getTranslator())
                .setTranslationEngineBuilder(TestTranslationEngine.builder())
                .build();

        List<Object> outputObjects = new ArrayList<>();

        outputObjects.add(TestObjectUtil.generateTestAppObject());
        outputObjects.add(TestObjectUtil.generateTestComplexAppObject());
        translationController.writeOutput(outputObjects);

        // preconditions
        // the runtime exception is covered by the test - testMakeFileWriter()
        // the contract exception for CoreTranslationError.INVALID_OUTPUT_CLASSREF is
        // covered by the test - testGetOutputPath()
        // the contract exception for CoreTranslationError.NULL_TRANSLATION_ENGINE is
        // covered by the test - testValidateTranslationEngine()
    }

    @Test
    @UnitTestMethod(target = TranslationController.class, name = "writeOutput", args = { List.class, Integer.class })
    public void testWriteOutput_List_ScenarioId() throws IOException {
        String fileName = "WriteOutput_List_ScenarioId_1-testOutput.json";
        String fileName2 = "WriteOutput_List_ScenarioId_2-testOutput.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);
        TestResourceHelper.createTestOutputFile(filePath, fileName2);

        TranslationController translationController = TranslationController.builder()
                .addOutputFilePath(filePath.resolve(fileName), TestAppObject.class, 1)
                .addOutputFilePath(filePath.resolve(fileName2), TestComplexAppObject.class, 1)
                .addTranslator(TestObjectTranslator.getTranslator())
                .addTranslator(TestComplexObjectTranslator.getTranslator())
                .setTranslationEngineBuilder(TestTranslationEngine.builder())
                .build();

        List<Object> outputObjects = new ArrayList<>();

        outputObjects.add(TestObjectUtil.generateTestAppObject());
        outputObjects.add(TestObjectUtil.generateTestComplexAppObject());
        translationController.writeOutput(outputObjects, 1);

        // preconditions
        // the runtime exception is covered by the test - testMakeFileWriter()
        // the contract exception for CoreTranslationError.INVALID_OUTPUT_CLASSREF is
        // covered by the test - testGetOutputPath()
        // the contract exception for CoreTranslationError.NULL_TRANSLATION_ENGINE is
        // covered by the test - testValidateTranslationEngine()
    }

    @Test
    @UnitTestMethod(target = TranslationController.class, name = "writeOutput", args = { Object.class })
    public void testWriteOutput() throws IOException {
        String fileName = "writeOutput-testOutput.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);

        TranslationController translationController = TranslationController.builder()
                .addOutputFilePath(filePath.resolve(fileName), TestAppObject.class)
                .addTranslator(TestObjectTranslator.getTranslator())
                .addTranslator(TestComplexObjectTranslator.getTranslator())
                .setTranslationEngineBuilder(TestTranslationEngine.builder())
                .build();

        TestAppObject expectedAppObject = TestObjectUtil.generateTestAppObject();

        translationController.writeOutput(expectedAppObject);

        // preconditions
        // the runtime exception is covered by the test - testMakeFileWriter()
        // the contract exception for CoreTranslationError.INVALID_OUTPUT_CLASSREF is
        // covered by the test - testGetOutputPath()
        // the contract exception for CoreTranslationError.NULL_TRANSLATION_ENGINE is
        // covered by the test - testValidateTranslationEngine()
    }

    @Test
    @UnitTestMethod(target = TranslationController.class, name = "writeOutput", args = { Object.class, Integer.class })
    public void testWriteOutput_ScenarioId() throws IOException {
        String fileName = "writeOutput_ScenarioId-testOutput.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);

        TranslationController translationController = TranslationController.builder()
                .addOutputFilePath(filePath.resolve(fileName), TestAppObject.class, 1)
                .addTranslator(TestObjectTranslator.getTranslator())
                .addTranslator(TestComplexObjectTranslator.getTranslator())
                .setTranslationEngineBuilder(TestTranslationEngine.builder())
                .build();

        TestAppObject expectedAppObject = TestObjectUtil.generateTestAppObject();

        translationController.writeOutput(expectedAppObject, 1);

        // preconditions
        // the runtime exception is covered by the test - testMakeFileWriter()
        // the contract exception for CoreTranslationError.INVALID_OUTPUT_CLASSREF is
        // covered by the test - testGetOutputPath()
        // the contract exception for CoreTranslationError.NULL_TRANSLATION_ENGINE is
        // covered by the test - testValidateTranslationEngine()
    }

    @Test
    @UnitTestMethod(target = TranslationController.class, name = "getFirstObject", args = { Class.class })
    public void testGetFirstObject() throws IOException {
        String fileName = "getFirstObject-testOutput.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);

        TranslationController translationController = TranslationController.builder()
                .addInputFilePath(filePath.resolve(fileName), TestInputObject.class)
                .addOutputFilePath(filePath.resolve(fileName), TestAppObject.class)
                .addTranslator(TestObjectTranslator.getTranslator())
                .addTranslator(TestComplexObjectTranslator.getTranslator())
                .setTranslationEngineBuilder(TestTranslationEngine.builder())
                .build();

        TestAppObject expectedAppObject = TestObjectUtil.generateTestAppObject();

        translationController.writeOutput(expectedAppObject);

        translationController.readInput();

        assertTrue(translationController.getObjects().size() == 1);

        TestAppObject actualTestAppObject = translationController.getFirstObject(TestAppObject.class);

        assertNotNull(actualTestAppObject);

        // preconditions
        ContractException contractException = assertThrows(ContractException.class, () -> {

            translationController.getFirstObject(TestInputObject.class);
        });

        assertEquals(CoreTranslationError.UNKNOWN_CLASSREF, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = TranslationController.class, name = "getObjects", args = { Class.class })
    public void testGetObjects_OfClass() throws IOException {
        String fileName = "GetObjects_OfClass_1-testOutput.json";
        String fileName2 = "GetObjects_OfClass_2-testOutput.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);
        TestResourceHelper.createTestOutputFile(filePath, fileName2);

        TranslationController translationController = TranslationController.builder()
                .addOutputFilePath(filePath.resolve(fileName), TestAppObject.class, 1)
                .addOutputFilePath(filePath.resolve(fileName2), TestAppObject.class, 2)
                .addInputFilePath(filePath.resolve(fileName), TestInputObject.class)
                .addInputFilePath(filePath.resolve(fileName2), TestInputObject.class)
                .addTranslator(TestObjectTranslator.getTranslator())
                .addTranslator(TestComplexObjectTranslator.getTranslator())
                .setTranslationEngineBuilder(TestTranslationEngine.builder())
                .build();

        List<TestAppObject> expectedObjects = TestObjectUtil.getListOfAppObjects(2);

        translationController.writeOutput(expectedObjects.get(0), 1);
        translationController.writeOutput(expectedObjects.get(1), 2);

        translationController.readInput();

        assertEquals(2, translationController.getObjects().size());

        List<TestAppObject> actualObjects = translationController.getObjects(TestAppObject.class);

        assertEquals(2, actualObjects.size());

        assertTrue(actualObjects.containsAll(expectedObjects));

        // preconditions
        ContractException contractException = assertThrows(ContractException.class, () -> {
            translationController.getObjects(TestInputObject.class);
        });

        assertEquals(CoreTranslationError.UNKNOWN_CLASSREF, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = TranslationController.class, name = "getObjects", args = {})
    public void testGetObjects() throws IOException {
        String fileName = "GetObjects_1-testOutput.json";
        String fileName2 = "GetObjects_2-testOutput.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);
        TestResourceHelper.createTestOutputFile(filePath, fileName2);

        TranslationController translationController = TranslationController.builder()
                .addOutputFilePath(filePath.resolve(fileName), TestAppObject.class, 1)
                .addOutputFilePath(filePath.resolve(fileName2), TestAppObject.class, 2)
                .addInputFilePath(filePath.resolve(fileName), TestInputObject.class)
                .addInputFilePath(filePath.resolve(fileName2), TestInputObject.class)
                .addTranslator(TestObjectTranslator.getTranslator())
                .addTranslator(TestComplexObjectTranslator.getTranslator())
                .setTranslationEngineBuilder(TestTranslationEngine.builder())
                .build();

        List<TestAppObject> expectedObjects = TestObjectUtil.getListOfAppObjects(2);

        translationController.writeOutput(expectedObjects.get(0), 1);
        translationController.writeOutput(expectedObjects.get(1), 2);

        translationController.readInput();

        assertEquals(2, translationController.getObjects().size());

        List<Object> actualObjects = translationController.getObjects();

        assertEquals(2, actualObjects.size());

        assertTrue(actualObjects.containsAll(expectedObjects));
    }

    @Test
    @UnitTestForCoverage
    public void testGetOrderedTranslators() {

        TranslationController translationController = TranslationController.builder()
                .addTranslator(TestObjectTranslator.getTranslator())
                .addTranslator(TestComplexObjectTranslator.getTranslator())
                .setTranslationEngineBuilder(TestTranslationEngine.builder())
                .build();

        List<Translator> expectedList = new ArrayList<>();
        expectedList.add(TestComplexObjectTranslator.getTranslator());
        expectedList.add(TestObjectTranslator.getTranslator());

        List<Translator> actualList = translationController.getOrderedTranslators();

        assertEquals(expectedList, actualList);

        // preconditions

        // duplicate translator in the graph

        ContractException contractException = assertThrows(ContractException.class, () -> {
            MutableGraph<TranslatorId, Object> mutableGraph = new MutableGraph<>();
            Map<TranslatorId, Translator> translatorMap = new LinkedHashMap<>();
            mutableGraph.addNode(TestObjectTranslatorId.TRANSLATOR_ID);
            translationController.addNodes(mutableGraph, translatorMap);
        });

        assertEquals(CoreTranslationError.DUPLICATE_TRANSLATOR, contractException.getErrorType());

        // missing translator
        contractException = assertThrows(ContractException.class, () -> {
            MutableGraph<TranslatorId, Object> mutableGraph = new MutableGraph<>();
            Map<TranslatorId, Translator> translatorMap = new LinkedHashMap<>();

            // call normally
            translationController.getOrderedTranslators(mutableGraph, translatorMap);
            // remove a mapping
            translatorMap.remove(TestComplexObjectTranslatorId.TRANSLATOR_ID);
            TranslatorId thirdId = new TranslatorId() {
            };
            mutableGraph.addNode(thirdId);
            mutableGraph.addEdge(new Object(), thirdId, TestComplexObjectTranslatorId.TRANSLATOR_ID);
            translationController.checkForMissingTranslators(mutableGraph, translatorMap);
        });

        assertEquals(CoreTranslationError.MISSING_TRANSLATOR, contractException.getErrorType());

        // cyclic graph
        contractException = assertThrows(ContractException.class, () -> {
            MutableGraph<TranslatorId, Object> mutableGraph = new MutableGraph<>();
            Map<TranslatorId, Translator> translatorMap = new LinkedHashMap<>();

            // call normally
            translationController.getOrderedTranslators(mutableGraph, translatorMap);
            mutableGraph.addEdge(new Object(), TestComplexObjectTranslatorId.TRANSLATOR_ID,
                    TestObjectTranslatorId.TRANSLATOR_ID);
            TranslatorId thirdId = new TranslatorId() {
            };
            TranslatorId fourthId = new TranslatorId() {
            };
            mutableGraph.addNode(thirdId);
            mutableGraph.addNode(fourthId);
            mutableGraph.addEdge(new Object(), thirdId, fourthId);
            mutableGraph.addEdge(new Object(), fourthId, thirdId);
            translationController.checkForCyclicGraph(mutableGraph);
        });

        assertEquals(CoreTranslationError.CIRCULAR_TRANSLATOR_DEPENDENCIES, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = TranslationController.class, name = "builder", args = {})
    public void testBuilder() {
        assertNotNull(TranslationController.builder());
    }

    @Test
    @UnitTestMethod(target = TranslationController.Builder.class, name = "build", args = {})
    public void testBuild() {
        TranslationController translationController = TranslationController.builder()
                .setTranslationEngineBuilder(TestTranslationEngine.builder())
                .build();

        assertNotNull(translationController);

        // preconditions
        ContractException contractException = assertThrows(ContractException.class, () -> {
            TranslationController.builder()
                    .build();
        });

        assertEquals(CoreTranslationError.NULL_TRANSLATION_ENGINE_BUILDER, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = TranslationController.Builder.class, name = "addInputFilePath", args = { Path.class,
            Class.class })
    public void testAddInputFilePath() {
        String fileName = "addInputFilePath-testOutput.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);

        assertDoesNotThrow(() -> TranslationController.builder()
                .addInputFilePath(filePath.resolve(fileName), TestInputObject.class)
                .setTranslationEngineBuilder(TestTranslationEngine.builder())
                .build());

        // preconditions
        ContractException contractException = assertThrows(ContractException.class, () -> {
            TranslationController.builder()
                    .addInputFilePath(null, TestInputObject.class);
        });

        assertEquals(CoreTranslationError.NULL_PATH, contractException.getErrorType());

        contractException = assertThrows(ContractException.class, () -> {
            TranslationController.builder()
                    .addInputFilePath(filePath.resolve(fileName), null);
        });

        assertEquals(CoreTranslationError.NULL_CLASS_REF, contractException.getErrorType());

        contractException = assertThrows(ContractException.class, () -> {
            TranslationController.builder()
                    .addInputFilePath(filePath.resolve(fileName), TestInputObject.class)
                    .addInputFilePath(filePath.resolve(fileName), TestInputObject.class);
        });

        assertEquals(CoreTranslationError.DUPLICATE_INPUT_PATH, contractException.getErrorType());

        contractException = assertThrows(ContractException.class, () -> {
            TranslationController.builder()
                    .addInputFilePath(filePath.resolve("badPath"), TestInputObject.class);
        });

        assertEquals(CoreTranslationError.INVALID_INPUT_PATH, contractException.getErrorType());

    }

    @Test
    @UnitTestMethod(target = TranslationController.Builder.class, name = "addOutputFilePath", args = { Path.class,
            Class.class })
    public void testAddOutputFilePath() {
        String fileName = "addOutputFilePath1-testOutput.json";
        String fileName2 = "addOutputFilePath2-testOutput.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);

        assertDoesNotThrow(() -> TranslationController.builder()
                .addOutputFilePath(filePath.resolve(fileName), TestAppObject.class)
                .setTranslationEngineBuilder(TestTranslationEngine.builder())
                .build());

        // preconditions
        ContractException contractException = assertThrows(ContractException.class, () -> {
            TranslationController.builder()
                    .addOutputFilePath(null, TestAppObject.class);
        });

        assertEquals(CoreTranslationError.NULL_PATH, contractException.getErrorType());

        contractException = assertThrows(ContractException.class, () -> {
            TranslationController.builder()
                    .addOutputFilePath(filePath.resolve(fileName), null);
        });

        assertEquals(CoreTranslationError.NULL_CLASS_REF, contractException.getErrorType());

        contractException = assertThrows(ContractException.class, () -> {
            TranslationController.builder()
                    .addOutputFilePath(filePath.resolve(fileName), TestAppObject.class)
                    .addOutputFilePath(filePath.resolve(fileName), TestAppObject.class);
        });

        assertEquals(CoreTranslationError.DUPLICATE_OUTPUT_PATH, contractException.getErrorType());

        contractException = assertThrows(ContractException.class, () -> {
            TranslationController.builder()
                    .addOutputFilePath(filePath.resolve(fileName), TestAppObject.class)
                    .addOutputFilePath(filePath.resolve(fileName2), TestAppObject.class);
        });

        assertEquals(CoreTranslationError.DUPLICATE_CLASSREF_SCENARIO_PAIR, contractException.getErrorType());

        contractException = assertThrows(ContractException.class, () -> {
            TranslationController.builder()
                    .addOutputFilePath(filePath.resolve("badpath").resolve(fileName2), TestAppObject.class);
        });

        assertEquals(CoreTranslationError.INVALID_OUTPUT_PATH, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = TranslationController.Builder.class, name = "addOutputFilePath", args = { Path.class,
            Class.class,
            Integer.class })
    public void testAddOutputFilePath_ScenarioId() {
        // Tested by testAddOutputFilePath, which internally calls
        // addOutputFilePath(path, class, 0)
    }

    @Test
    @UnitTestMethod(target = TranslationController.Builder.class, name = "addParentChildClassRelationship", args = {
            Class.class, Class.class })
    public void testAddParentChildClassRelationship() {
        TranslationController.builder().addParentChildClassRelationship(TestAppObject.class, Object.class);

        // preconditions
        ContractException contractException = assertThrows(ContractException.class, () -> {
            TranslationController.builder()
                    .addParentChildClassRelationship(null, Object.class);
        });

        assertEquals(CoreTranslationError.NULL_CLASS_REF, contractException.getErrorType());

        contractException = assertThrows(ContractException.class, () -> {
            TranslationController.builder()
                    .addParentChildClassRelationship(TestAppObject.class, null);
        });

        assertEquals(CoreTranslationError.NULL_CLASS_REF, contractException.getErrorType());

        contractException = assertThrows(ContractException.class, () -> {
            TranslationController.builder()
                    .addParentChildClassRelationship(TestAppObject.class, Object.class)
                    .addParentChildClassRelationship(TestAppObject.class, Object.class);
        });

        assertEquals(CoreTranslationError.DUPLICATE_CLASSREF, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = TranslationController.Builder.class, name = "addTranslator", args = { Translator.class })
    public void testAddTranslator() {
        TranslationController.builder().addTranslator(TestObjectTranslator.getTranslator());

        // preconditions
        ContractException contractException = assertThrows(ContractException.class, () -> {
            TranslationController.builder()
                    .addTranslator(null);
        });

        assertEquals(CoreTranslationError.NULL_TRANSLATOR, contractException.getErrorType());

        contractException = assertThrows(ContractException.class, () -> {
            TranslationController.builder()
                    .addTranslator(TestObjectTranslator.getTranslator())
                    .addTranslator(TestObjectTranslator.getTranslator());
        });

        assertEquals(CoreTranslationError.DUPLICATE_TRANSLATOR, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = TranslationController.Builder.class, name = "setTranslationEngineBuilder", args = {
            TranslationEngine.Builder.class })
    public void testSetTransationEngineBuilder() {
        TranslationController.builder().setTranslationEngineBuilder(TestTranslationEngine.builder());

        // preconditions
        ContractException contractException = assertThrows(ContractException.class, () -> {
            TranslationController.builder()
                    .setTranslationEngineBuilder(null);
        });

        assertEquals(CoreTranslationError.NULL_TRANSLATION_ENGINE_BUILDER, contractException.getErrorType());
    }
}
