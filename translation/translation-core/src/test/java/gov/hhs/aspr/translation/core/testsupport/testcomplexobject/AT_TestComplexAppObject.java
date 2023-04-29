package gov.hhs.aspr.translation.core.testsupport.testcomplexobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.translation.core.testsupport.TestObjectUtil;

public class AT_TestComplexAppObject {

    @Test
    public void testSetNumEntities() {
        TestComplexAppObject testComplexAppObject = new TestComplexAppObject();

        testComplexAppObject.setNumEntities(15);

        assertEquals(15, testComplexAppObject.getNumEntities());
    }

    @Test
    public void testGetNumEntities() {
        TestComplexAppObject testComplexAppObject = new TestComplexAppObject();

        testComplexAppObject.setNumEntities(150);

        assertEquals(150, testComplexAppObject.getNumEntities());
    }

    @Test
    public void testSetStartTime() {
        TestComplexAppObject testComplexAppObject = new TestComplexAppObject();

        testComplexAppObject.setStartTime(0.0);

        assertEquals(0.0, testComplexAppObject.getStartTime());
    }

    @Test
    public void testIsStartTime() {
        TestComplexAppObject testComplexAppObject = new TestComplexAppObject();

        testComplexAppObject.setStartTime(150.0);

        assertEquals(150.0, testComplexAppObject.getStartTime());
    }

    @Test
    public void testSetTestString() {
        TestComplexAppObject testComplexAppObject = new TestComplexAppObject();

        testComplexAppObject.setTestString("test");

        assertEquals("test", testComplexAppObject.getTestString());
    }

    @Test
    public void testGetString() {
        TestComplexAppObject testComplexAppObject = new TestComplexAppObject();

        testComplexAppObject.setTestString("test2");

        assertEquals("test2", testComplexAppObject.getTestString());
    }

    @Test
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

        Set<TestComplexAppObject> appObjects = new LinkedHashSet<>();

        appObjects.add(testComplexAppObject1);
        assertTrue(appObjects.contains(testComplexAppObject1));
        assertTrue(appObjects.size() == 1);

        appObjects.add(testComplexAppObject2);
        assertTrue(appObjects.contains(testComplexAppObject1));
        assertTrue(appObjects.contains(testComplexAppObject2));
        assertTrue(appObjects.size() == 1);

        appObjects.add(testComplexAppObject3);
        assertTrue(appObjects.contains(testComplexAppObject1));
        assertTrue(appObjects.contains(testComplexAppObject2));
        assertTrue(appObjects.contains(testComplexAppObject3));
        assertTrue(appObjects.size() == 2);

        appObjects.add(testComplexAppObject4);
        assertTrue(appObjects.contains(testComplexAppObject1));
        assertTrue(appObjects.contains(testComplexAppObject2));
        assertTrue(appObjects.contains(testComplexAppObject3));
        assertTrue(appObjects.contains(testComplexAppObject4));
        assertTrue(appObjects.size() == 3);
    }

    @Test
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
