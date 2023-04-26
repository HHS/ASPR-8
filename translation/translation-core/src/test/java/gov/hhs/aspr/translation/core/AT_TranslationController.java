package gov.hhs.aspr.translation.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.translation.core.testsupport.TestResourceHelper;
import gov.hhs.aspr.translation.core.testsupport.TestTranslationEngine;
import gov.hhs.aspr.translation.core.testsupport.testcomplexobject.TestComplexAppObject;
import gov.hhs.aspr.translation.core.testsupport.testcomplexobject.TestComplexTranslator;
import gov.hhs.aspr.translation.core.testsupport.testobject.TestAppObject;
import gov.hhs.aspr.translation.core.testsupport.testobject.TestInputObject;
import gov.hhs.aspr.translation.core.testsupport.testobject.TestTranslator;
import util.annotations.UnitTag;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

public class AT_TranslationController {
    Path basePath = TestResourceHelper.getResourceDir(this.getClass());
    Path filePath = TestResourceHelper.makeTestOutputDir(basePath);
    org.apache.commons.math3.random.RandomGenerator randomGenerator = RandomGeneratorProvider
            .getRandomGenerator(4444833210967964206L);

    private TestAppObject generateTestAppObject() {

        TestAppObject appObject = new TestAppObject();
        TestComplexAppObject complexAppObject = new TestComplexAppObject();

        complexAppObject.setNumEntities(randomGenerator.nextInt(100));
        complexAppObject.setStartTime(randomGenerator.nextDouble() * 15);
        complexAppObject.setTestString("readInput" + randomGenerator.nextInt(15));

        appObject.setTestComplexAppObject(complexAppObject);
        appObject.setBool(randomGenerator.nextBoolean());
        appObject.setInteger(randomGenerator.nextInt(1500));
        appObject.setString("readInput" + randomGenerator.nextInt(25));

        return appObject;
    }

    private List<TestAppObject> getListOfAppObjects(int num) {
        List<TestAppObject> appObjects = new ArrayList<>();

        for (int i = 0; i < num; i++) {
            appObjects.add(generateTestAppObject());
        }

        return appObjects;
    }

