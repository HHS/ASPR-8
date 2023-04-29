package gov.hhs.aspr.translation.core.testsupport.testobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.translation.core.testsupport.TestObjectUtil;
import gov.hhs.aspr.translation.core.testsupport.testcomplexobject.TestComplexAppObject;

public class AT_TestAppObject {

    @Test
    public void testSetInteger() {
        TestAppObject testAppObject = new TestAppObject();

        testAppObject.setInteger(15);

        assertEquals(15, testAppObject.getInteger());
    }

    @Test
    public void testGetInteger() {
        TestAppObject testAppObject = new TestAppObject();

        testAppObject.setInteger(150);

        assertEquals(150, testAppObject.getInteger());
    }

    @Test
    public void testSetBool() {
        TestAppObject testAppObject = new TestAppObject();

        testAppObject.setBool(false);

        assertEquals(false, testAppObject.isBool());
    }

    @Test
    public void testIsBool() {
        TestAppObject testAppObject = new TestAppObject();

        testAppObject.setBool(true);

        assertEquals(true, testAppObject.isBool());
    }

    @Test
    public void testSetString() {
        TestAppObject testAppObject = new TestAppObject();

        testAppObject.setString("test");

        assertEquals("test", testAppObject.getString());
    }

    @Test
    public void testGetString() {
        TestAppObject testAppObject = new TestAppObject();

        testAppObject.setString("test2");

        assertEquals("test2", testAppObject.getString());
    }

    @Test
    public void testSetTestComplexInputObject() {
        TestAppObject testAppObject = new TestAppObject();
        TestComplexAppObject testComplexAppObject = TestObjectUtil.generateTestComplexAppObject();

        testAppObject.setTestComplexAppObject(testComplexAppObject);

        assertEquals(testComplexAppObject, testAppObject.getTestComplexAppObject());
    }

    @Test
    public void testGetTestComplexInputObject() {
        TestAppObject testAppObject = new TestAppObject();
        TestComplexAppObject testComplexAppObject = TestObjectUtil.generateTestComplexAppObject();

        testAppObject.setTestComplexAppObject(testComplexAppObject);

        assertEquals(testComplexAppObject, testAppObject.getTestComplexAppObject());
    }

    @Test
    public void testHashCode() {
        TestAppObject testAppObject1 = new TestAppObject();
        TestAppObject testAppObject2 = new TestAppObject();
        TestAppObject testAppObject3 = TestObjectUtil.generateTestAppObject();
        TestAppObject testAppObject4 = TestObjectUtil.generateTestAppObject();

        int integer = 1000;
        String string = "test";
        boolean bool = false;
        TestComplexAppObject testComplexAppObject = TestObjectUtil.generateTestComplexAppObject();
        testAppObject1.setInteger(integer);
        testAppObject1.setBool(bool);
        testAppObject1.setString(string);
        testAppObject1.setTestComplexAppObject(testComplexAppObject);

        testAppObject2.setInteger(integer);
        testAppObject2.setBool(bool);
        testAppObject2.setString(string);
        testAppObject2.setTestComplexAppObject(testComplexAppObject);

        Set<TestAppObject> appObjects = new LinkedHashSet<>();

        appObjects.add(testAppObject1);
        assertTrue(appObjects.contains(testAppObject1));
        assertTrue(appObjects.size() == 1);

        appObjects.add(testAppObject2);
        assertTrue(appObjects.contains(testAppObject1));
        assertTrue(appObjects.contains(testAppObject2));
        assertTrue(appObjects.size() == 1);

        appObjects.add(testAppObject3);
        assertTrue(appObjects.contains(testAppObject1));
        assertTrue(appObjects.contains(testAppObject2));
        assertTrue(appObjects.contains(testAppObject3));
        assertTrue(appObjects.size() == 2);

        appObjects.add(testAppObject4);
        assertTrue(appObjects.contains(testAppObject1));
        assertTrue(appObjects.contains(testAppObject2));
        assertTrue(appObjects.contains(testAppObject3));
        assertTrue(appObjects.contains(testAppObject4));
        assertTrue(appObjects.size() == 3);
    }

    @Test
    public void testEquals() {
        TestAppObject testAppObject1 = new TestAppObject();
        TestAppObject testAppObject2 = new TestAppObject();
        TestAppObject testAppObject3 = TestObjectUtil.generateTestAppObject();
        TestAppObject testAppObject4 = TestObjectUtil.generateTestAppObject();

        int integer = 1000;
        String string = "test";
        boolean bool = false;
        TestComplexAppObject testComplexAppObject = TestObjectUtil.generateTestComplexAppObject();
        testAppObject1.setInteger(integer);
        testAppObject1.setBool(bool);
        testAppObject1.setString(string);
        testAppObject1.setTestComplexAppObject(testComplexAppObject);

        testAppObject2.setInteger(integer);
        testAppObject2.setBool(bool);
        testAppObject2.setString(string);
        testAppObject2.setTestComplexAppObject(testComplexAppObject);

        // exact same instance should be equal
        assertEquals(testAppObject1, testAppObject1);

        // null should not be equal
        assertNotEquals(testAppObject1, null);

        // different objects should not be equal
        assertNotEquals(testAppObject1, new Object());

        // different values of integer, bool, string and testComplexAppObject should
        // not be equal
        assertNotEquals(testAppObject1, testAppObject3);
        assertNotEquals(testAppObject1, testAppObject4);
        assertNotEquals(testAppObject3, testAppObject4);

        testAppObject2.setInteger(0);
        assertNotEquals(testAppObject1, testAppObject2);
        testAppObject2.setInteger(integer);

        testAppObject2.setBool(!bool);
        assertNotEquals(testAppObject1, testAppObject2);
        testAppObject2.setBool(bool);

        testAppObject2.setString("Test");
        assertNotEquals(testAppObject1, testAppObject2);
        testAppObject2.setString(string);

        testAppObject2.setTestComplexAppObject(TestObjectUtil.generateTestComplexAppObject());
        assertNotEquals(testAppObject1, testAppObject2);
        testAppObject2.setTestComplexAppObject(testComplexAppObject);

        // exact same values of integer, bool, string and testComplexAppObject should
        // be equal
        assertEquals(testAppObject1, testAppObject2);
    }

}
