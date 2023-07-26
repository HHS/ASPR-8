package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.testsupport;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PartitionsContext;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_TestFilter {

    @Test
    @UnitTestConstructor(target = TestFilter.class, args = { int.class })
    public void testConstructor() {
        assertNotNull(new TestFilter(0));
    }

    @Test
    @UnitTestMethod(target = TestFilter.class, name = "getFilterId", args = {})
    public void testGetFilterId() {
        TestFilter testFilter = new TestFilter(0);

        assertEquals(0, testFilter.getFilterId());
    }

    @Test
    @UnitTestMethod(target = TestFilter.class, name = "evaluate", args = { PartitionsContext.class, PersonId.class })
    public void testEvaluate() {
        TestFilter testFilter = new TestFilter(0);

        assertTrue(testFilter.evaluate(null, null));
    }

    @Test
    @UnitTestMethod(target = TestFilter.class, name = "getFilterSensitivities", args = {})
    public void testGetFilterSensitivities() {
        TestFilter testFilter = new TestFilter(0);

        assertEquals(new LinkedHashSet<>(), testFilter.getFilterSensitivities());
    }

    @Test
    @UnitTestMethod(target = TestFilter.class, name = "validate", args = { PartitionsContext.class })
    public void testValidate() {
        TestFilter testFilter = new TestFilter(0);

        assertDoesNotThrow(() -> testFilter.validate(null));
    }

    @Test
    @UnitTestMethod(target = TestFilter.class, name = "hashCode", args = {})
    public void testHashCode() {
        TestFilter testFilter1 = new TestFilter(1);
        TestFilter testFilter2 = new TestFilter(2);
        TestFilter testFilter3 = new TestFilter(3);
        TestFilter testFilter4 = new TestFilter(4);
        TestFilter testFilter5 = new TestFilter(1);

        assertEquals(testFilter1.hashCode(), testFilter1.hashCode());

        assertNotEquals(testFilter1.hashCode(), testFilter2.hashCode());
        assertNotEquals(testFilter1.hashCode(), testFilter3.hashCode());
        assertNotEquals(testFilter1.hashCode(), testFilter4.hashCode());
        assertNotEquals(testFilter2.hashCode(), testFilter3.hashCode());
        assertNotEquals(testFilter2.hashCode(), testFilter4.hashCode());
        assertNotEquals(testFilter3.hashCode(), testFilter4.hashCode());

        assertEquals(testFilter1.hashCode(), testFilter5.hashCode());
    }

    @Test
    @UnitTestMethod(target = TestFilter.class, name = "equals", args = { Object.class })
    public void testEquals() {
        TestFilter testFilter1 = new TestFilter(1);
        TestFilter testFilter2 = new TestFilter(2);
        TestFilter testFilter3 = new TestFilter(3);
        TestFilter testFilter4 = new TestFilter(4);
        TestFilter testFilter5 = new TestFilter(1);

        assertEquals(testFilter1, testFilter1);

        assertNotEquals(testFilter1, null);

        assertNotEquals(testFilter1, new Object());

        assertNotEquals(testFilter1, testFilter2);
        assertNotEquals(testFilter1, testFilter3);
        assertNotEquals(testFilter1, testFilter4);
        assertNotEquals(testFilter2, testFilter3);
        assertNotEquals(testFilter2, testFilter4);
        assertNotEquals(testFilter3, testFilter4);

        assertEquals(testFilter1, testFilter5);
    }

    @Test
    @UnitTestMethod(target = TestFilter.class, name = "toString", args = {})
    public void testToString() {

        for (int i = 0; i < 10; i++) {
            TestFilter testFilter = new TestFilter(i);

            assertEquals("TestFilter [filterId=" + i + "]", testFilter.toString());
        }

    }
}
