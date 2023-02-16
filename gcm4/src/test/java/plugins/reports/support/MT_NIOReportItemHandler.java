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
import util.annotations.UnitTag;
import util.annotations.UnitTestMethod;
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

	private final Path basePath;

	private MT_NIOReportItemHandler(Path dirPath) {
		this.basePath = dirPath;
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
		//create the base directory and then sub directories for each sub test
		//run subtest out to its own directory
		//manual inspection of the subdirectories for correct output
		
		
		//write 3 distinct reports
		//experiment with some dimensions that we can control in the output files
		//we need a user selected director for the inspection tests
		
		//we need variant on the manual test to conduct the various inpsections
		
		//we need to test with/without progress logs and reading from progress log - 4 choices
		
		//we need to run with and without experiment columms -- 2 choices
		
		testWithProgressLogWithReadFromProgressLog(basePath.resolve("test1"));
		
	}

	private NIOReportItemHandler getNIOReportItemHandler() {

		NIOReportItemHandler.Builder builder = NIOReportItemHandler.builder();
		for (ReportLabels reportLabels : ReportLabels.values()) {
			builder.addReport(reportLabels, basePath.resolve(reportLabels.getFileName()));
		}
		// builder.setDisplayExperimentColumnsInReports(false);
		return builder.build();
	}

	
	//@UnitTestMethod(target = NIOReportItemHandler.class, name = "accept", args = { ExperimentContext.class })
	private void testWithProgressLogWithReadFromProgressLog(Path dirPath) {
		
		Experiment	.builder()//
					.addExperimentContextConsumer(getNIOReportItemHandler())//
					.setContinueFromProgressLog(true)//
					.setExperimentProgressLog(dirPath.resolve("ProgressLog.txt"))//
					.addDimension(getDimension())//
					.build()//
					.execute();
	}
}