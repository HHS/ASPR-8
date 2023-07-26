package gov.hhs.aspr.ms.taskit.core.testsupport.testobject.translationSpecs;

import gov.hhs.aspr.ms.taskit.core.testsupport.TestTranslationSpec;
import gov.hhs.aspr.ms.taskit.core.testsupport.testobject.TestAppObject;
import gov.hhs.aspr.ms.taskit.core.testsupport.testobject.TestInputObject;

public class TestObjectTranslationSpec extends TestTranslationSpec<TestInputObject, TestAppObject> {

    @Override
    protected TestAppObject convertInputObject(TestInputObject inputObject) {
        TestAppObject testAppObject = new TestAppObject();

        testAppObject.setBool(inputObject.isBool());
        testAppObject.setInteger(inputObject.getInteger());
        testAppObject.setString(inputObject.getString());
        testAppObject
                .setTestComplexAppObject(this.translationEngine.convertObject(inputObject.getTestComplexInputObject()));

        return testAppObject;
    }

    @Override
    protected TestInputObject convertAppObject(TestAppObject appObject) {
        TestInputObject testInputObject = new TestInputObject();

        testInputObject.setBool(appObject.isBool());
        testInputObject.setInteger(appObject.getInteger());
        testInputObject.setString(appObject.getString());
        testInputObject
                .setTestComplexInputObject(this.translationEngine.convertObject(appObject.getTestComplexAppObject()));

        return testInputObject;
    }

    @Override
    public Class<TestAppObject> getAppObjectClass() {
        return TestAppObject.class;
    }

    @Override
    public Class<TestInputObject> getInputObjectClass() {
        return TestInputObject.class;
    }

}
