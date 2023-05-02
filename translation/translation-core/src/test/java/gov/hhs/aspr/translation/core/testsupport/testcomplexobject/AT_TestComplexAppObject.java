package gov.hhs.aspr.translation.core.testsupport.testcomplexobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.translation.core.testsupport.TestObjectUtil;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_TestComplexAppObject {

    @Test
    @UnitTestConstructor(target = TestComplexAppObject.class, args = {})
    public void testConstructor() {
        assertNotNull(new TestComplexAppObject());
    }

    @Test
    @UnitTestMethod(target = TestComplexAppObject.class, name = "setNumEntities", args = { int.class })
    public void testSetNumEntities() {
        TestComplexAppObject testComplexAppObject = new TestComplexAppObject();

        testComplexAppObject.setNumEntities(15);

        assertEquals(15, testComplexAppObject.getNumEntities());
    }

    @Test
    @UnitTestMethod(target = TestComplexAppObject.class, name = "getNumEntities", args = {})
    public void testGetNumEntities() {
        TestComplexAppObject testComplexAppObject = new TestComplexAppObject();

        testComplexAppObject.setNumEntities(150);

        assertEquals(150, testComplexAppObject.getNumEntities());
    }

    @Test
    @UnitTestMethod(target = TestComplexAppObject.class, name = "setStartTime", args = { double.class })
    public void testSetStartTime() {
        TestComplexAppObject testComplexAppObject = new TestComplexAppObject();

        testComplexAppObject.setStartTime(0.0);

        assertEquals(0.0, testComplexAppObject.getStartTime());
    }

    @Test
    @UnitTestMethod(target = TestComplexAppObject.class, name = "getStartTime", args = {})
    public void testGetStartTime() {
        TestComplexAppObject testComplexAppObject = new TestComplexAppObject();

        testComplexAppObject.setStartTime(150.0);

        assertEquals(150.0, testComplexAppObject.getStartTime());
    }

    @Test
    @UnitTestMethod(target = TestComplexAppObject.class, name = "setTestString", args = { String.class })
    public void testSetTestString() {
        TestComplexAppObject testComplexAppObject = new TestComplexAppObject();

        testComplexAppObject.setTestString("test");

        assertEquals("test", testComplexAppObject.getTestString());
    }

    @Test
    @UnitTestMethod(target = TestComplexAppObject.class, name = "getTestString", args = {})
    public void testGetTestString() {
        TestComplexAppObject testComplexAppObject = new TestComplexAppObject();

        testComplexAppObject.setTestString("test2");

        assertEquals("test2", testComplexAppObject.getTestString());
    }

    @Test
    @UnitTestMethod(target = TestComplexAppObject.class, name = "hashCode", args = {})
    public void testHashCode() {
        TestComplexAppObject testComplexAppObject1 = new TestComplexAppObject();
        TestComplexAppObject testComplexAppObject2 = new TestComplexAppObject();
        TestComplexAppObject testComplexAppObject3 = TestObjectUtil.generateTestComplexAppObject();
        TestComplexAppObject testComplexAppObject4 = TestObjectUtil.generateTestComplexAppObject();

        int numEntities = 1000;
        String testString = "test";
        double startTime = 0.156789;

        testComplexAppObject1.setNumEntities(numEntities);
        testComplexAppObject1.setStartTime(startTime);
        testComplexAppObject1.setTestString(testString);

        testComplexAppObject2.setNumEntities(numEntities);
        testComplexAppObject2.setStartTime(startTime);
        testComplexAppObject2.setTestString(testString);

        // exact same instance should be equal
        assertEquals(testComplexAppObject1.hashCode(), testComplexAppObject1.hashCode());

        // different objects should not be equal
        assertNotEquals(testComplexAppObject1.hashCode(), new Object().hashCode());

        // different values of integer, bool, string and testComplexInputObject should
        // not be equal
        assertNotEquals(testComplexAppObject1.hashCode(), testComplexAppObject3.hashCode());
        assertNotEquals(testComplexAppObject1.hashCode(), testComplexAppObject4.hashCode());
        assertNotEquals(testComplexAppObject3.hashCode(), testComplexAppObject4.hashCode());

        testComplexAppObject2.setNumEntities(0);
        assertNotEquals(testComplexAppObject1.hashCode(), testComplexAppObject2.hashCode());
        testComplexAppObject2.setNumEntities(numEntities);

        testComplexAppObject2.setStartTime(150.0156);
        assertNotEquals(testComplexAppObject1.hashCode(), testComplexAppObject2.hashCode());
        testComplexAppObject2.setStartTime(startTime);

        testComplexAppObject2.setTestString("Test");
        assertNotEquals(testComplexAppObject1.hashCode(), testComplexAppObject2.hashCode());
        testComplexAppObject2.setTestString(testString);

        // exact same values of integer, bool, string and testComplexInputObject should
        // be equal
        assertEquals(testComplexAppObject1.hashCode(), testComplexAppObject2.hashCode());
    }

    @Test
    @UnitTestMethod(target = TestComplexAppObject.class, name = "equals", args = { Object.class })
    public void testEquals() {
        TestComplexAppObject testComplexAppObject1 = new TestComplexAppObject();
        TestComplexAppObject testComplexAppObject2 = new TestComplexAppObject();
        TestComplexAppObject testComplexAppObject3 = TestObjectUtil.generateTestComplexAppObject();
        TestComplexAppObject testComplexAppObject4 = TestObjectUtil.generateTestComplexAppObject();

        int numEntities = 1000;
        String testString = "test";
        double startTime = 0.156789;

        testComplexAppObject1.setNumEntities(numEntities);
        testComplexAppObject1.setStartTime(startTime);
        testComplexAppObject1.setTestString(testString);

        testComplexAppObject2.setNumEntities(numEntities);
        testComplexAppObject2.setStartTime(startTime);
        testComplexAppObject2.setTestString(testString);

        // exact same instance should be equal
        assertEquals(testComplexAppObject1, testComplexAppObject1);

        // null should not be equal
        assertNotEquals(testComplexAppObject1, null);

        // different objects should not be equal
        assertNotEquals(testComplexAppObject1, new Object());

        // different values of integer, bool, string and testComplexAppObject should
        // not be equal
        assertNotEquals(testComplexAppObject1, testComplexAppObject3);
        assertNotEquals(testComplexAppObject1, testComplexAppObject4);
        assertNotEquals(testComplexAppObject3, testComplexAppObject4);

        testComplexAppObject2.setNumEntities(0);
        assertNotEquals(testComplexAppObject1, testComplexAppObject2);
        testComplexAppObject2.setNumEntities(numEntities);

        testComplexAppObject2.setStartTime(150.0156);
        assertNotEquals(testComplexAppObject1, testComplexAppObject2);
        testComplexAppObject2.setStartTime(startTime);

        testComplexAppObject2.setTestString("Test");
        assertNotEquals(testComplexAppObject1, testComplexAppObject2);
        testComplexAppObject2.setTestString(testString);

        // exact same values of integer, bool, string and testComplexAppObject should
        // be equal
        assertEquals(testComplexAppObject1, testComplexAppObject2);
    }

}
