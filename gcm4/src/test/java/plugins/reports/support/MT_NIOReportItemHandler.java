package plugins.reports.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.IntStream;

import nucleus.Dimension;
import nucleus.Experiment;
import nucleus.ExperimentContext;
import tools.annotations.UnitTag;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

public class MT_NIOReportItemHandler {

	private static enum ReportLabels implements ReportLabel {
		ALPHA("ALPHA.txt"), BETA("BETA.txt"),;

		private final String fileName;

		private ReportLabels(String fileName) {
			this.fileName = fileName;
		}

		public String getFileName() {
			return this.fileName;
		}
	}

	private final Path dirPath;

	private MT_NIOReportItemHandler(Path dirPath) {
		this.dirPath = dirPath;
	}

	private Dimension getDimension() {
		final Dimension.Builder dimensionBuilder = Dimension.builder();//
		IntStream.range(0, 10).forEach((i) -> {
			dimensionBuilder.addLevel((context) -> {
				final ArrayList<String> result = new ArrayList<>();
				result.add("x_" + Integer.toString(i));
				return result;
			});//
		});
		dimensionBuilder.addMetaDatum("header");//
		return dimensionBuilder.build();
	}

	public static void main(String[] args) {

		assertNotNull(args);
		assertEquals(args.length, 1);
		Path dirPath = Paths.get(args[0]);
		new MT_NIOReportItemHandler(dirPath).execute();
	}

	private void execute() {
		// testBuilder();
		// testAccept();
		// testBuild();
		// testAddReport();
		// testSetDisplayExperimentColumnsInReports();
		// testAcceptWithProgressLog();
	}

	private NIOReportItemHandler getNIOReportItemHandler() {

		NIOReportItemHandler.Builder builder = NIOReportItemHandler.builder();
		for (ReportLabels reportLabels : ReportLabels.values()) {
			builder.addReport(reportLabels, dirPath.resolve(reportLabels.getFileName()));
		}
		// builder.setDisplayExperimentColumnsInReports(false);
		return builder.build();
	}

	@UnitTestMethod(target = NIOReportItemHandler.Builder.class, name = "build", args = {})
	private void testBuild() {
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

	@UnitTestMethod(target = NIOReportItemHandler.Builder.class, name = "addReport", args = { ReportLabel.class, Path.class })
	private void testAddReport() {
		NIOReportItemHandler.Builder builder = NIOReportItemHandler.builder();
		ReportLabel reportLabel = new SimpleReportLabel("testReportLabel");
		final Path path1 = Path.of("example_path3");

		// null report label check
		ContractException pathContractException = assertThrows(ContractException.class, () -> builder.addReport(null, path1));
		assertEquals(pathContractException.getErrorType(), ReportError.NULL_REPORT_LABEL);

		// null report path check
		ContractException idContractException = assertThrows(ContractException.class, () -> builder.addReport(reportLabel, null));
		assertEquals(idContractException.getErrorType(), ReportError.NULL_REPORT_PATH);

	}

	@UnitTestMethod(target = NIOReportItemHandler.Builder.class, name = "setDisplayExperimentColumnsInReports", args = { boolean.class }, tags = { UnitTag.INCOMPLETE })
	private void testSetDisplayExperimentColumnsInReports() {
		NIOReportItemHandler.Builder builder = NIOReportItemHandler.builder();
		final boolean displayExperimentColumnsInReports = true;

		assertNotNull(builder.setDisplayExperimentColumnsInReports(displayExperimentColumnsInReports));

		fail("experiment columns still appear when set to false");

	}

	@UnitTestMethod(target = NIOReportItemHandler.class, name = "builder", args = {})
	private void testBuilder() {
		assertNotNull(getNIOReportItemHandler());
	}

	@UnitTestMethod(target = NIOReportItemHandler.class, name = "accept", args = { ExperimentContext.class })
	private void testAccept() {
		/*
		 * Procedure:
		 * 
		 * Select an existing directory that is empty
		 * 
		 * Run this method
		 * 
		 * Observe that each ENUM element has a corresponding empty file
		 * 
		 * Edit each file and put something in it
		 * 
		 * Run this method again
		 * 
		 * Observe that each file is now empty
		 * 
		 */
		Experiment	.builder()//
					.addExperimentContextConsumer(getNIOReportItemHandler())//
					.build()//
					.execute();
	}

	@UnitTestMethod(target = NIOReportItemHandler.class, name = "accept", args = { ExperimentContext.class })
	private void testAcceptWithProgressLog() {
		/*
		 * Procedure:
		 * 
		 * Copy files from src/test/resources/nioreportitemhandlermanualtesting
		 * into a local directory
		 * 
		 * Observe that the alpha and beta contain lines that aren't scenarios
		 * in the progress log
		 * 
		 * Run this method
		 * 
		 * Observe tha the scenarios not included in the progress log have been
		 * removed from alpha and beta
		 * 
		 * Observe that the executable terminated with an exit code of 0
		 * 
		 */
		Experiment	.builder()//
					.addExperimentContextConsumer(getNIOReportItemHandler())//
					.setContinueFromProgressLog(true)//
					.setExperimentProgressLog(dirPath.resolve("ProgressLog.txt"))//
					.addDimension(getDimension())//
					.build()//
					.execute();
	}
}