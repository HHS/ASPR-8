package gov.hhs.aspr.translation.core.testsupport.testobject.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.translation.core.testsupport.TestObjectUtil;
import gov.hhs.aspr.translation.core.testsupport.testobject.input.TestInputObject;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_TestObjectWrapper {

    @Test
    @UnitTestConstructor(target = TestObjectWrapper.class, args = {})
    public void testConstructor() {
        assertNotNull(new TestObjectWrapper());
    }

    @Test
    @UnitTestMethod(target = TestObjectWrapper.class, name = "setWrappedObject", args = { Object.class })
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
    @UnitTestMethod(target = TestObjectWrapper.class, name = "getWrappedObject", args = {})
    public void testGetWrappedObject() {
        TestInputObject testInputObject = TestObjectUtil.generateTestInputObject();
        TestObjectWrapper testObjectWrapper = new TestObjectWrapper();

        testObjectWrapper.setWrappedObject(testInputObject);

        assertEquals(testInputObject, testObjectWrapper.getWrappedObject());
    }

    @Test
    @UnitTestMethod(target = TestObjectWrapper.class, name = "hashCode", args = {})
    public void testHashCode() {
        TestAppObject testAppObject = TestObjectUtil.generateTestAppObject();
        TestObjectWrapper testObjectWrapper = new TestObjectWrapper();
        testObjectWrapper.setWrappedObject(testAppObject);

        TestObjectWrapper testObjectWrapper2 = new TestObjectWrapper();
        testObjectWrapper2.setWrappedObject(testAppObject);

        TestInputObject testInputObject = TestObjectUtil.generateTestInputObject();
        TestObjectWrapper testObjectWrapper3 = new TestObjectWrapper();

        testObjectWrapper3.setWrappedObject(testInputObject);

        // exact same instance is equal
        assertEquals(testObjectWrapper.hashCode(), testObjectWrapper.hashCode());

        // different objects should not be equal
        assertNotEquals(testObjectWrapper.hashCode(), new Object().hashCode());

        // different wrapped objects should not be equal
        assertNotEquals(testObjectWrapper.hashCode(), testObjectWrapper3.hashCode());

        // same wrapped objects should be equal
        assertEquals(testObjectWrapper.hashCode(), testObjectWrapper2.hashCode());
    }

    @Test
    @UnitTestMethod(target = TestObjectWrapper.class, name = "equals", args = { Object.class })
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
