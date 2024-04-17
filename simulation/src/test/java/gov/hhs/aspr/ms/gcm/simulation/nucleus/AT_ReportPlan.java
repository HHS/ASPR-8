package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;

public class AT_ReportPlan {

    @Test
    @UnitTestConstructor(target = ReportPlan.class, args = { double.class, Consumer.class })
    public void testConstructor() {
        for (int i = 0; i < 10; i++) {
            ReportPlan reportPlan = new ReportPlan(i, (c) -> {
            });

            assertNotNull(reportPlan);
        }
    }

    @Test
    @UnitTestConstructor(target = ReportPlan.class, args = { double.class, long.class, Consumer.class })
    public void testConstructor_Arrival() {
        for (int i = 0; i < 10; i++) {
            ReportPlan reportPlan = new ReportPlan(i, (long) i, (c) -> {
            });

            assertNotNull(reportPlan);
        }
    }
}
