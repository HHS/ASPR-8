package gov.hhs.aspr.translation.core.testsupport.testcomplexobject;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.translation.core.testsupport.TestObjectUtil;
import gov.hhs.aspr.translation.core.testsupport.TestTranslationEngine;
import gov.hhs.aspr.translation.core.testsupport.testobject.TestObjectTranslationSpec;
import util.annotations.UnitTestMethod;

public class AT_TestComplexObjectTranslationSpec {

    @Test
    @UnitTestMethod(target = TestComplexObjectTranslationSpec.class, name = "convertInputObject", args = {
            Object.class })
    public void testConvertInputObject() {
        TestComplexInputObject testComplexInputObject = TestObjectUtil.generateTestComplexInputObject();
        TestComplexAppObject expectedComplexAppObject = TestObjectUtil
                .getComplexAppFromComplexInput(testComplexInputObject);

        TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();
        TestComplexObjectTranslationSpec complexObjectTranslationSpec = new TestComplexObjectTranslationSpec();
        TestTranslationEngine testTranslationEngine = (TestTranslationEngine) TestTranslationEngine
                .builder()
                .addTranslationSpec(testObjectTranslationSpec)
                .addTranslationSpec(complexObjectTranslationSpec)
                .build();

        testTranslationEngine.init();

        TestComplexAppObject actualComplexAppObject = complexObjectTranslationSpec
                .convertInputObject(testComplexInputObject);

        assertEquals(expectedComplexAppObject, actualComplexAppObject);
    }

    @Test
    @UnitTestMethod(target = TestComplexObjectTranslationSpec.class, name = "convertAppObject", args = {
            Object.class })
    public void testConvertAppObject() {
        TestComplexAppObject testComplexAppObject = TestObjectUtil.generateTestComplexAppObject();
        TestComplexInputObject expectedComplexInputObject = TestObjectUtil
                .getComplexInputFromComplexApp(testComplexAppObject);

        TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();
        TestComplexObjectTranslationSpec complexObjectTranslationSpec = new TestComplexObjectTranslationSpec();
        TestTranslationEngine testTranslationEngine = (TestTranslationEngine) TestTranslationEngine
                .builder()
                .addTranslationSpec(testObjectTranslationSpec)
                .addTranslationSpec(complexObjectTranslationSpec)
                .build();

        testTranslationEngine.init();

        TestComplexInputObject actualComplexInputObject = complexObjectTranslationSpec
                .convertAppObject(testComplexAppObject);

        assertEquals(expectedComplexInputObject, actualComplexInputObject);
    }

    @Test
    @UnitTestMethod(target = TestComplexObjectTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        TestComplexObjectTranslationSpec testComplexObjectTranslationSpec = new TestComplexObjectTranslationSpec();

        assertEquals(TestComplexAppObject.class, testComplexObjectTranslationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = TestComplexObjectTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        TestComplexObjectTranslationSpec testComplexObjectTranslationSpec = new TestComplexObjectTranslationSpec();

        assertEquals(TestComplexInputObject.class, testComplexObjectTranslationSpec.getInputObjectClass());
    }

}
