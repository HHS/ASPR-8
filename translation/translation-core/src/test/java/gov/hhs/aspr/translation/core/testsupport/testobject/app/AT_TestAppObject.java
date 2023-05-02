package gov.hhs.aspr.translation.core.testsupport.testobject.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.translation.core.testsupport.TestObjectUtil;
import gov.hhs.aspr.translation.core.testsupport.testcomplexobject.app.TestComplexAppObject;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_TestAppObject {

    @Test
    @UnitTestConstructor(target = TestAppObject.class, args = {})
    public void testConstructor() {
        assertNotNull(new TestAppObject());
    }

    @Test
    @UnitTestMethod(target = TestAppObject.class, name = "setInteger", args = { int.class })
    public void testSetInteger() {
        TestAppObject testAppObject = new TestAppObject();

        testAppObject.setInteger(15);

        assertEquals(15, testAppObject.getInteger());
    }

    @Test
    @UnitTestMethod(target = TestAppObject.class, name = "getInteger", args = {})
    public void testGetInteger() {
        TestAppObject testAppObject = new TestAppObject();

        testAppObject.setInteger(150);

        assertEquals(150, testAppObject.getInteger());
    }

    @Test
    @UnitTestMethod(target = TestAppObject.class, name = "setBool", args = { boolean.class })
    public void testSetBool() {
        TestAppObject testAppObject = new TestAppObject();

        testAppObject.setBool(false);

        assertEquals(false, testAppObject.isBool());
    }

    @Test
    @UnitTestMethod(target = TestAppObject.class, name = "isBool", args = {})
    public void testIsBool() {
        TestAppObject testAppObject = new TestAppObject();

        testAppObject.setBool(true);

        assertEquals(true, testAppObject.isBool());
    }

    @Test
    @UnitTestMethod(target = TestAppObject.class, name = "setString", args = { String.class })
    public void testSetString() {
        TestAppObject testAppObject = new TestAppObject();

        testAppObject.setString("test");

        assertEquals("test", testAppObject.getString());
    }

    @Test
    @UnitTestMethod(target = TestAppObject.class, name = "getString", args = {})
    public void testGetString() {
        TestAppObject testAppObject = new TestAppObject();

        testAppObject.setString("test2");

        assertEquals("test2", testAppObject.getString());
    }

    @Test
    @UnitTestMethod(target = TestAppObject.class, name = "setTestComplexAppObject", args = {
            TestComplexAppObject.class })
    public void testSetTestComplexInputObject() {
        TestAppObject testAppObject = new TestAppObject();
        TestComplexAppObject testComplexAppObject = TestObjectUtil.generateTestComplexAppObject();

        testAppObject.setTestComplexAppObject(testComplexAppObject);

        assertEquals(testComplexAppObject, testAppObject.getTestComplexAppObject());
    }

    @Test
    @UnitTestMethod(target = TestAppObject.class, name = "getTestComplexAppObject", args = {})
    public void testGetTestComplexInputObject() {
        TestAppObject testAppObject = new TestAppObject();
        TestComplexAppObject testComplexAppObject = TestObjectUtil.generateTestComplexAppObject();

        testAppObject.setTestComplexAppObject(testComplexAppObject);

        assertEquals(testComplexAppObject, testAppObject.getTestComplexAppObject());
    }

    @Test
    @UnitTestMethod(target = TestAppObject.class, name = "hashCode", args = {})
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

        // exact same instance should be equal
        assertEquals(testAppObject1.hashCode(), testAppObject1.hashCode());

        // different objects should not be equal
        assertNotEquals(testAppObject1.hashCode(), new Object().hashCode());

        // different values of integer, bool, string and testComplexAppObject should
        // not be equal
        assertNotEquals(testAppObject1.hashCode(), testAppObject3.hashCode());
        assertNotEquals(testAppObject1.hashCode(), testAppObject4.hashCode());
        assertNotEquals(testAppObject3.hashCode(), testAppObject4.hashCode());

        testAppObject2.setInteger(0);
        assertNotEquals(testAppObject1.hashCode(), testAppObject2.hashCode());
        testAppObject2.setInteger(integer);

        testAppObject2.setBool(!bool);
        assertNotEquals(testAppObject1.hashCode(), testAppObject2.hashCode());
        testAppObject2.setBool(bool);

        testAppObject2.setString("Test");
        assertNotEquals(testAppObject1.hashCode(), testAppObject2.hashCode());
        testAppObject2.setString(string);

        testAppObject2.setTestComplexAppObject(TestObjectUtil.generateTestComplexAppObject());
        assertNotEquals(testAppObject1.hashCode(), testAppObject2.hashCode());
        testAppObject2.setTestComplexAppObject(testComplexAppObject);

        // exact same values of integer, bool, string and testComplexAppObject should
        // be equal
        assertEquals(testAppObject1.hashCode(), testAppObject2.hashCode());
    }

    @Test
    @UnitTestMethod(target = TestAppObject.class, name = "equals", args = { Object.class })
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
