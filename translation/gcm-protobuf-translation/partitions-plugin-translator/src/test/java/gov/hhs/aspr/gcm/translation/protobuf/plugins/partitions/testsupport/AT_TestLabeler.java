package gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.LinkedHashSet;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import plugins.partitions.support.PartitionsContext;
import plugins.people.support.PersonId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_TestLabeler {

    @Test
    @UnitTestConstructor(target = TestLabeler.class, args = { String.class })
    public void testConstructor() {
        assertNotNull(new TestLabeler("test"));
    }

    @Test
    @UnitTestMethod(target = TestLabeler.class, name = "getId", args = {})
    public void testGetId() {
        TestLabeler testLabeler = new TestLabeler("test");

        assertEquals("test", testLabeler.getId());
    }

    @Test
    @UnitTestMethod(target = TestLabeler.class, name = "getCurrentLabel", args = { PartitionsContext.class,
            PersonId.class })
    public void testGetCurrentLabel() {
        TestLabeler testLabeler = new TestLabeler("test");

        assertEquals("Current Label", testLabeler.getCurrentLabel(null, null));
    }

    @Test
    @UnitTestMethod(target = TestLabeler.class, name = "getPastLabel", args = { PartitionsContext.class, Event.class })
    public void testGetPastLabel() {
        TestLabeler testLabeler = new TestLabeler("test");

        assertEquals("Past Label", testLabeler.getPastLabel(null, null));
    }

    @Test
    @UnitTestMethod(target = TestLabeler.class, name = "getLabelerSensitivities", args = {})
    public void testGetLabelerSensitivities() {
        TestLabeler testLabeler = new TestLabeler("test");

        assertEquals(new LinkedHashSet<>(), testLabeler.getLabelerSensitivities());
    }

    @Test
    @UnitTestMethod(target = TestLabeler.class, name = "hashCode", args = {})
    public void testHashCode() {
        TestLabeler testLabeler1 = new TestLabeler("test1");
        TestLabeler testLabeler2 = new TestLabeler("test2");
        TestLabeler testLabeler3 = new TestLabeler("test3");
        TestLabeler testLabeler4 = new TestLabeler("test4");
        TestLabeler testLabeler5 = new TestLabeler("test1");

        assertEquals(testLabeler1.hashCode(), testLabeler1.hashCode());

        assertNotEquals(testLabeler1.hashCode(), testLabeler2.hashCode());
        assertNotEquals(testLabeler1.hashCode(), testLabeler3.hashCode());
        assertNotEquals(testLabeler1.hashCode(), testLabeler4.hashCode());
        assertNotEquals(testLabeler2.hashCode(), testLabeler3.hashCode());
        assertNotEquals(testLabeler2.hashCode(), testLabeler4.hashCode());
        assertNotEquals(testLabeler3.hashCode(), testLabeler5.hashCode());

        assertEquals(testLabeler1.hashCode(), testLabeler5.hashCode());
    }

    @Test
    @UnitTestMethod(target = TestLabeler.class, name = "equals", args = { Object.class })
    public void testEquals() {
        TestLabeler testLabeler1 = new TestLabeler("test1");
        TestLabeler testLabeler2 = new TestLabeler("test2");
        TestLabeler testLabeler3 = new TestLabeler("test3");
        TestLabeler testLabeler4 = new TestLabeler("test4");
        TestLabeler testLabeler5 = new TestLabeler("test1");

        assertEquals(testLabeler1, testLabeler1);

        assertNotEquals(testLabeler1, null);

        assertNotEquals(testLabeler1, new Object());

        assertNotEquals(testLabeler1, testLabeler2);
        assertNotEquals(testLabeler1, testLabeler3);
        assertNotEquals(testLabeler1, testLabeler4);
        assertNotEquals(testLabeler2, testLabeler3);
        assertNotEquals(testLabeler2, testLabeler4);
        assertNotEquals(testLabeler3, testLabeler5);

        assertEquals(testLabeler1, testLabeler5);
    }
}
