package gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.DimensionContext;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_ExampleDimension {

    @Test
    @UnitTestConstructor(target = ExampleDimension.class, args = { String.class })
    public void testConstructor() {
        ExampleDimension exampleDimension = new ExampleDimension("test");

        assertNotNull(exampleDimension);
        assertEquals("test", exampleDimension.getLevelName());
    }

    @Test
    @UnitTestMethod(target = ExampleDimension.class, name = "executeLevel", args = { DimensionContext.class,
            int.class })
    public void testExecuteLevel() {
        ExampleDimension exampleDimension = new ExampleDimension("test");

        assertEquals(new ArrayList<>(), exampleDimension.executeLevel(null, 0));
    }

    @Test
    @UnitTestMethod(target = ExampleDimension.class, name = "getExperimentMetaData", args = {})
    public void testGetExperimentMetaData() {
        ExampleDimension exampleDimension = new ExampleDimension("test");

        assertEquals(new ArrayList<>(), exampleDimension.getExperimentMetaData());
    }

    @Test
    @UnitTestMethod(target = ExampleDimension.class, name = "getLevelName", args = {})
    public void testGetLevelName() {
        ExampleDimension exampleDimension = new ExampleDimension("test");

        assertEquals("test", exampleDimension.getLevelName());
    }

    @Test
    @UnitTestMethod(target = ExampleDimension.class, name = "levelCount", args = {})
    public void testLevelCount() {
        ExampleDimension exampleDimension = new ExampleDimension("test");

        assertEquals(5, exampleDimension.levelCount());
    }

    @Test
    @UnitTestMethod(target = ExampleDimension.class, name = "hashCode", args = {})
    public void testHashCode() {
        ExampleDimension exampleDimension1 = new ExampleDimension("test1");
        ExampleDimension exampleDimension2 = new ExampleDimension("test2");
        ExampleDimension exampleDimension3 = new ExampleDimension("test3");
        ExampleDimension exampleDimension4 = new ExampleDimension("test1");

        assertEquals(exampleDimension1.hashCode(), exampleDimension1.hashCode());

        assertNotEquals(exampleDimension1.hashCode(), exampleDimension2.hashCode());
        assertNotEquals(exampleDimension1.hashCode(), exampleDimension3.hashCode());
        assertNotEquals(exampleDimension2.hashCode(), exampleDimension3.hashCode());
        assertNotEquals(exampleDimension2.hashCode(), exampleDimension4.hashCode());

        assertEquals(exampleDimension1.hashCode(), exampleDimension4.hashCode());
    }

    @Test
    @UnitTestMethod(target = ExampleDimension.class, name = "equals", args = { Object.class })
    public void testEquals() {
        ExampleDimension exampleDimension1 = new ExampleDimension("test1");
        ExampleDimension exampleDimension2 = new ExampleDimension("test2");
        ExampleDimension exampleDimension3 = new ExampleDimension("test3");
        ExampleDimension exampleDimension4 = new ExampleDimension("test1");

        assertEquals(exampleDimension1, exampleDimension1);

        assertNotEquals(exampleDimension1, null);

        assertNotEquals(exampleDimension1, new Object());

        assertNotEquals(exampleDimension1, exampleDimension2);
        assertNotEquals(exampleDimension1, exampleDimension3);
        assertNotEquals(exampleDimension2, exampleDimension3);
        assertNotEquals(exampleDimension2, exampleDimension4);

        assertEquals(exampleDimension1, exampleDimension4);
    }
}
