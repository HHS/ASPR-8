package gov.hhs.aspr.ms.taskit.core.testsupport.testcomplexobject.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.taskit.core.testsupport.TestObjectUtil;
import gov.hhs.aspr.ms.taskit.core.testsupport.testcomplexobject.TestComplexInputObject;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_TestComplexInputObject {

    @Test
    @UnitTestConstructor(target = TestComplexInputObject.class, args = {})
    public void testConstructor() {
        assertNotNull(new TestComplexInputObject());
    }

    @Test
    @UnitTestMethod(target = TestComplexInputObject.class, name = "setNumEntities", args = { int.class })
    public void testSetNumEntities() {
        TestComplexInputObject testComplexInputObject = new TestComplexInputObject();

        testComplexInputObject.setNumEntities(15);

        assertEquals(15, testComplexInputObject.getNumEntities());
    }

    @Test
    @UnitTestMethod(target = TestComplexInputObject.class, name = "getNumEntities", args = {})
    public void testGetNumEntities() {
        TestComplexInputObject testComplexInputObject = new TestComplexInputObject();

        testComplexInputObject.setNumEntities(150);

        assertEquals(150, testComplexInputObject.getNumEntities());
    }

    @Test
    @UnitTestMethod(target = TestComplexInputObject.class, name = "setStartTime", args = { double.class })
    public void testSetStartTime() {
        TestComplexInputObject testComplexInputObject = new TestComplexInputObject();

        testComplexInputObject.setStartTime(0.0);

        assertEquals(0.0, testComplexInputObject.getStartTime());
    }

    @Test
    @UnitTestMethod(target = TestComplexInputObject.class, name = "getStartTime", args = {})
    public void testIsStartTime() {
        TestComplexInputObject testComplexInputObject = new TestComplexInputObject();

        testComplexInputObject.setStartTime(150.0);

        assertEquals(150.0, testComplexInputObject.getStartTime());
    }

    @Test
    @UnitTestMethod(target = TestComplexInputObject.class, name = "setTestString", args = { String.class })
    public void testSetTestString() {
        TestComplexInputObject testComplexInputObject = new TestComplexInputObject();

        testComplexInputObject.setTestString("test");

        assertEquals("test", testComplexInputObject.getTestString());
    }

    @Test
    @UnitTestMethod(target = TestComplexInputObject.class, name = "getTestString", args = {})
    public void testGetString() {
        TestComplexInputObject testComplexInputObject = new TestComplexInputObject();

        testComplexInputObject.setTestString("test2");

        assertEquals("test2", testComplexInputObject.getTestString());
    }

    @Test
    @UnitTestMethod(target = TestComplexInputObject.class, name = "hashCode", args = {})
    public void testHashCode() {
        TestComplexInputObject testComplexInputObject1 = new TestComplexInputObject();
        TestComplexInputObject testComplexInputObject2 = new TestComplexInputObject();
        TestComplexInputObject testComplexInputObject3 = TestObjectUtil.generateTestComplexInputObject();
        TestComplexInputObject testComplexInputObject4 = TestObjectUtil.generateTestComplexInputObject();

        int numEntities = 1000;
        String testString = "test";
        double startTime = 0.156789;

        testComplexInputObject1.setNumEntities(numEntities);
        testComplexInputObject1.setStartTime(startTime);
        testComplexInputObject1.setTestString(testString);

        testComplexInputObject2.setNumEntities(numEntities);
        testComplexInputObject2.setStartTime(startTime);
        testComplexInputObject2.setTestString(testString);

        // exact same instance should be equal
        assertEquals(testComplexInputObject1.hashCode(), testComplexInputObject1.hashCode());

        // different objects should not be equal
        assertNotEquals(testComplexInputObject1.hashCode(), new Object().hashCode());

        // different values of integer, bool, string and testComplexInputObject should
        // not be equal
        assertNotEquals(testComplexInputObject1.hashCode(), testComplexInputObject3.hashCode());
        assertNotEquals(testComplexInputObject1.hashCode(), testComplexInputObject4.hashCode());
        assertNotEquals(testComplexInputObject3.hashCode(), testComplexInputObject4.hashCode());

        testComplexInputObject2.setNumEntities(0);
        assertNotEquals(testComplexInputObject1.hashCode(), testComplexInputObject2.hashCode());
        testComplexInputObject2.setNumEntities(numEntities);

        testComplexInputObject2.setStartTime(150.0156);
        assertNotEquals(testComplexInputObject1.hashCode(), testComplexInputObject2.hashCode());
        testComplexInputObject2.setStartTime(startTime);

        testComplexInputObject2.setTestString("Test");
        assertNotEquals(testComplexInputObject1.hashCode(), testComplexInputObject2.hashCode());
        testComplexInputObject2.setTestString(testString);

        // exact same values of integer, bool, string and testComplexInputObject should
        // be equal
        assertEquals(testComplexInputObject1.hashCode(), testComplexInputObject2.hashCode());
    }

    @Test
    @UnitTestMethod(target = TestComplexInputObject.class, name = "equals", args = { Object.class })
    public void testEquals() {
        TestComplexInputObject testComplexInputObject1 = new TestComplexInputObject();
        TestComplexInputObject testComplexInputObject2 = new TestComplexInputObject();
        TestComplexInputObject testComplexInputObject3 = TestObjectUtil.generateTestComplexInputObject();
        TestComplexInputObject testComplexInputObject4 = TestObjectUtil.generateTestComplexInputObject();

        int numEntities = 1000;
        String testString = "test";
        double startTime = 0.156789;

        testComplexInputObject1.setNumEntities(numEntities);
        testComplexInputObject1.setStartTime(startTime);
        testComplexInputObject1.setTestString(testString);

        testComplexInputObject2.setNumEntities(numEntities);
        testComplexInputObject2.setStartTime(startTime);
        testComplexInputObject2.setTestString(testString);

        // exact same instance should be equal
        assertEquals(testComplexInputObject1, testComplexInputObject1);

        // null should not be equal
        assertNotEquals(testComplexInputObject1, null);

        // different objects should not be equal
        assertNotEquals(testComplexInputObject1, new Object());

        // different values of integer, bool, string and testComplexInputObject should
        // not be equal
        assertNotEquals(testComplexInputObject1, testComplexInputObject3);
        assertNotEquals(testComplexInputObject1, testComplexInputObject4);
        assertNotEquals(testComplexInputObject3, testComplexInputObject4);

        testComplexInputObject2.setNumEntities(0);
        assertNotEquals(testComplexInputObject1, testComplexInputObject2);
        testComplexInputObject2.setNumEntities(numEntities);

        testComplexInputObject2.setStartTime(150.0156);
        assertNotEquals(testComplexInputObject1, testComplexInputObject2);
        testComplexInputObject2.setStartTime(startTime);

        testComplexInputObject2.setTestString("Test");
        assertNotEquals(testComplexInputObject1, testComplexInputObject2);
        testComplexInputObject2.setTestString(testString);

        // exact same values of integer, bool, string and testComplexInputObject should
        // be equal
        assertEquals(testComplexInputObject1, testComplexInputObject2);
    }
}
