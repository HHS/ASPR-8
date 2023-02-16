package plugins.reports.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import nucleus.ExperimentContext;
import util.annotations.UnitTag;
import util.annotations.UnitTestMethod;

public class AT_NIOReportItemHandler {

	@Test
	@UnitTestMethod(target = NIOReportItemHandler.Builder.class, name = "build", args = {})
	public void testBuild() {
		NIOReportItemHandler.Builder builder = NIOReportItemHandler.builder();
		ReportLabel reportLabel1 = new SimpleReportLabel("testReportLabel1");
		ReportLabel reportLabel2 = new SimpleReportLabel("testReportLabel2");
		final Path path1 = Path.of("example_path1");
		final Path path2 = Path.of("example_path2");

		// show that a path collision error happens when 2 reports have the same
		// path
		builder.addReport(reportLabel1, path1);
		builder.addReport(reportLabel2, path1);

		ContractException contractException = assertThrows(ContractException.class, () -> builder.build());
		assertEquals(contractException.getErrorType(), ReportError.PATH_COLLISION);

		// show that what is built is not null
		builder.addReport(reportLabel1, path1);
		builder.addReport(reportLabel2, path2);
		assertNotNull(builder.build());
	}

	@Test
	@UnitTestMethod(target = NIOReportItemHandler.Builder.class, name = "addReport", args = { ReportLabel.class, Path.class }, tags = { UnitTag.MANUAL })
	public void testAddReport() {
		NIOReportItemHandler.Builder builder = NIOReportItemHandler.builder();
		ReportLabel reportLabel = new SimpleReportLabel("testReportLabel");
		final Path path1 = Path.of("example_path3");

		// null report label check
		ContractException pathContractException = assertThrows(ContractException.class, () -> builder.addReport(null, path1));
		assertEquals(pathContractException.getErrorType(), ReportError.NULL_REPORT_LABEL);

		// null report path check
		ContractException idContractException = assertThrows(ContractException.class, () -> builder.addReport(reportLabel, null));
		assertEquals(idContractException.getErrorType(), ReportError.NULL_REPORT_PATH);
		
		// the existence of the report file is covered by a manual test
	}

	@Test
	@UnitTestMethod(target = NIOReportItemHandler.Builder.class, name = "setDisplayExperimentColumnsInReports", args = { boolean.class }, tags = { UnitTag.MANUAL })
	public void testSetDisplayExperimentColumnsInReports() {
		NIOReportItemHandler.Builder builder = NIOReportItemHandler.builder();
		final boolean displayExperimentColumnsInReports = true;
		assertNotNull(builder.setDisplayExperimentColumnsInReports(displayExperimentColumnsInReports));
		// the existence of the columns in the reports is covered by manual test
	}

	@Test
	@UnitTestMethod(target = NIOReportItemHandler.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(NIOReportItemHandler.builder());
	}

	@Test
	@UnitTestMethod(target = NIOReportItemHandler.class, name = "accept", args = { ExperimentContext.class }, tags = { UnitTag.MANUAL })
	public void testAccept() {
		// covered by test for accept in manual test
	}

}