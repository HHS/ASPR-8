package gov.hhs.aspr.ms.taskit.core.testsupport.testobject.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.taskit.core.testsupport.TestObjectUtil;
import gov.hhs.aspr.ms.taskit.core.testsupport.TestTranslationEngine;
import gov.hhs.aspr.ms.taskit.core.testsupport.testcomplexobject.translationSpecs.TestComplexObjectTranslationSpec;
import gov.hhs.aspr.ms.taskit.core.testsupport.testobject.TestAppObject;
import gov.hhs.aspr.ms.taskit.core.testsupport.testobject.input.TestInputObject;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_TestObjectTranslationSpec {

    @Test
    @UnitTestConstructor(target = TestObjectTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new TestObjectTranslationSpec());
    }

    @Test
    @UnitTestForCoverage
    public void testConvertInputObject() {
        TestInputObject testInputObject = TestObjectUtil.generateTestInputObject();
        TestAppObject expectedAppObject = TestObjectUtil.getAppFromInput(testInputObject);

        TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();
        TestComplexObjectTranslationSpec complexObjectTranslationSpec = new TestComplexObjectTranslationSpec();
        TestTranslationEngine testTranslationEngine = TestTranslationEngine
                .builder()
                .addTranslationSpec(testObjectTranslationSpec)
                .addTranslationSpec(complexObjectTranslationSpec)
                .build();

        testTranslationEngine.init();

        TestAppObject actualAppObject = testObjectTranslationSpec.convertInputObject(testInputObject);

        assertEquals(expectedAppObject, actualAppObject);
    }

    @Test
    @UnitTestForCoverage
    public void testConvertAppObject() {
        TestAppObject testAppObject = TestObjectUtil.generateTestAppObject();
        TestInputObject expectedInputObject = TestObjectUtil.getInputFromApp(testAppObject);

        TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();
        TestComplexObjectTranslationSpec complexObjectTranslationSpec = new TestComplexObjectTranslationSpec();
        TestTranslationEngine testTranslationEngine = TestTranslationEngine
                .builder()
                .addTranslationSpec(testObjectTranslationSpec)
                .addTranslationSpec(complexObjectTranslationSpec)
                .build();

        testTranslationEngine.init();

        TestInputObject actualInputObject = testObjectTranslationSpec.convertAppObject(testAppObject);

        assertEquals(expectedInputObject, actualInputObject);
    }

    @Test
    @UnitTestMethod(target = TestObjectTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();

        assertEquals(TestAppObject.class, testObjectTranslationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = TestObjectTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();

        assertEquals(TestInputObject.class, testObjectTranslationSpec.getInputObjectClass());
    }

}
