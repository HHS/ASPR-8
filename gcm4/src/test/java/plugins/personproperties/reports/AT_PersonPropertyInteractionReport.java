package plugins.personproperties.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import nucleus.ReportContext;
import plugins.personproperties.support.PersonPropertyId;
import plugins.reports.support.ReportError;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;
import plugins.reports.support.SimpleReportLabel;
import util.annotations.UnitTag;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_PersonPropertyInteractionReport {

	@Test
	@UnitTestConstructor(target = PersonPropertyInteractionReport.class, args = { ReportLabel.class, ReportPeriod.class, PersonPropertyId[].class })
	public void testConstructor() {
		
		// precondition: report label is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyInteractionReportPluginData pluginData = PersonPropertyInteractionReportPluginData.builder().setReportPeriod(ReportPeriod.DAILY).build();
		    new PersonPropertyInteractionReport(pluginData);
		});
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());

		// precondition: report period is null
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyInteractionReportPluginData pluginData = PersonPropertyInteractionReportPluginData.builder().setReportLabel(new SimpleReportLabel("label")).build();
		    new PersonPropertyInteractionReport(pluginData);
		});
		assertEquals(ReportError.NULL_REPORT_PERIOD, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyInteractionReport.class,name = "init", args = { ReportContext.class }, tags = { UnitTag.INCOMPLETE })
	public void testInit() {
		// TBD
	}
}
