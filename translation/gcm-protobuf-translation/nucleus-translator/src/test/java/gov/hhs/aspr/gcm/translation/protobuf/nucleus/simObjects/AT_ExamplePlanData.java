package gov.hhs.aspr.gcm.translation.protobuf.nucleus.simObjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_ExamplePlanData {

    @Test
    @UnitTestConstructor(target = ExamplePlanData.class, args = { double.class })
    public void testConstructor() {
        ExamplePlanData examplePlanData = new ExamplePlanData(10.0);

        assertNotNull(examplePlanData);
        assertEquals(10.0, examplePlanData.getPlanTime());
    }

    @Test
    @UnitTestMethod(target = ExamplePlanData.class, name = "getPlanTime", args = {})
    public void testGetPlanTime() {
        double expectedValue = 100.0;
        ExamplePlanData examplePlanData = new ExamplePlanData(expectedValue);

        assertEquals(expectedValue, examplePlanData.getPlanTime());
    }

    @Test
    @UnitTestMethod(target = ExamplePlanData.class, name = "hashCode", args = {})
    public void testHashCode() {
        ExamplePlanData examplePlanData1 = new ExamplePlanData(10.0);
        ExamplePlanData examplePlanData2 = new ExamplePlanData(100.0);
        ExamplePlanData examplePlanData3 = new ExamplePlanData(10.0);

        assertEquals(examplePlanData1.hashCode(), examplePlanData1.hashCode());

        assertNotEquals(examplePlanData1.hashCode(), examplePlanData2.hashCode());

        assertEquals(examplePlanData1.hashCode(), examplePlanData3.hashCode());
    }

    @Test
    @UnitTestMethod(target = ExamplePlanData.class, name = "equals", args = { Object.class })
    public void testEquals() {
        ExamplePlanData examplePlanData1 = new ExamplePlanData(10.0);
        ExamplePlanData examplePlanData2 = new ExamplePlanData(100.0);
        ExamplePlanData examplePlanData3 = new ExamplePlanData(10.0);

        assertEquals(examplePlanData1, examplePlanData1);

        assertNotEquals(examplePlanData1, null);
        assertNotEquals(examplePlanData1, new Object());

        assertNotEquals(examplePlanData1, examplePlanData2);

        assertEquals(examplePlanData1, examplePlanData3);
    }

}
