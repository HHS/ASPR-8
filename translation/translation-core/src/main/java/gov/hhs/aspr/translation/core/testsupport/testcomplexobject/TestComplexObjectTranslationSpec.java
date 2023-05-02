package gov.hhs.aspr.translation.core.testsupport.testcomplexobject;

import gov.hhs.aspr.translation.core.testsupport.TestTranslationSpec;

public class TestComplexObjectTranslationSpec extends TestTranslationSpec<TestComplexInputObject, TestComplexAppObject> {

    @Override
    protected TestComplexAppObject convertInputObject(TestComplexInputObject inputObject) {
        TestComplexAppObject testAppObject = new TestComplexAppObject();

        testAppObject.setNumEntities(inputObject.getNumEntities());
        testAppObject.setStartTime(inputObject.getStartTime());
        testAppObject.setTestString(inputObject.getTestString());

        return testAppObject;
    }

    @Override
    protected TestComplexInputObject convertAppObject(TestComplexAppObject appObject) {
        TestComplexInputObject testInputObject = new TestComplexInputObject();

        testInputObject.setNumEntities(appObject.getNumEntities());
        testInputObject.setStartTime(appObject.getStartTime());
        testInputObject.setTestString(appObject.getTestString());

        return testInputObject;
    }

    @Override
    public Class<TestComplexAppObject> getAppObjectClass() {
        return TestComplexAppObject.class;
    }

    @Override
    public Class<TestComplexInputObject> getInputObjectClass() {
        return TestComplexInputObject.class;
    }

}
