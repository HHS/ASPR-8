package gov.hhs.aspr.translation.core.testsupport.testobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.translation.core.testsupport.TestObjectUtil;
import gov.hhs.aspr.translation.core.testsupport.TestTranslationEngine;
import gov.hhs.aspr.translation.core.testsupport.testcomplexobject.TestComplexObjectTranslationSpec;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_TestObjectTranslationSpec {

    @Test
    @UnitTestConstructor(target = TestObjectTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new TestObjectTranslationSpec());
    }

    @Test
    @UnitTestMethod(target = TestObjectTranslationSpec.class, name = "convertInputObject", args = { Object.class })
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
    @UnitTestMethod(target = TestObjectTranslationSpec.class, name = "convertAppObject", args = { Object.class })
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
