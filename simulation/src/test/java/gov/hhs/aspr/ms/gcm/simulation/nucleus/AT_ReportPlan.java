package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;

public class AT_ReportPlan {

    @Test
    @UnitTestConstructor(target = ReportPlan.class, args = { double.class})
    public void testConstructor() {
       //nothing to test
    }

    @Test
    @UnitTestConstructor(target = ReportPlan.class, args = { double.class, long.class})
    public void testConstructor_Arrival() {
    	//nothing to test
    }
}
