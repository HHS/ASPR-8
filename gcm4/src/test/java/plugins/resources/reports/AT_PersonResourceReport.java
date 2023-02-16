package plugins.resources.actors;

import org.junit.jupiter.api.Test;

import nucleus.ReportContext;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;
import plugins.resources.support.ResourceId;
import util.annotations.UnitTag;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_PersonResourceReport {

	@Test
	@UnitTestConstructor(target = PersonResourceReport.class, args = { ReportLabel.class, ReportPeriod.class, boolean.class, boolean.class, ResourceId[].class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonResourceReport.class, name = "init", args = { ReportContext.class }, tags = UnitTag.INCOMPLETE)
	public void testInit() {
		// test incomplete
	}

}