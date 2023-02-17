package plugins.reports.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import nucleus.Dimension;
import nucleus.Experiment;
import nucleus.ExperimentStatusConsole;
import nucleus.Plugin;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;

public final class MT_NIOReportItemHandler {

	

	private final Path basePath;
	
	private MT_NIOReportItemHandler(Path dirPath) {
		this.basePath = dirPath;
		
		
	}
	
	public static void main(String[] args) throws IOException {
		assertNotNull(args);
		assertEquals(args.length, 1);
		Path basePath = Paths.get(args[0]);
		if (!basePath.toFile().exists()) {
			throw new RuntimeException("base directory does not exist");
		}
		if (!basePath.toFile().isDirectory()) {
			throw new RuntimeException("base directory is not a directory");
		}
		new MT_NIOReportItemHandler(basePath).execute();
	}

	private void recursiveDelete(File file) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				File[] entries = file.listFiles();
				if (entries != null) {
					for (File entry : entries) {
						recursiveDelete(entry);
					}
				}
			}
			if (!file.delete()) {
				throw new IOException("Failed to delete " + file);
			}
		}
	}

	private void createDirectory(Path path) throws IOException {
		recursiveDelete(path.toFile());

		// investigate how to use the file attributes here
		Files.createDirectory(path);

	}

	private void execute() throws IOException {

		Path subPath = basePath.resolve("test1");
		createDirectory(subPath);
		test1(subPath);

		subPath = basePath.resolve("test2");
		createDirectory(subPath);
		test2(subPath);

		subPath = basePath.resolve("test3");
		createDirectory(subPath);
		test3(subPath);

		
		
		
		

	}

	/*
	 * no progress log written
	 * 
	 * no progress log read
	 *
	 * use experiment columns
	 * 
	 * write three reports
	 */
	private void test1(Path subPath) {
		ReportLabel reportLabel = new SimpleReportLabel("report label");

		ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
		reportHeaderBuilder.add("alpha");
		reportHeaderBuilder.add("beta");
		ReportHeader reportHeader = reportHeaderBuilder.build();

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

			for (int i = 0; i < 10; i++) {
				ReportItem.Builder reportItemBuilder = ReportItem.builder();
				reportItemBuilder.setReportHeader(reportHeader);
				reportItemBuilder.setReportLabel(reportLabel);
				reportItemBuilder.addValue(i);
				reportItemBuilder.addValue("value " + i);
				ReportItem reportItem = reportItemBuilder.build();
				c.releaseOutput(reportItem);
			}
		}));

		Dimension.Builder dimensionBuilder = Dimension.builder();
		dimensionBuilder.addMetaDatum("xxx");
		dimensionBuilder.addLevel((c) -> {
			List<String> result = new ArrayList<>();
			result.add("a");
			return result;
		});
		dimensionBuilder.addLevel((c) -> {
			List<String> result = new ArrayList<>();
			result.add("b");
			return result;
		});
		Dimension dimension1 = dimensionBuilder.build();

		dimensionBuilder.addMetaDatum("xyz");
		dimensionBuilder.addLevel((c) -> {
			List<String> result = new ArrayList<>();
			result.add("x");
			return result;
		});
		dimensionBuilder.addLevel((c) -> {
			List<String> result = new ArrayList<>();
			result.add("y");
			return result;
		});
		dimensionBuilder.addLevel((c) -> {
			List<String> result = new ArrayList<>();
			result.add("z");
			return result;
		});
		Dimension dimension2 = dimensionBuilder.build();

		NIOReportItemHandler nioReportItemHandler = //
				NIOReportItemHandler.builder()//
									.addReport(reportLabel, subPath.resolve("report1.txt"))//
									.build();

		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		Experiment	.builder()//
					.addPlugin(testPlugin)//
					.addDimension(dimension1)//
					.addDimension(dimension2)//
					.addExperimentContextConsumer(nioReportItemHandler)//
					.build()//
					.execute();

	}
	/*
	 * no progress log written
	 * 
	 * no progress log read
	 *
	 * no experiment columns
	 * 
	 * write three reports
	 * 
	 * expected observations : 
	 * 
	 * 
	 */
	private void test2(Path subPath) {
		ReportLabel reportLabel = new SimpleReportLabel("report label");

		ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
		reportHeaderBuilder.add("alpha");
		reportHeaderBuilder.add("beta");
		ReportHeader reportHeader = reportHeaderBuilder.build();

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

			for (int i = 0; i < 10; i++) {
				ReportItem.Builder reportItemBuilder = ReportItem.builder();
				reportItemBuilder.setReportHeader(reportHeader);
				reportItemBuilder.setReportLabel(reportLabel);
				reportItemBuilder.addValue(i);
				reportItemBuilder.addValue("value " + i);
				ReportItem reportItem = reportItemBuilder.build();
				c.releaseOutput(reportItem);
			}
		}));

		Dimension.Builder dimensionBuilder = Dimension.builder();
		dimensionBuilder.addMetaDatum("xxx");
		dimensionBuilder.addLevel((c) -> {
			List<String> result = new ArrayList<>();
			result.add("a");
			return result;
		});
		dimensionBuilder.addLevel((c) -> {
			List<String> result = new ArrayList<>();
			result.add("b");
			return result;
		});
		Dimension dimension1 = dimensionBuilder.build();

		dimensionBuilder.addMetaDatum("xyz");
		dimensionBuilder.addLevel((c) -> {
			List<String> result = new ArrayList<>();
			result.add("x");
			return result;
		});
		dimensionBuilder.addLevel((c) -> {
			List<String> result = new ArrayList<>();
			result.add("y");
			return result;
		});
		dimensionBuilder.addLevel((c) -> {
			List<String> result = new ArrayList<>();
			result.add("z");
			return result;
		});
		Dimension dimension2 = dimensionBuilder.build();

		NIOReportItemHandler nioReportItemHandler = //
				NIOReportItemHandler.builder()//
									.addReport(reportLabel, subPath.resolve("report1.txt"))//
									.setDisplayExperimentColumnsInReports(false)//
									.build();

		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		Experiment	.builder()//
					.addPlugin(testPlugin)//
					.addDimension(dimension1)//
					.addDimension(dimension2)//
					.addExperimentContextConsumer(nioReportItemHandler)//
					.build()//
					.execute();
	}
	/*
	 * write progress log
	 * 
	 * do not read progress log
	 * 
	 * write three reports
	 * 
	 * expected observations : 
	 * 
	 * 
	 */
	private void test3(Path subPath) {		
		
		ReportLabel reportLabel = new SimpleReportLabel("report label");

		ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
		reportHeaderBuilder.add("alpha");
		reportHeaderBuilder.add("beta");
		ReportHeader reportHeader = reportHeaderBuilder.build();

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

			for (int i = 0; i < 10; i++) {
				ReportItem.Builder reportItemBuilder = ReportItem.builder();
				reportItemBuilder.setReportHeader(reportHeader);
				reportItemBuilder.setReportLabel(reportLabel);
				reportItemBuilder.addValue(i);
				reportItemBuilder.addValue("value " + i);
				ReportItem reportItem = reportItemBuilder.build();
				c.releaseOutput(reportItem);
			}
		}));

		Dimension.Builder dimensionBuilder = Dimension.builder();
		dimensionBuilder.addMetaDatum("xxx");
		dimensionBuilder.addLevel((c) -> {
			List<String> result = new ArrayList<>();
			result.add("a");
			return result;
		});
		dimensionBuilder.addLevel((c) -> {
			List<String> result = new ArrayList<>();
			result.add("b");
			return result;
		});
		Dimension dimension1 = dimensionBuilder.build();

		dimensionBuilder.addMetaDatum("xyz");
		dimensionBuilder.addLevel((c) -> {
			List<String> result = new ArrayList<>();
			result.add("x");
			return result;
		});
		dimensionBuilder.addLevel((c) -> {
			List<String> result = new ArrayList<>();
			result.add("y");
			return result;
		});
		dimensionBuilder.addLevel((c) -> {
			List<String> result = new ArrayList<>();
			result.add("z");
			return result;
		});
		Dimension dimension2 = dimensionBuilder.build();

		NIOReportItemHandler nioReportItemHandler = //
				NIOReportItemHandler.builder()//
									.addReport(reportLabel, subPath.resolve("report1.txt"))//
									.build();

		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		
		
		ExperimentStatusConsole experimentStatusConsole = ExperimentStatusConsole.builder().build();

		Experiment	.builder()//
					.addPlugin(testPlugin)//
					.addDimension(dimension1)//
					.addDimension(dimension2)//
					.addExperimentContextConsumer(nioReportItemHandler)//
					.setExperimentProgressLog(subPath.resolve("progresslog.txt"))//
					.setContinueFromProgressLog(false)//
					.addExperimentContextConsumer(experimentStatusConsole)//
					.build()//
					.execute();
	}

	

	
}