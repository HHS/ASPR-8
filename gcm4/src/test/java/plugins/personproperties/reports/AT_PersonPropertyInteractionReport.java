package plugins.personproperties.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.ReportContext;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.testsupport.TestPersonPropertyId;
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
		ReportLabel reportLabel = new SimpleReportLabel(1000);
		ReportPeriod reportPeriod = ReportPeriod.DAILY;
		Set<PersonPropertyId> expectedPersonPropertyIds = new LinkedHashSet<>();
		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK);
		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK);

		PersonPropertyInteractionReportPluginData.Builder builder = PersonPropertyInteractionReportPluginData.builder(); 
		builder.setReportLabel(reportLabel);
		builder.setReportPeriod(reportPeriod);
		for(PersonPropertyId personPropertyId : expectedPersonPropertyIds) {
			builder.addPersonPropertyId(personPropertyId);
		}
		
		PersonPropertyInteractionReportPluginData personPropertyInteractionReportPluginData = builder.build();
		PersonPropertyInteractionReport report = new PersonPropertyInteractionReport(personPropertyInteractionReportPluginData);

		assertNotNull(report);

		// precondition: report label is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyInteractionReportPluginData pluginData = PersonPropertyInteractionReportPluginData.builder().setReportPeriod(reportPeriod).build();
		    new PersonPropertyInteractionReport(pluginData);
		});
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());

		// precondition: report period is null
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyInteractionReportPluginData pluginData = PersonPropertyInteractionReportPluginData.builder().setReportLabel(reportLabel).build();
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
