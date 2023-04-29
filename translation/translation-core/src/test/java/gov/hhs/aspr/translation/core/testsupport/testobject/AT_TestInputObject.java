package gov.hhs.aspr.translation.core.testsupport.testobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.translation.core.testsupport.TestObjectUtil;
import gov.hhs.aspr.translation.core.testsupport.testcomplexobject.TestComplexAppObject;
import gov.hhs.aspr.translation.core.testsupport.testcomplexobject.TestComplexInputObject;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_TestInputObject {

    @Test
    @UnitTestConstructor(target = TestInputObject.class, args = {})
    public void testConstructor() {
        assertNotNull(new TestInputObject());
    }

    @Test
    @UnitTestMethod(target = TestInputObject.class, name = "setInteger", args = { int.class })
    public void testSetInteger() {
        TestInputObject testInputObject = new TestInputObject();

        testInputObject.setInteger(15);

        assertEquals(15, testInputObject.getInteger());
    }

    @Test
    @UnitTestMethod(target = TestInputObject.class, name = "getInteger", args = {})
    public void testGetInteger() {
        TestInputObject testInputObject = new TestInputObject();

        testInputObject.setInteger(150);

        assertEquals(150, testInputObject.getInteger());
    }

    @Test
    @UnitTestMethod(target = TestInputObject.class, name = "setBool", args = { boolean.class })
    public void testSetBool() {
        TestInputObject testInputObject = new TestInputObject();

        testInputObject.setBool(false);

        assertEquals(false, testInputObject.isBool());
    }

    @Test
    @UnitTestMethod(target = TestInputObject.class, name = "isBool", args = {})
    public void testIsBool() {
        TestInputObject testInputObject = new TestInputObject();

        testInputObject.setBool(true);

        assertEquals(true, testInputObject.isBool());
    }

    @Test
    @UnitTestMethod(target = TestInputObject.class, name = "setString", args = { String.class })
    public void testSetString() {
        TestInputObject testInputObject = new TestInputObject();

        testInputObject.setString("test");

        assertEquals("test", testInputObject.getString());
    }

    @Test
    @UnitTestMethod(target = TestInputObject.class, name = "getString", args = {})
    public void testGetString() {
        TestInputObject testInputObject = new TestInputObject();

        testInputObject.setString("test2");

        assertEquals("test2", testInputObject.getString());
    }

    @Test
    @UnitTestMethod(target = TestInputObject.class, name = "setTestComplexAppObject", args = {
            TestComplexAppObject.class })
    public void testSetTestComplexInputObject() {
        TestInputObject testInputObject = new TestInputObject();
        TestComplexInputObject testComplexInputObject = TestObjectUtil.generateTestComplexInputObject();

        testInputObject.setTestComplexInputObject(testComplexInputObject);

        assertEquals(testComplexInputObject, testInputObject.getTestComplexInputObject());
    }

    @Test
    @UnitTestMethod(target = TestInputObject.class, name = "getTestComplexAppObject", args = {})
    public void testGetTestComplexInputObject() {
        TestInputObject testInputObject = new TestInputObject();
        TestComplexInputObject testComplexInputObject = TestObjectUtil.generateTestComplexInputObject();

        testInputObject.setTestComplexInputObject(testComplexInputObject);

        assertEquals(testComplexInputObject, testInputObject.getTestComplexInputObject());
    }

    @Test
    @UnitTestMethod(target = TestInputObject.class, name = "hashCode", args = {})
    public void testHashCode() {
        TestInputObject testInputObject1 = new TestInputObject();
        TestInputObject testInputObject2 = new TestInputObject();
        TestInputObject testInputObject3 = TestObjectUtil.generateTestInputObject();
        TestInputObject testInputObject4 = TestObjectUtil.generateTestInputObject();

        int integer = 1000;
        String string = "test";
        boolean bool = false;
        TestComplexInputObject testComplexInputObject = TestObjectUtil.generateTestComplexInputObject();
        testInputObject1.setInteger(integer);
        testInputObject1.setBool(bool);
        testInputObject1.setString(string);
        testInputObject1.setTestComplexInputObject(testComplexInputObject);

        testInputObject2.setInteger(integer);
        testInputObject2.setBool(bool);
        testInputObject2.setString(string);
        testInputObject2.setTestComplexInputObject(testComplexInputObject);
        // TODO: fix this

        Set<TestInputObject> inputObjects = new LinkedHashSet<>();

        inputObjects.add(testInputObject1);
        assertTrue(inputObjects.contains(testInputObject1));
        assertTrue(inputObjects.size() == 1);

        inputObjects.add(testInputObject2);
        assertTrue(inputObjects.contains(testInputObject1));
        assertTrue(inputObjects.contains(testInputObject2));
        assertTrue(inputObjects.size() == 1);

        inputObjects.add(testInputObject3);
        assertTrue(inputObjects.contains(testInputObject1));
        assertTrue(inputObjects.contains(testInputObject2));
        assertTrue(inputObjects.contains(testInputObject3));
        assertTrue(inputObjects.size() == 2);

        inputObjects.add(testInputObject4);
        assertTrue(inputObjects.contains(testInputObject1));
        assertTrue(inputObjects.contains(testInputObject2));
        assertTrue(inputObjects.contains(testInputObject3));
        assertTrue(inputObjects.contains(testInputObject4));
        assertTrue(inputObjects.size() == 3);
    }

    @Test
    @UnitTestMethod(target = TestInputObject.class, name = "equals", args = { Object.class })
    public void testEquals() {
        TestInputObject testInputObject1 = new TestInputObject();
        TestInputObject testInputObject2 = new TestInputObject();
        TestInputObject testInputObject3 = TestObjectUtil.generateTestInputObject();
        TestInputObject testInputObject4 = TestObjectUtil.generateTestInputObject();

        int integer = 1000;
        String string = "test";
        boolean bool = false;
        TestComplexInputObject testComplexInputObject = TestObjectUtil.generateTestComplexInputObject();
        testInputObject1.setInteger(integer);
        testInputObject1.setBool(bool);
        testInputObject1.setString(string);
        testInputObject1.setTestComplexInputObject(testComplexInputObject);

        testInputObject2.setInteger(integer);
        testInputObject2.setBool(bool);
        testInputObject2.setString(string);
        testInputObject2.setTestComplexInputObject(testComplexInputObject);

        // exact same instance should be equal
        assertEquals(testInputObject1, testInputObject1);

        // null should not be equal
        assertNotEquals(testInputObject1, null);

        // different objects should not be equal
        assertNotEquals(testInputObject1, new Object());

        // different values of integer, bool, string and testComplexInputObject should
        // not be equal
        assertNotEquals(testInputObject1, testInputObject3);
        assertNotEquals(testInputObject1, testInputObject4);
        assertNotEquals(testInputObject3, testInputObject4);

        testInputObject2.setInteger(0);
        assertNotEquals(testInputObject1, testInputObject2);
        testInputObject2.setInteger(integer);

        testInputObject2.setBool(!bool);
        assertNotEquals(testInputObject1, testInputObject2);
        testInputObject2.setBool(bool);

        testInputObject2.setString("Test");
        assertNotEquals(testInputObject1, testInputObject2);
        testInputObject2.setString(string);

        testInputObject2.setTestComplexInputObject(TestObjectUtil.generateTestComplexInputObject());
        assertNotEquals(testInputObject1, testInputObject2);
        testInputObject2.setTestComplexInputObject(testComplexInputObject);

        // exact same values of integer, bool, string and testComplexInputObject should
        // be equal
        assertEquals(testInputObject1, testInputObject2);
    }

}
