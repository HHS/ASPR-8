package gov.hhs.aspr.translation.protobuf.core.testsupport;

import java.util.ArrayList;
import java.util.List;

import gov.hhs.aspr.translation.core.testsupport.testcomplexobject.app.TestComplexAppObject;
import gov.hhs.aspr.translation.core.testsupport.testobject.app.TestAppChildObject;
import gov.hhs.aspr.translation.core.testsupport.testobject.app.TestAppObject;
import gov.hhs.aspr.translation.protobuf.core.testsupport.testcomplexobject.input.TestComplexInputObject;
import gov.hhs.aspr.translation.protobuf.core.testsupport.testobject.input.TestInputObject;
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

        TestInputObject inputObject = TestInputObject.newBuilder()
                .setTestComplexInputObject(generateTestComplexInputObject())
                .setBool(randomGenerator.nextBoolean())
                .setInteger(randomGenerator.nextInt(1500))
                .setString("readInput" + randomGenerator.nextInt(25))
                .build();

        return inputObject;
    }

    public static TestComplexInputObject generateTestComplexInputObject() {
        TestComplexInputObject complexInputObject = TestComplexInputObject.newBuilder()
                .setNumEntities(randomGenerator.nextInt(100))
                .setStartTime(randomGenerator.nextDouble() * 15)
                .setTestString("readInput" + randomGenerator.nextInt(15))
                .build();

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
        TestComplexAppObject complextAppObject = getComplexAppFromComplexInput(inputObject.getTestComplexInputObject());

        appObject.setTestComplexAppObject(complextAppObject);
        appObject.setBool(inputObject.getBool());
        appObject.setInteger(inputObject.getInteger());
        appObject.setString(inputObject.getString());

        return appObject;
    }

    public static TestInputObject getInputFromApp(TestAppObject appObject) {
        TestComplexInputObject complextInputObject = getComplexInputFromComplexApp(appObject.getTestComplexAppObject());
        TestInputObject inputObject = TestInputObject.newBuilder()
                .setTestComplexInputObject(complextInputObject)
                .setBool(appObject.isBool())
                .setInteger(appObject.getInteger())
                .setString(appObject.getString())
                .build();

        return inputObject;
    }

    public static TestAppChildObject getChildAppFromApp(TestAppObject appObject) {
        TestAppChildObject childAppObject = new TestAppChildObject();

        childAppObject.setTestComplexAppObject(appObject.getTestComplexAppObject());
        childAppObject.setBool(appObject.isBool());
        childAppObject.setInteger(appObject.getInteger());
        childAppObject.setString(appObject.getString());

        return childAppObject;
    }

    public static TestComplexAppObject getComplexAppFromComplexInput(TestComplexInputObject inputObject) {
        TestComplexAppObject complextAppObject = new TestComplexAppObject();

        complextAppObject.setNumEntities(inputObject.getNumEntities());
        complextAppObject.setStartTime(inputObject.getStartTime());
        complextAppObject.setTestString(inputObject.getTestString());

        return complextAppObject;
    }

    public static TestComplexInputObject getComplexInputFromComplexApp(TestComplexAppObject appObject) {
        TestComplexInputObject complextInputObject = TestComplexInputObject.newBuilder()
                .setNumEntities(appObject.getNumEntities())
                .setStartTime(appObject.getStartTime())
                .setTestString(appObject.getTestString())
                .build();

        return complextInputObject;
    }
}
