package gov.hhs.aspr.ms.gcm.plugins.reports.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_ReportPeriod {

	@Test
	@UnitTestMethod(target = ReportPeriod.class, name = "next", args = {})
	public void testNext() {

		for (ReportPeriod reportPeriod : ReportPeriod.values()) {
			int n = ReportPeriod.values().length;
			int index = (reportPeriod.ordinal() + 1) % n;
			ReportPeriod expectedValue = ReportPeriod.values()[index];
			ReportPeriod actualValue = reportPeriod.next();
			assertEquals(expectedValue, actualValue);
		}

	}
}
