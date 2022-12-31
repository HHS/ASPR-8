package plugins.resources.actors;

import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportPeriod;
import plugins.resources.support.ResourceId;
import tools.annotations.UnitTag;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

public class AT_PersonResourceReport {

	@Test
	@UnitTestConstructor(target = PersonResourceReport.class, args = { ReportId.class, ReportPeriod.class, boolean.class, boolean.class, ResourceId[].class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonResourceReport.class, name = "init", args = { ActorContext.class }, tags = UnitTag.INCOMPLETE)
	public void testInit() {
		// test incomplete
	}

}