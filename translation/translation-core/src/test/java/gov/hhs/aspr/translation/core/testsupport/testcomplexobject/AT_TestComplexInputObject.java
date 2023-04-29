package gov.hhs.aspr.translation.core.testsupport.testcomplexobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.translation.core.support.TestObjectUtil;

public class AT_TestComplexInputObject {
    
    @Test
    public void testSetNumEntities() {
        TestComplexInputObject testComplexInputObject = new TestComplexInputObject();

        testComplexInputObject.setNumEntities(15);

        assertEquals(15, testComplexInputObject.getNumEntities());
    }

    @Test
    public void testGetNumEntities() {
        TestComplexInputObject testComplexInputObject = new TestComplexInputObject();

        testComplexInputObject.setNumEntities(150);

        assertEquals(150, testComplexInputObject.getNumEntities());
    }

    @Test
    public void testSetStartTime() {
        TestComplexInputObject testComplexInputObject = new TestComplexInputObject();

        testComplexInputObject.setStartTime(0.0);

        assertEquals(0.0, testComplexInputObject.getStartTime());
    }

    @Test
    public void testIsStartTime() {
        TestComplexInputObject testComplexInputObject = new TestComplexInputObject();

        testComplexInputObject.setStartTime(150.0);

        assertEquals(150.0, testComplexInputObject.getStartTime());
    }

    @Test
    public void testSetTestString() {
        TestComplexInputObject testComplexInputObject = new TestComplexInputObject();

        testComplexInputObject.setTestString("test");

        assertEquals("test", testComplexInputObject.getTestString());
    }

    @Test
    public void testGetString() {
        TestComplexInputObject testComplexInputObject = new TestComplexInputObject();

        testComplexInputObject.setTestString("test2");

        assertEquals("test2", testComplexInputObject.getTestString());
    }

    @Test
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

        Set<TestComplexInputObject> appObjects = new LinkedHashSet<>();

        appObjects.add(testComplexInputObject1);
        assertTrue(appObjects.contains(testComplexInputObject1));
        assertTrue(appObjects.size() == 1);

        appObjects.add(testComplexInputObject2);
        assertTrue(appObjects.contains(testComplexInputObject1));
        assertTrue(appObjects.contains(testComplexInputObject2));
        assertTrue(appObjects.size() == 1);

        appObjects.add(testComplexInputObject3);
        assertTrue(appObjects.contains(testComplexInputObject1));
        assertTrue(appObjects.contains(testComplexInputObject2));
        assertTrue(appObjects.contains(testComplexInputObject3));
        assertTrue(appObjects.size() == 2);

        appObjects.add(testComplexInputObject4);
        assertTrue(appObjects.contains(testComplexInputObject1));
        assertTrue(appObjects.contains(testComplexInputObject2));
        assertTrue(appObjects.contains(testComplexInputObject3));
        assertTrue(appObjects.contains(testComplexInputObject4));
        assertTrue(appObjects.size() == 3);
    }

    @Test
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
