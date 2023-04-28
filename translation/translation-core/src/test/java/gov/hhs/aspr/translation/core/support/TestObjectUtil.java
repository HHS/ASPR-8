package gov.hhs.aspr.translation.core.support;

import java.util.ArrayList;
import java.util.List;

import gov.hhs.aspr.translation.core.testsupport.testcomplexobject.TestComplexAppObject;
import gov.hhs.aspr.translation.core.testsupport.testcomplexobject.TestComplexInputObject;
import gov.hhs.aspr.translation.core.testsupport.testobject.TestAppChildObject;
import gov.hhs.aspr.translation.core.testsupport.testobject.TestAppObject;
import gov.hhs.aspr.translation.core.testsupport.testobject.TestInputChildObject;
import gov.hhs.aspr.translation.core.testsupport.testobject.TestInputObject;
import util.random.RandomGeneratorProvider;

public class TestObjectUtil {
    static org.apache.commons.math3.random.RandomGenerator randomGenerator = RandomGeneratorProvider
            .getRandomGenerator(4444833210967964206L);

    public static TestAppObject generateTestAppObject() {

        TestAppObject appObject = new TestAppObject();

        appObject.setTestComplexAppObject(generateTestComplexAppObject());
        appObject.setBool(randomGenerator.nextBoolean());
        appObject.setInteger(randomGenerator.nextInt(1500));
        appObject.setString("readInput" + randomGenerator.nextInt(25));

        return appObject;
    }

    public static TestComplexAppObject generateTestComplexAppObject() {
        TestComplexAppObject complexAppObject = new TestComplexAppObject();

        complexAppObject.setNumEntities(randomGenerator.nextInt(100));
        complexAppObject.setStartTime(randomGenerator.nextDouble() * 15);
        complexAppObject.setTestString("readInput" + randomGenerator.nextInt(15));

        return complexAppObject;
    }

    public static List<TestAppObject> getListOfAppObjects(int num) {
        List<TestAppObject> appObjects = new ArrayList<>();

        for (int i = 0; i < num; i++) {
            appObjects.add(generateTestAppObject());
        }

        return appObjects;
    }

    public static TestInputObject generateTestInputObject() {

        TestInputObject inputObject = new TestInputObject();

        inputObject.setTestComplexInputObject(generateTestComplexInputObject());
        inputObject.setBool(randomGenerator.nextBoolean());
        inputObject.setInteger(randomGenerator.nextInt(1500));
        inputObject.setString("readInput" + randomGenerator.nextInt(25));

        return inputObject;
    }

    public static TestComplexInputObject generateTestComplexInputObject() {
        TestComplexInputObject complexInputObject = new TestComplexInputObject();

        complexInputObject.setNumEntities(randomGenerator.nextInt(100));
        complexInputObject.setStartTime(randomGenerator.nextDouble() * 15);
        complexInputObject.setTestString("readInput" + randomGenerator.nextInt(15));

        return complexInputObject;
    }

    public static List<TestInputObject> getListOfInputObjects(int num) {
        List<TestInputObject> inputObjects = new ArrayList<>();

        for (int i = 0; i < num; i++) {
            inputObjects.add(generateTestInputObject());
        }

        return inputObjects;
    }

    public static TestAppObject getAppFromInput(TestInputObject inputObject) {
        TestAppObject appObject = new TestAppObject();
        TestComplexAppObject complextAppObject = new TestComplexAppObject();

        complextAppObject.setNumEntities(inputObject.getTestComplexInputObject().getNumEntities());
        complextAppObject.setStartTime(inputObject.getTestComplexInputObject().getStartTime());
        complextAppObject.setTestString(inputObject.getTestComplexInputObject().getTestString());

        appObject.setTestComplexAppObject(complextAppObject);
        appObject.setBool(inputObject.isBool());
        appObject.setInteger(inputObject.getInteger());
        appObject.setString(inputObject.getString());

        return appObject;
    }

    public static TestInputObject getInputFromApp(TestAppObject appObject) {
        TestInputObject inputObject = new TestInputObject();
        TestComplexInputObject complextInputObject = new TestComplexInputObject();

        complextInputObject.setNumEntities(appObject.getTestComplexAppObject().getNumEntities());
        complextInputObject.setStartTime(appObject.getTestComplexAppObject().getStartTime());
        complextInputObject.setTestString(appObject.getTestComplexAppObject().getTestString());

        inputObject.setTestComplexInputObject(complextInputObject);
        inputObject.setBool(appObject.isBool());
        inputObject.setInteger(appObject.getInteger());
        inputObject.setString(appObject.getString());

        return inputObject;
    }

    public static TestAppChildObject getChildAppFromApp(TestAppObject appObject) {
        TestAppChildObject childAppObject = new TestAppChildObject();
        TestComplexAppObject complexAppObject = new TestComplexAppObject();

        complexAppObject.setNumEntities(appObject.getTestComplexAppObject().getNumEntities());
        complexAppObject.setStartTime(appObject.getTestComplexAppObject().getStartTime());
        complexAppObject.setTestString(appObject.getTestComplexAppObject().getTestString());

        childAppObject.setTestComplexAppObject(complexAppObject);
        childAppObject.setBool(appObject.isBool());
        childAppObject.setInteger(appObject.getInteger());
        childAppObject.setString(appObject.getString());

        return childAppObject;
    }

    public static TestInputChildObject getChildInputFromInput(TestInputObject inputObject) {
        TestInputChildObject childInputObject = new TestInputChildObject();
        TestComplexInputObject complexInputObject = new TestComplexInputObject();

        complexInputObject.setNumEntities(inputObject.getTestComplexInputObject().getNumEntities());
        complexInputObject.setStartTime(inputObject.getTestComplexInputObject().getStartTime());
        complexInputObject.setTestString(inputObject.getTestComplexInputObject().getTestString());

        childInputObject.setTestComplexInputObject(complexInputObject);
        childInputObject.setBool(inputObject.isBool());
        childInputObject.setInteger(inputObject.getInteger());
        childInputObject.setString(inputObject.getString());

        return childInputObject;
    }
}
