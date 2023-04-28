package gov.hhs.aspr.translation.core.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.translation.core.support.TestObjectUtil;
import gov.hhs.aspr.translation.core.testsupport.testcomplexobject.TestComplexObjectTranslationSpec;
import gov.hhs.aspr.translation.core.testsupport.testobject.TestAppObject;
import gov.hhs.aspr.translation.core.testsupport.testobject.TestInputObject;
import gov.hhs.aspr.translation.core.testsupport.testobject.TestObjectTranslationSpec;

public class AT_TestTranslationEngine {
    Path basePath = TestResourceHelper.getResourceDir(this.getClass());
    Path filePath = TestResourceHelper.makeTestOutputDir(basePath);

    @Test
    public void testWriteOutput() throws IOException {
        String fileName = "writeOutputFromEngine_1-testOutput.json";
        String fileName2 = "writeOutputFromEngine_2-testOutput.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);
        TestResourceHelper.createTestOutputFile(filePath, fileName2);

        TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();
        TestComplexObjectTranslationSpec complexObjectTranslationSpec = new TestComplexObjectTranslationSpec();
        TestTranslationEngine testTranslationEngine = (TestTranslationEngine) TestTranslationEngine
                .builder()
                .addTranslatorSpec(testObjectTranslationSpec)
                .addTranslatorSpec(complexObjectTranslationSpec)
                .build();

        testTranslationEngine.init();

        TestAppObject expectedAppObject = TestObjectUtil.generateTestAppObject();

        FileWriter fileWriter = new FileWriter(filePath.resolve(fileName).toFile());
        FileReader fileReader = new FileReader(filePath.resolve(fileName).toFile());

        FileWriter fileWriter2 = new FileWriter(filePath.resolve(fileName2).toFile());
        FileReader fileReader2 = new FileReader(filePath.resolve(fileName2).toFile());

        testTranslationEngine.writeOutput(fileWriter, expectedAppObject, Optional.empty());
        TestAppObject actualAppObject = testTranslationEngine.readInput(fileReader, TestInputObject.class);
        assertEquals(expectedAppObject, actualAppObject);

        testTranslationEngine.writeOutput(fileWriter2, TestObjectUtil.getChildAppFromApp(expectedAppObject),
                Optional.of(TestAppObject.class));
        TestAppObject actualAppChildObject = testTranslationEngine.readInput(fileReader2, TestInputObject.class);
        assertEquals(expectedAppObject, actualAppChildObject);

        // preconditions
        // IO error occurs
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> {
            FileWriter fileWriter3 = new FileWriter(filePath.resolve(fileName).toFile());
            // close the file reader
            fileWriter3.close();
            testTranslationEngine.writeOutput(fileWriter3, expectedAppObject, Optional.empty());
        });

        assertTrue(runtimeException.getCause() instanceof IOException);

        filePath.resolve(fileName).toFile().setWritable(true);
    }

    @Test
    public void testReadInput() throws IOException {
        String fileName = "readInputFromEngine_1-testOutput.json";
        String fileName2 = "readInputFromEngine_2-testOutput.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);
        TestResourceHelper.createTestOutputFile(filePath, fileName2);

        TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();
        TestComplexObjectTranslationSpec complexObjectTranslationSpec = new TestComplexObjectTranslationSpec();
        TestTranslationEngine testTranslationEngine = (TestTranslationEngine) TestTranslationEngine
                .builder()
                .addTranslatorSpec(testObjectTranslationSpec)
                .addTranslatorSpec(complexObjectTranslationSpec)
                .build();

        testTranslationEngine.init();

        TestAppObject expectedAppObject = TestObjectUtil.generateTestAppObject();

        FileWriter fileWriter = new FileWriter(filePath.resolve(fileName).toFile());
        FileReader fileReader = new FileReader(filePath.resolve(fileName).toFile());

        FileWriter fileWriter2 = new FileWriter(filePath.resolve(fileName2).toFile());
        FileReader fileReader2 = new FileReader(filePath.resolve(fileName2).toFile());

        testTranslationEngine.writeOutput(fileWriter, expectedAppObject, Optional.empty());
        TestAppObject actualAppObject = testTranslationEngine.readInput(fileReader, TestInputObject.class);
        assertEquals(expectedAppObject, actualAppObject);

        testTranslationEngine.writeOutput(fileWriter2, TestObjectUtil.getChildAppFromApp(expectedAppObject),
                Optional.of(TestAppObject.class));
        TestAppObject actualAppChildObject = testTranslationEngine.readInput(fileReader2, TestInputObject.class);
        assertEquals(expectedAppObject, actualAppChildObject);
    }

    @Test
    public void testBuilder() {
        assertNotNull(TestTranslationEngine
                .builder());
    }

    @Test
    public void testBuild() {
        assertNotNull(TestTranslationEngine
                .builder().build());
    }
}
