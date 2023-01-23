package plugins.personproperties.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import plugins.reports.support.ReportError;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportPeriod;
import plugins.reports.support.SimpleReportId;
import plugins.util.properties.PropertyError;
import tools.annotations.UnitTag;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_PersonPropertyReport {

	@Test
	@UnitTestMethod(target = PersonPropertyReport2.class, name = "builder", args = {})
	public void testBuilder() {
		PersonPropertyReport2.Builder builder = PersonPropertyReport2.builder();

		assertNotNull(builder);
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReport2.class, name = "init", args = { ActorContext.class }, tags = { UnitTag.INCOMPLETE })
	public void testInit() {
		// TBD
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReport2.Builder.class, name = "build", args = {})
	public void testBuild() {
		PersonPropertyReport2.Builder builder = PersonPropertyReport2.builder();

		builder.setReportId(new SimpleReportId(1000));
		builder.setReportPeriod(ReportPeriod.DAILY);

		PersonPropertyReport2 report = builder.build();

		assertNotNull(report);

		// precondition: null report id
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyReport2.builder().setReportPeriod(ReportPeriod.DAILY).build();
		});
		assertEquals(ReportError.NULL_REPORT_ID, contractException.getErrorType());

		// precondition: null report period
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyReport2.builder().setReportId(new SimpleReportId(1000)).build();
		});
		assertEquals(ReportError.NULL_REPORT_PERIOD, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReport2.Builder.class, name = "setReportId", args = { ReportId.class })
	public void testSetReportId() {
		PersonPropertyReport2.Builder builder = PersonPropertyReport2.builder();
		ReportId reportId = new SimpleReportId(1000);
		builder.setReportId(reportId);
		builder.setReportPeriod(ReportPeriod.DAILY);

		PersonPropertyReport2 report = builder.build();

		assertNotNull(report);

		// precondition: report id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyReport2.builder().setReportId(null);
		});
		assertEquals(ReportError.NULL_REPORT_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReport2.Builder.class, name = "setReportPeriod", args = { ReportPeriod.class })
	public void testSetReportPeriod() {
		PersonPropertyReport2.Builder builder = PersonPropertyReport2.builder();
		ReportId reportId = new SimpleReportId(1000);
		ReportPeriod reportPeriod = ReportPeriod.DAILY;
		builder.setReportId(reportId);
		builder.setReportPeriod(reportPeriod);

		PersonPropertyReport2 report = builder.build();

		assertNotNull(report);

		// precondition: report period is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyReport2.builder().setReportPeriod(null);
		});
		assertEquals(ReportError.NULL_REPORT_PERIOD, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReport2.Builder.class, name = "setDefaultInclusion", args = { boolean.class })
	public void testSetDefaultInclusion() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReport2.Builder.class, name = "includePersonProperty", args = { PersonPropertyId.class })
	public void testIncludePersonProperty() {
		PersonPropertyReport2.Builder builder = PersonPropertyReport2.builder();
		ReportId reportId = new SimpleReportId(1000);
		ReportPeriod reportPeriod = ReportPeriod.DAILY;
		builder.setReportId(reportId);
		builder.setReportPeriod(reportPeriod);
		builder.includePersonProperty(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK);

		PersonPropertyReport2 report = builder.build();

		assertNotNull(report);

		// precondition: person property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyReport2.builder().includePersonProperty(null);
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReport2.Builder.class, name = "excludePersonProperty", args = { PersonPropertyId.class })
	public void testExcludePersonProperty() {
		PersonPropertyReport2.Builder builder = PersonPropertyReport2.builder();
		ReportId reportId = new SimpleReportId(1000);
		ReportPeriod reportPeriod = ReportPeriod.DAILY;
		builder.setReportId(reportId);
		builder.setReportPeriod(reportPeriod);
		builder.excludePersonProperty(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK);

		PersonPropertyReport2 report = builder.build();

		assertNotNull(report);

		// precondition: person property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyReport2.builder().excludePersonProperty(null);
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
	}
}
