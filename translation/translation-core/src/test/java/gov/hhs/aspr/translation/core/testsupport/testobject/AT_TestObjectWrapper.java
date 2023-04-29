package gov.hhs.aspr.translation.core.testsupport.testobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.translation.core.support.TestObjectUtil;

public class AT_TestObjectWrapper {

    @Test
    public void testConstructor() {
        assertNotNull(new TestObjectWrapper());
    }

    @Test
    public void testSetWrappedObject() {
        TestAppObject testAppObject = TestObjectUtil.generateTestAppObject();
        TestObjectWrapper testObjectWrapper = new TestObjectWrapper();

        testObjectWrapper.setWrappedObject(testAppObject);

        assertEquals(testAppObject, testObjectWrapper.getWrappedObject());

        // preconditions
        // cannot set the wrapped object to the TestObjectWrapper instance
        assertThrows(RuntimeException.class, () -> {
            testObjectWrapper.setWrappedObject(testObjectWrapper);
        });

        // cannot set the wrapped object to another instance of TestObjectWrapper
        assertThrows(RuntimeException.class, () -> {
            testObjectWrapper.setWrappedObject(new TestObjectWrapper());
        });
    }

    @Test
    public void testGetWrappedObject() {
        TestInputObject testInputObject = TestObjectUtil.generateTestInputObject();
        TestObjectWrapper testObjectWrapper = new TestObjectWrapper();

        testObjectWrapper.setWrappedObject(testInputObject);

        assertEquals(testInputObject, testObjectWrapper.getWrappedObject());
    }

    @Test
    public void testHashCode() {
        TestAppObject testAppObject = TestObjectUtil.generateTestAppObject();
        TestObjectWrapper testObjectWrapper = new TestObjectWrapper();
        testObjectWrapper.setWrappedObject(testAppObject);

        TestObjectWrapper testObjectWrapper2 = new TestObjectWrapper();
        testObjectWrapper2.setWrappedObject(testAppObject);

        TestInputObject testInputObject = TestObjectUtil.generateTestInputObject();
        TestObjectWrapper testObjectWrapper3 = new TestObjectWrapper();

        testObjectWrapper3.setWrappedObject(testInputObject);

        Set<TestObjectWrapper> wrapperSet = new LinkedHashSet<>();

        wrapperSet.add(testObjectWrapper);

        assertTrue(wrapperSet.contains(testObjectWrapper));
        assertTrue(wrapperSet.size() == 1);

        wrapperSet.add(testObjectWrapper2);
        assertTrue(wrapperSet.contains(testObjectWrapper2));
        assertTrue(wrapperSet.contains(testObjectWrapper));
        assertTrue(wrapperSet.size() == 1);

        wrapperSet.add(testObjectWrapper3);
        assertTrue(wrapperSet.contains(testObjectWrapper3));
        assertTrue(wrapperSet.contains(testObjectWrapper2));
        assertTrue(wrapperSet.contains(testObjectWrapper));
        assertTrue(wrapperSet.size() == 2);
    }

    @Test
    public void testEquals() {
        TestAppObject testAppObject = TestObjectUtil.generateTestAppObject();
        TestObjectWrapper testObjectWrapper = new TestObjectWrapper();
        testObjectWrapper.setWrappedObject(testAppObject);

        TestObjectWrapper testObjectWrapper2 = new TestObjectWrapper();
        testObjectWrapper2.setWrappedObject(testAppObject);

        TestInputObject testInputObject = TestObjectUtil.generateTestInputObject();
        TestObjectWrapper testObjectWrapper3 = new TestObjectWrapper();

        testObjectWrapper3.setWrappedObject(testInputObject);

        // exact same instance is equal
        assertEquals(testObjectWrapper, testObjectWrapper);

        // null should not be equal
        assertNotEquals(testObjectWrapper, null);

        // different objects should not be equal
        assertNotEquals(testObjectWrapper, new Object());

        // different wrapped objects should not be equal
        assertNotEquals(testObjectWrapper, testObjectWrapper3);

        // same wrapped objects should be equal
        assertEquals(testObjectWrapper, testObjectWrapper2);
    }

}
