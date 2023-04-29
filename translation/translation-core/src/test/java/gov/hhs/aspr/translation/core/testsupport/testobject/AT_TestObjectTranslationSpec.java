package gov.hhs.aspr.translation.core.testsupport.testobject;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.translation.core.testsupport.TestObjectUtil;
import gov.hhs.aspr.translation.core.testsupport.TestTranslationEngine;
import gov.hhs.aspr.translation.core.testsupport.testcomplexobject.TestComplexObjectTranslationSpec;

public class AT_TestObjectTranslationSpec {

    @Test
    public void testConvertInputObject() {
        TestInputObject testInputObject = TestObjectUtil.generateTestInputObject();
        TestAppObject expectedAppObject = TestObjectUtil.getAppFromInput(testInputObject);

        TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();
        TestComplexObjectTranslationSpec complexObjectTranslationSpec = new TestComplexObjectTranslationSpec();
        TestTranslationEngine testTranslationEngine = (TestTranslationEngine) TestTranslationEngine
                .builder()
                .addTranslationSpec(testObjectTranslationSpec)
                .addTranslationSpec(complexObjectTranslationSpec)
                .build();

        testTranslationEngine.init();

        TestAppObject actualAppObject = testObjectTranslationSpec.convertInputObject(testInputObject);

        assertEquals(expectedAppObject, actualAppObject);
    }

    @Test
    public void testConvertAppObject() {
        TestAppObject testAppObject = TestObjectUtil.generateTestAppObject();
        TestInputObject expectedInputObject = TestObjectUtil.getInputFromApp(testAppObject);

        TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();
        TestComplexObjectTranslationSpec complexObjectTranslationSpec = new TestComplexObjectTranslationSpec();
        TestTranslationEngine testTranslationEngine = (TestTranslationEngine) TestTranslationEngine
                .builder()
                .addTranslationSpec(testObjectTranslationSpec)
                .addTranslationSpec(complexObjectTranslationSpec)
                .build();

        testTranslationEngine.init();

        TestInputObject actualInputObject = testObjectTranslationSpec.convertAppObject(testAppObject);

        assertEquals(expectedInputObject, actualInputObject);
    }

    @Test
    public void testGetAppObjectClass() {
        TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();

        assertEquals(TestAppObject.class, testObjectTranslationSpec.getAppObjectClass());
    }

    @Test
    public void testGetInputObjectClass() {
        TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();

        assertEquals(TestInputObject.class, testObjectTranslationSpec.getInputObjectClass());
    }

}
