package plugins.personproperties.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import nucleus.ReportContext;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import plugins.reports.support.ReportError;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;
import plugins.reports.support.SimpleReportLabel;
import tools.annotations.UnitTag;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_PersonPropertyInteractionReport {

	@Test
	@UnitTestConstructor(target = PersonPropertyInteractionReport.class, args = { ReportLabel.class, ReportPeriod.class, PersonPropertyId[].class })
	public void testConstructor() {
		ReportLabel reportLabel = new SimpleReportLabel(1000);
		ReportPeriod reportPeriod = ReportPeriod.DAILY;
		PersonPropertyId[] personPropertyIds = { TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK };

		PersonPropertyInteractionReport report = new PersonPropertyInteractionReport(reportLabel, reportPeriod, personPropertyIds);

		assertNotNull(report);

		// precondition: report label is null
		ContractException contractException = assertThrows(ContractException.class, () -> new PersonPropertyInteractionReport(null, reportPeriod, personPropertyIds));
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());

		// precondition: report period is null
		contractException = assertThrows(ContractException.class, () -> new PersonPropertyInteractionReport(reportLabel, null, personPropertyIds));
		assertEquals(ReportError.NULL_REPORT_PERIOD, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyInteractionReport.class,name = "init", args = { ReportContext.class }, tags = { UnitTag.INCOMPLETE })
	public void testInit() {
		// TBD
	}
}