    @Test
    @UnitTestMethod(target = TranslationController.class, name = "readInput", args = {})
    public void testReadInput() {
        String fileName = "readInput-testOutput.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);

        TranslationController translationController = TranslationController.builder()
                .addInputFilePath(filePath.resolve(fileName), TestInputObject.class)
                .addOutputFilePath(filePath.resolve(fileName), TestAppObject.class)
                .addTranslator(TestTranslator.getTestTranslator())
                .addTranslator(TestComplexTranslator.getTestComplexTranslator())
                .setTranslationEngineBuilder(TestTranslationEngine.builder())
                .build();

        TestAppObject expectedAppObject = generateTestAppObject();

        translationController.writeOutput(expectedAppObject);

        translationController.readInput();

        assertTrue(translationController.getObjects().size() == 1);

        TestAppObject actualTestAppObject = translationController.getFirstObject(TestAppObject.class);

        assertEquals(expectedAppObject, actualTestAppObject);
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
                .addTranslator(TestTranslator.getTestTranslator())
                .addTranslator(TestComplexTranslator.getTestComplexTranslator())
                .setTranslationEngineBuilder(TestTranslationEngine.builder())
                .build();

        List<Object> outputObjects = new ArrayList<>();

        outputObjects.add(generateTestAppObject());
        outputObjects.add(generateTestAppObject().getTestComplexAppObject());
        translationController.writeOutput(outputObjects);

        // preconditions
        ContractException contractException = assertThrows(ContractException.class, () -> {
            List<Object> invalidList = new ArrayList<>();

            invalidList.add(new TestInputObject());
            translationController.writeOutput(invalidList);
        });

        assertEquals(CoreTranslationError.INVALID_OUTPUT_CLASSREF, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = TranslationController.class, name = "writeOutput", args = { List.class, Integer.class })
    public void testWriteOutput_List_ScenarioId() {
        String fileName = "WriteOutput_List_ScenarioId_1-testOutput.json";
        String fileName2 = "WriteOutput_List_ScenarioId_2-testOutput.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);
        TestResourceHelper.createTestOutputFile(filePath, fileName2);

        TranslationController translationController = TranslationController.builder()
                .addOutputFilePath(filePath.resolve(fileName), TestAppObject.class, 1)
                .addOutputFilePath(filePath.resolve(fileName2), TestComplexAppObject.class, 1)
                .addTranslator(TestTranslator.getTestTranslator())
                .addTranslator(TestComplexTranslator.getTestComplexTranslator())
                .setTranslationEngineBuilder(TestTranslationEngine.builder())
                .build();

        List<Object> outputObjects = new ArrayList<>();

        outputObjects.add(generateTestAppObject());
        outputObjects.add(generateTestAppObject().getTestComplexAppObject());
        translationController.writeOutput(outputObjects, 1);

        // preconditions
        ContractException contractException = assertThrows(ContractException.class, () -> {
            List<Object> invalidList = new ArrayList<>();

            invalidList.add(new TestInputObject());
            translationController.writeOutput(invalidList, 1);
        });

        assertEquals(CoreTranslationError.INVALID_OUTPUT_CLASSREF, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = TranslationController.class, name = "writeOutput", args = { Object.class })
    public void testWriteOutput() {
        String fileName = "writeOutput-testOutput.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);

        TranslationController translationController = TranslationController.builder()
                .addOutputFilePath(filePath.resolve(fileName), TestAppObject.class)
                .addTranslator(TestTranslator.getTestTranslator())
                .addTranslator(TestComplexTranslator.getTestComplexTranslator())
                .setTranslationEngineBuilder(TestTranslationEngine.builder())
                .build();

        TestAppObject expectedAppObject = generateTestAppObject();

        translationController.writeOutput(expectedAppObject);

        // preconditions
        ContractException contractException = assertThrows(ContractException.class, () -> {

            translationController.writeOutput(new TestInputObject());
        });

        assertEquals(CoreTranslationError.INVALID_OUTPUT_CLASSREF, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = TranslationController.class, name = "writeOutput", args = { Object.class, Integer.class })
    public void testWriteOutput_ScenarioId() {
        String fileName = "writeOutput_ScenarioId-testOutput.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);

        TranslationController translationController = TranslationController.builder()
                .addOutputFilePath(filePath.resolve(fileName), TestAppObject.class, 1)
                .addTranslator(TestTranslator.getTestTranslator())
                .addTranslator(TestComplexTranslator.getTestComplexTranslator())
                .setTranslationEngineBuilder(TestTranslationEngine.builder())
                .build();

        TestAppObject expectedAppObject = generateTestAppObject();

        translationController.writeOutput(expectedAppObject, 1);

        // preconditions
        ContractException contractException = assertThrows(ContractException.class, () -> {

            translationController.writeOutput(new TestInputObject(), 1);
        });

        assertEquals(CoreTranslationError.INVALID_OUTPUT_CLASSREF, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = TranslationController.class, name = "getFirstObject", args = { Class.class })
    public void testGetFirstObject() {
        String fileName = "getFirstObject-testOutput.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);

        TranslationController translationController = TranslationController.builder()
                .addInputFilePath(filePath.resolve(fileName), TestInputObject.class)
                .addOutputFilePath(filePath.resolve(fileName), TestAppObject.class)
                .addTranslator(TestTranslator.getTestTranslator())
                .addTranslator(TestComplexTranslator.getTestComplexTranslator())
                .setTranslationEngineBuilder(TestTranslationEngine.builder())
                .build();

        TestAppObject expectedAppObject = generateTestAppObject();

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
    public void testGetObjects_OfClass() {
        String fileName = "GetObjects_OfClass_1-testOutput.json";
        String fileName2 = "GetObjects_OfClass_2-testOutput.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);
        TestResourceHelper.createTestOutputFile(filePath, fileName2);

        TranslationController translationController = TranslationController.builder()
                .addOutputFilePath(filePath.resolve(fileName), TestAppObject.class, 1)
                .addOutputFilePath(filePath.resolve(fileName2), TestAppObject.class, 2)
                .addInputFilePath(filePath.resolve(fileName), TestInputObject.class)
                .addInputFilePath(filePath.resolve(fileName2), TestInputObject.class)
                .addTranslator(TestTranslator.getTestTranslator())
                .addTranslator(TestComplexTranslator.getTestComplexTranslator())
                .setTranslationEngineBuilder(TestTranslationEngine.builder())
                .build();

        List<TestAppObject> expectedObjects = getListOfAppObjects(2);

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
    public void testGetObjects() {
        String fileName = "GetObjects_1-testOutput.json";
        String fileName2 = "GetObjects_2-testOutput.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);
        TestResourceHelper.createTestOutputFile(filePath, fileName2);

        TranslationController translationController = TranslationController.builder()
                .addOutputFilePath(filePath.resolve(fileName), TestAppObject.class, 1)
                .addOutputFilePath(filePath.resolve(fileName2), TestAppObject.class, 2)
                .addInputFilePath(filePath.resolve(fileName), TestInputObject.class)
                .addInputFilePath(filePath.resolve(fileName2), TestInputObject.class)
                .addTranslator(TestTranslator.getTestTranslator())
                .addTranslator(TestComplexTranslator.getTestComplexTranslator())
                .setTranslationEngineBuilder(TestTranslationEngine.builder())
                .build();

        List<TestAppObject> expectedObjects = getListOfAppObjects(2);

        translationController.writeOutput(expectedObjects.get(0), 1);
        translationController.writeOutput(expectedObjects.get(1), 2);

        translationController.readInput();

        assertEquals(2, translationController.getObjects().size());

        List<Object> actualObjects = translationController.getObjects();

        assertEquals(2, actualObjects.size());

        assertTrue(actualObjects.containsAll(expectedObjects));
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
    @UnitTestMethod(target = TranslationController.Builder.class, name = "build", args = { Path.class, Class.class })
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
    @UnitTestMethod(target = TranslationController.Builder.class, name = "build", args = { Path.class, Class.class })
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
    @UnitTestMethod(target = TranslationController.Builder.class, name = "build", args = { Path.class, Class.class,
            Integer.class }, tags = { UnitTag.LOCAL_PROXY })
    public void testAddOutputFilePath_ScenarioId() {
        // Tested by testAddOutputFilePath, which internally calls
        // addOutputFilePath(path, class, 0)
    }

    @Test
    @UnitTestMethod(target = TranslationController.Builder.class, name = "build", args = { Class.class, Class.class })
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
    @UnitTestMethod(target = TranslationController.Builder.class, name = "build", args = { Translator.class })
    public void testAddTranslator() {
        TranslationController.builder().addTranslator(TestTranslator.getTestTranslator());

        // preconditions
        ContractException contractException = assertThrows(ContractException.class, () -> {
            TranslationController.builder()
                    .addTranslator(null);
        });

        assertEquals(CoreTranslationError.NULL_TRANSLATOR, contractException.getErrorType());

        contractException = assertThrows(ContractException.class, () -> {
            TranslationController.builder()
                    .addTranslator(TestTranslator.getTestTranslator())
                    .addTranslator(TestTranslator.getTestTranslator());
        });

        assertEquals(CoreTranslationError.DUPLICATE_TRANSLATOR, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = TranslationController.Builder.class, name = "build", args = {
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
