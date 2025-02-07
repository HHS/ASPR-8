package gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Dimension;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Experiment;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.ExperimentParameterData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.ExperimentStatusConsole;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.FunctionalDimension;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.FunctionalDimensionData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestActorPlan;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestPlugin;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestPluginData;

public final class MT_NIOReportItemHandler {

	private static enum Command {
		COMMENT("-c"), //
		HELP("-help"), //
		DIRECTORY("-d"), //
		TEST("-t"), //
		UNKNOWN("-")//
		;

		private String commandString;

		private Command(String commandString) {
			this.commandString = commandString;
		}

		public static Command getCommand(String value) {
			Command result = null;

			for (Command command : Command.values()) {
				if (command != UNKNOWN) {
					if (command.commandString.equals(value)) {
						result = command;
					}
				}
			}

			if (result == null) {
				if (value.startsWith("-")) {
					result = UNKNOWN;
					result.commandString = value;
				}
			}

			return result;
		}
	}

	private static void printInstructions() {

		StringBuilder sb = new StringBuilder();

		sb.append("Usage: " + "\n");
		sb.append("\t" + "Any order of the following commands are legal:" + "\n");
		sb.append("\t" + "\t" + "-c followed by any number arguments to ignore" + "\n");
		sb.append("\t" + "\t" + "-d followed by a directory name" + "\n");
		sb.append("\t" + "\t" + "-t followed by a test case number" + "\n");
		sb.append("\t" + "\t" + "-help for instructions" + "\n");
		sb.append("\t" + "Exactly one directory name and exactly one test case number are required.");
		sb.append("Example: " + "\n");
		sb.append("\t" + "-d c:\\temp\\src\\main\\java c:\\temp\\src\\test\\java" + "\n");
		sb.append("\t" + "-t 1" + "\n");
		sb.append("\t" + "-c testing" + "\n");
		sb.append("Test Cases: " + "\n");
		sb.append("\t" + "Test 1:" + "\n");
		sb.append("\t" + "\t" + "No progress log will be written, no progress log will be read, and " + "\n");
		sb.append("\t" + "\t" + "the experiment columns will be used. For this test, a custom delimiter is also set."
				+ "\n");
		sb.append("\t" + "Test 2:" + "\n");
		sb.append("\t" + "\t" + "No progress log will be written, no progress log will be read, and " + "\n");
		sb.append("\t" + "\t" + "no experiment columns will be used." + "\n");
		sb.append("\t" + "Test 3:" + "\n");
		sb.append("\t" + "\t" + "A progress log will be written, the progress log won't be read, and " + "\n");
		sb.append("\t" + "\t" + "no experiment columns will be used" + "\n");
		sb.append("\t" + "Test 4:" + "\n");
		sb.append("\t" + "\t" + "A progress log will be written, the progress log will be read, and " + "\n");
		sb.append("\t" + "\t" + "the experiment columns will be used" + "\n");
		sb.append("\t" + "Test 5:" + "\n");
		sb.append("\t" + "\t" + "No progress log will be written, an attempt at reading a non-existent progress log "
				+ "\n");
		sb.append("\t" + "\t" + "will be made, and the experiment columns will be used" + "\n");
		sb.append("\t" + "Test 6:" + "\n");
		sb.append("\t" + "\t" + "No progress log will be written, no progress log will be read, and " + "\n");
		sb.append("\t" + "\t" + "the experiment columns will be used. For this test, a custom delimiter is also set."
				+ "\n");
		sb.append("\t" + "\t" + "The process for this test will be executed twice." + "\n");
		sb.append("\t" + "Test 7:" + "\n");
		sb.append("\t" + "\t" + "No progress log will be written, no progress log will be read, and " + "\n");
		sb.append("\t" + "\t" + "the experiment columns will be used. For this test, a custom delimiter is also set."
				+ "\n");
		sb.append("\t" + "\t" + "An experiment report will be written." + "\n");
		System.out.println(sb);
	}

	private static class CommandBlock {
		private final Command command;
		private final List<String> arguments = new ArrayList<>();

		public CommandBlock(Command command) {
			this.command = command;
		}

	}

	private final Path basePath;
	private Integer testToRun;

	private MT_NIOReportItemHandler(Path dirPath, Integer testToRun) {
		this.basePath = dirPath;
		this.testToRun = testToRun;
	}

	public static void main(String[] args) throws IOException {

		Map<Command, List<CommandBlock>> commandBlocks = new LinkedHashMap<>();
		for (Command command : Command.values()) {
			commandBlocks.put(command, new ArrayList<>());
		}
		CommandBlock currentCommandBlock = new CommandBlock(Command.UNKNOWN);
		commandBlocks.get(currentCommandBlock.command).add(currentCommandBlock);

		for (String arg : args) {
			Command command = Command.getCommand(arg);
			if (command != null) {
				currentCommandBlock = new CommandBlock(command);
				commandBlocks.get(command).add(currentCommandBlock);
			} else {
				currentCommandBlock.arguments.add(arg);
			}
		}

		Path basePath = null;
		int testIndex = 0;

		// HELP
		int directoryCount = commandBlocks.get(Command.DIRECTORY).size();
		int testCount = commandBlocks.get(Command.TEST).size();

		if (directoryCount == 0 && testCount == 0) {
			printInstructions();
			return;
		}

		List<CommandBlock> blocks = commandBlocks.get(Command.HELP);
		if (!blocks.isEmpty()) {
			printInstructions();
			return;
		}

		// DIRECTORY
		blocks = commandBlocks.get(Command.DIRECTORY);
		if (blocks.isEmpty()) {
			throw new RuntimeException("requires a directory command -d");
		}

		if (blocks.size() > 1) {
			throw new RuntimeException("too many directory commands -d");
		}

		CommandBlock commandBlock = blocks.get(0);
		if (commandBlock.arguments.isEmpty()) {
			throw new RuntimeException("requires exactly one directory for -d");
		}

		if (commandBlock.arguments.size() > 1) {
			throw new RuntimeException("too many directories listed for -d");
		}

		String directoryName = commandBlock.arguments.get(0);
		basePath = Paths.get(directoryName);
		if (!basePath.toFile().exists()) {
			throw new RuntimeException("base directory does not exist");
		}
		if (!basePath.toFile().isDirectory()) {
			throw new RuntimeException("base directory is not a directory");
		}

		// TEST

		blocks = commandBlocks.get(Command.TEST);
		if (blocks.isEmpty()) {
			throw new RuntimeException("requires a test command -t");
		}

		if (blocks.size() > 1) {
			throw new RuntimeException("too many test commands -t");
		}

		commandBlock = blocks.get(0);
		if (commandBlock.arguments.isEmpty()) {
			throw new RuntimeException("requires exactly one test number for -t");
		}

		if (commandBlock.arguments.size() > 1) {
			throw new RuntimeException("too many test numbers listed for -t");
		}
		try {
			testIndex = Integer.parseInt(commandBlock.arguments.get(0));
		} catch (NumberFormatException e) {
			throw new RuntimeException("test index needs to be a integer", e);
		}

		if (testIndex < 1 || testIndex > 7) {
			throw new RuntimeException("test index out of bounds");
		}

		// UNKNOWN
		blocks = commandBlocks.get(Command.UNKNOWN);

		if (blocks.size() > 1) {
			StringBuilder sb = new StringBuilder();
			String unknownCommand = commandBlocks.get(Command.UNKNOWN).get(0).command.commandString;
			if (!currentCommandBlock.arguments.isEmpty()) {
				String unknownCommandArgs = currentCommandBlock.arguments.get(0);
				sb.append("encountered an unknown command: ");
				sb.append(unknownCommand);
				sb.append(" ");
				sb.append(unknownCommandArgs);
			} else {
				sb.append("encountered an unknown command: ");
				sb.append(unknownCommand);
			}
			throw new RuntimeException(sb.toString());
		}

		new MT_NIOReportItemHandler(basePath, testIndex).execute();
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
		Files.createDirectory(path);

	}

	private void execute() throws IOException {
		switch (testToRun) {
		case 1:
			Path subPath = basePath.resolve("test1");
			createDirectory(subPath);
			printExpected(1);
			test1(subPath);
			break;
		case 2:
			subPath = basePath.resolve("test2");
			createDirectory(subPath);
			printExpected(2);
			test2(subPath);
			break;
		case 3:
			subPath = basePath.resolve("test3");
			createDirectory(subPath);
			printExpected(3);
			test3(subPath);
			break;
		case 4:
			subPath = basePath.resolve("test4");
			createDirectory(subPath);
			printExpected(4);
			test4(subPath);
			break;
		case 5:
			subPath = basePath.resolve("test5");
			createDirectory(subPath);
			printExpected(5);
			test5(subPath);
			break;
		case 6:
			subPath = basePath.resolve("test6");
			createDirectory(subPath);
			printExpected(6);
			test1(subPath);
			test1(subPath);
			break;
		case 7:
			subPath = basePath.resolve("test7");
			createDirectory(subPath);
			printExpected(7);
			test7(subPath);
			break;
		default:
			throw new RuntimeException("unknown test number: " + testToRun);
		}
	}

	/*
	 * no progress log written
	 * 
	 * no progress log read
	 *
	 * use experiment columns
	 *
	 * delimiter set
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

			c.releaseOutput(reportHeader);

			for (int i = 0; i < 10; i++) {
				ReportItem.Builder reportItemBuilder = ReportItem.builder();
				reportItemBuilder.setReportLabel(reportLabel);
				reportItemBuilder.addValue(i);
				reportItemBuilder.addValue("value " + i);
				ReportItem reportItem = reportItemBuilder.build();
				c.releaseOutput(reportItem);
			}
		}));

		FunctionalDimensionData dimensionData1 = FunctionalDimensionData.builder()//
				.addMetaDatum("xxx")//
				.addValue("Level_0", (c) -> {
					List<String> result = new ArrayList<>();
					result.add("a");
					return result;
				}).addValue("Level_1", (c) -> {
					List<String> result = new ArrayList<>();
					result.add("b");
					return result;
				}).build();
		Dimension dimension1 = new FunctionalDimension(dimensionData1);

		FunctionalDimensionData dimensionData2 = FunctionalDimensionData.builder()//
				.addMetaDatum("xyz")
				.addValue("Level_0", (c) -> {
					List<String> result = new ArrayList<>();
					result.add("x");
					return result;
				}).addValue("Level_1", (c) -> {
					List<String> result = new ArrayList<>();
					result.add("y");
					return result;
				}).addValue("Level_1", (c) -> {
					List<String> result = new ArrayList<>();
					result.add("z");
					return result;
				}).build();
		Dimension dimension2 = new FunctionalDimension(dimensionData2);

		NIOReportItemHandler nioReportItemHandler = //
				NIOReportItemHandler.builder()//
						.addReport(reportLabel, subPath.resolve("report1.txt"))//
						.setDelimiter(",").build();

		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		ExperimentStatusConsole experimentStatusConsole = ExperimentStatusConsole.builder().build();

		Experiment.builder()//
				.addPlugin(testPlugin)//
				.addDimension(dimension1)//
				.addDimension(dimension2)//
				.addExperimentContextConsumer(nioReportItemHandler)//
				.addExperimentContextConsumer(experimentStatusConsole)//
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
	 */
	private void test2(Path subPath) {
		ReportLabel reportLabel = new SimpleReportLabel("report label");

		ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
		reportHeaderBuilder.add("alpha");
		reportHeaderBuilder.add("beta");
		ReportHeader reportHeader = reportHeaderBuilder.build();

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

			c.releaseOutput(reportHeader);

			for (int i = 0; i < 10; i++) {
				ReportItem.Builder reportItemBuilder = ReportItem.builder();
				reportItemBuilder.setReportLabel(reportLabel);
				reportItemBuilder.addValue(i);
				reportItemBuilder.addValue("value " + i);
				ReportItem reportItem = reportItemBuilder.build();
				c.releaseOutput(reportItem);
			}
		}));

		FunctionalDimensionData dimensionData1 = FunctionalDimensionData.builder()//
				.addMetaDatum("xxx")//
				.addValue("Level_0", (c) -> {
					List<String> result = new ArrayList<>();
					result.add("a");
					return result;
				})//
				.addValue("Level_1", (c) -> {
					List<String> result = new ArrayList<>();
					result.add("b");
					return result;
				})//
				.build();
		FunctionalDimension dimension1 = new FunctionalDimension(dimensionData1);

		FunctionalDimensionData dimensionData2 = FunctionalDimensionData.builder()//
				.addMetaDatum("xyz")//
				.addValue("Level_0", (c) -> {
					List<String> result = new ArrayList<>();
					result.add("x");
					return result;
				})//
				.addValue("Level_1", (c) -> {
					List<String> result = new ArrayList<>();
					result.add("y");
					return result;
				})//
				.addValue("Level_2", (c) -> {
					List<String> result = new ArrayList<>();
					result.add("z");
					return result;
				})//
				.build();//
		Dimension dimension2 = new FunctionalDimension(dimensionData2);

		NIOReportItemHandler nioReportItemHandler = //
				NIOReportItemHandler.builder()//
						.addReport(reportLabel, subPath.resolve("report1.txt"))//
						.setDisplayExperimentColumnsInReports(false)//
						.build();

		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		ExperimentStatusConsole experimentStatusConsole = ExperimentStatusConsole.builder().build();

		Experiment.builder()//
				.addPlugin(testPlugin)//
				.addDimension(dimension1)//
				.addDimension(dimension2)//
				.addExperimentContextConsumer(nioReportItemHandler)//
				.addExperimentContextConsumer(experimentStatusConsole)//
				.build()//
				.execute();
	}

	/*
	 * write progress log
	 * 
	 * do not read progress log
	 * 
	 * write three reports
	 */
	private void test3(Path subPath) {

		ReportLabel reportLabel = new SimpleReportLabel("report label");

		ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
		reportHeaderBuilder.add("alpha");
		reportHeaderBuilder.add("beta");
		ReportHeader reportHeader = reportHeaderBuilder.build();

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

			c.releaseOutput(reportHeader);

			for (int i = 0; i < 10; i++) {
				ReportItem.Builder reportItemBuilder = ReportItem.builder();
				reportItemBuilder.setReportLabel(reportLabel);
				reportItemBuilder.addValue(i);
				reportItemBuilder.addValue("value " + i);
				ReportItem reportItem = reportItemBuilder.build();
				c.releaseOutput(reportItem);
			}
		}));

		FunctionalDimensionData dimensionData1 = FunctionalDimensionData.builder()//
				.addMetaDatum("xxx")//
				.addValue("Level_0", (c) -> {
					List<String> result = new ArrayList<>();
					result.add("a");
					return result;
				})//
				.addValue("Level_1", (c) -> {
					List<String> result = new ArrayList<>();
					result.add("b");
					return result;
				})//
				.build();
		Dimension dimension1 = new FunctionalDimension(dimensionData1);

		FunctionalDimensionData dimensionData2 = FunctionalDimensionData.builder()//
				.addMetaDatum("xyz")//
				.addValue("Level_0", (c) -> {
					List<String> result = new ArrayList<>();
					result.add("x");
					return result;
				})//
				.addValue("Level_1", (c) -> {
					List<String> result = new ArrayList<>();
					result.add("y");
					return result;
				})//
				.addValue("Level_2", (c) -> {
					List<String> result = new ArrayList<>();
					result.add("z");
					return result;
				})//
				.build();
		Dimension dimension2 = new FunctionalDimension(dimensionData2);

		NIOReportItemHandler nioReportItemHandler = //
				NIOReportItemHandler.builder()//
						.addReport(reportLabel, subPath.resolve("report1.txt"))//
						.build();

		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		ExperimentStatusConsole experimentStatusConsole = ExperimentStatusConsole.builder().build();

		ExperimentParameterData experimentParameterData = ExperimentParameterData.builder()//
				.setExperimentProgressLog(subPath.resolve("progresslog.txt"))//
				.setContinueFromProgressLog(false)//
				.build();

		Experiment.builder()//
				.addPlugin(testPlugin)//
				.addDimension(dimension1)//
				.addDimension(dimension2)//
				.addExperimentContextConsumer(nioReportItemHandler)//
				.addExperimentContextConsumer(experimentStatusConsole)//
				.setExperimentParameterData(experimentParameterData)//
				.build()//
				.execute();
	}

	/*
	 * write progress log
	 *
	 * read progress log
	 *
	 * write three reports
	 *
	 */
	private void test4(Path subPath) throws IOException {

		CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
		OutputStream out = Files.newOutputStream(subPath.resolve("progresslog.txt"), StandardOpenOption.CREATE);
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, encoder));

		writer.write("scenario");
		writer.write("\t");
		writer.write("xxx");
		writer.write("\t");
		writer.write("xyz");
		writer.newLine();

		writer.write("0");
		writer.write("\t");
		writer.write("a");
		writer.write("\t");
		writer.write("x");
		writer.newLine();

		writer.write("1");
		writer.write("\t");
		writer.write("b");
		writer.write("\t");
		writer.write("x");
		writer.newLine();

		writer.close();

		ReportLabel reportLabel = new SimpleReportLabel("report label");

		ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
		reportHeaderBuilder.add("alpha");
		reportHeaderBuilder.add("beta");
		ReportHeader reportHeader = reportHeaderBuilder.build();

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

			c.releaseOutput(reportHeader);

			for (int i = 0; i < 10; i++) {
				ReportItem.Builder reportItemBuilder = ReportItem.builder();
				reportItemBuilder.setReportLabel(reportLabel);
				reportItemBuilder.addValue(i);
				reportItemBuilder.addValue("value " + i);
				ReportItem reportItem = reportItemBuilder.build();
				c.releaseOutput(reportItem);
			}
		}));

		FunctionalDimensionData dimensionData1 = FunctionalDimensionData.builder()//
				.addMetaDatum("xxx")//
				.addValue("Level_0", (c) -> {
					List<String> result = new ArrayList<>();
					result.add("a");
					return result;
				})//
				.addValue("Level_1", (c) -> {
					List<String> result = new ArrayList<>();
					result.add("b");
					return result;
				})//
				.build();//
		Dimension dimension1 = new FunctionalDimension(dimensionData1);

		FunctionalDimensionData dimensionData2 = FunctionalDimensionData.builder()//
				.addMetaDatum("xyz")//
				.addValue("Level_0", (c) -> {
					List<String> result = new ArrayList<>();
					result.add("x");
					return result;
				})//
				.addValue("Level_1", (c) -> {
					List<String> result = new ArrayList<>();
					result.add("y");
					return result;
				})//
				.addValue("Level_2", (c) -> {
					List<String> result = new ArrayList<>();
					result.add("z");
					return result;
				})//
				.build();//
		Dimension dimension2 = new FunctionalDimension(dimensionData2);

		NIOReportItemHandler nioReportItemHandler = //
				NIOReportItemHandler.builder()//
						.addReport(reportLabel, subPath.resolve("report1.txt"))//
						.build();

		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		ExperimentStatusConsole experimentStatusConsole = ExperimentStatusConsole.builder().build();

		ExperimentParameterData experimentParameterData = ExperimentParameterData.builder()//
				.setExperimentProgressLog(subPath.resolve("progresslog.txt"))//
				.setContinueFromProgressLog(true)//
				.build();

		Experiment.builder()//
				.addPlugin(testPlugin)//
				.addDimension(dimension1)//
				.addDimension(dimension2)//
				.addExperimentContextConsumer(nioReportItemHandler)//
				.addExperimentContextConsumer(experimentStatusConsole)//
				.setExperimentParameterData(experimentParameterData)//
				.build()//
				.execute();

	}

	/*
	 * no progress log written
	 *
	 * progress log read
	 *
	 * write three reports
	 *
	 * use experiment columns
	 *
	 */
	private void test5(Path subPath) {

		ReportLabel reportLabel = new SimpleReportLabel("report label");

		ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
		reportHeaderBuilder.add("alpha");
		reportHeaderBuilder.add("beta");
		ReportHeader reportHeader = reportHeaderBuilder.build();

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

			c.releaseOutput(reportHeader);

			for (int i = 0; i < 10; i++) {
				ReportItem.Builder reportItemBuilder = ReportItem.builder();
				reportItemBuilder.setReportLabel(reportLabel);
				reportItemBuilder.addValue(i);
				reportItemBuilder.addValue("value " + i);
				ReportItem reportItem = reportItemBuilder.build();
				c.releaseOutput(reportItem);
			}
		}));

		FunctionalDimensionData dimensionData1 = FunctionalDimensionData.builder()//
				.addMetaDatum("xxx")//
				.addValue("Level_0", (c) -> {
					List<String> result = new ArrayList<>();
					result.add("a");
					return result;
				})//
				.addValue("Level_1", (c) -> {
					List<String> result = new ArrayList<>();
					result.add("b");
					return result;
				})//
				.build();
		Dimension dimension1 = new FunctionalDimension(dimensionData1);

		FunctionalDimensionData dimensionData2 = FunctionalDimensionData.builder()//
				.addMetaDatum("xyz")//
				.addValue("Level_0", (c) -> {
					List<String> result = new ArrayList<>();
					result.add("x");
					return result;
				})//
				.addValue("Level_1", (c) -> {
					List<String> result = new ArrayList<>();
					result.add("y");
					return result;
				})//
				.addValue("Level_2", (c) -> {
					List<String> result = new ArrayList<>();
					result.add("z");
					return result;
				})//
				.build();
		Dimension dimension2 = new FunctionalDimension(dimensionData2);

		NIOReportItemHandler nioReportItemHandler = //
				NIOReportItemHandler.builder()//
						.addReport(reportLabel, subPath.resolve("report1.txt"))//
						.build();

		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		ExperimentStatusConsole experimentStatusConsole = ExperimentStatusConsole.builder().build();

		ExperimentParameterData experimentParameterData = ExperimentParameterData.builder()//
				.setExperimentProgressLog(subPath.resolve("progresslog.txt"))//
				.setContinueFromProgressLog(true)//
				.build();

		Experiment.builder()//
				.addPlugin(testPlugin)//
				.addDimension(dimension1)//
				.addDimension(dimension2)//
				.addExperimentContextConsumer(nioReportItemHandler)//
				.addExperimentContextConsumer(experimentStatusConsole)//
				.setExperimentParameterData(experimentParameterData)//
				.build()//
				.execute();

	}

	/*
	 * no progress log written
	 *
	 * no progress log read
	 *
	 * use experiment columns
	 *
	 * delimiter set
	 *
	 * write four reports
	 *
	 * write one experiment report
	 */
	private void test7(Path subPath) {
		ReportLabel reportLabel = new SimpleReportLabel("report label");

		ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
		reportHeaderBuilder.add("alpha");
		reportHeaderBuilder.add("beta");
		ReportHeader reportHeader = reportHeaderBuilder.build();

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

			c.releaseOutput(reportHeader);
			for (int i = 0; i < 10; i++) {
				ReportItem.Builder reportItemBuilder = ReportItem.builder();
				reportItemBuilder.setReportLabel(reportLabel);
				reportItemBuilder.addValue(i);
				reportItemBuilder.addValue("value " + i);
				ReportItem reportItem = reportItemBuilder.build();
				c.releaseOutput(reportItem);
			}
		}));

		FunctionalDimensionData dimensionData1 = FunctionalDimensionData.builder()//
				.addMetaDatum("xxx")//
				.addValue("Level_0", (c) -> {
					List<String> result = new ArrayList<>();
					result.add("a");
					return result;
				}).addValue("Level_1", (c) -> {
					List<String> result = new ArrayList<>();
					result.add("b");
					return result;
				}).build();
		Dimension dimension1 = new FunctionalDimension(dimensionData1);

		FunctionalDimensionData dimensionData2 = FunctionalDimensionData.builder()//
				.addMetaDatum("xyz")//
				.addValue("Level_0", (c) -> {
					List<String> result = new ArrayList<>();
					result.add("x");
					return result;
				})//
				.addValue("Level_1", (c) -> {
					List<String> result = new ArrayList<>();
					result.add("y");
					return result;
				})//
				.addValue("Level_2", (c) -> {
					List<String> result = new ArrayList<>();
					result.add("z");
					return result;
				})//
				.build();
		Dimension dimension2 = new FunctionalDimension(dimensionData2);

		NIOReportItemHandler nioReportItemHandler = //
				NIOReportItemHandler.builder()//
						.addReport(reportLabel, subPath.resolve("report1.csv"))//
						.addExperimentReport(subPath.resolve("experiment_report.csv")).setDelimiter(",").build();

		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		ExperimentStatusConsole experimentStatusConsole = ExperimentStatusConsole.builder().build();

		Experiment.builder()//
				.addPlugin(testPlugin)//
				.addDimension(dimension1)//
				.addDimension(dimension2)//
				.addExperimentContextConsumer(nioReportItemHandler)//
				.addExperimentContextConsumer(experimentStatusConsole)//
				.build()//
				.execute();
	}

	private void printExpected(Integer testNum) {

		StringBuilder sb = new StringBuilder();

		switch (testNum) {
		case 1:
			sb.append(
					"This test is meant to prove that when we run a simulation, we can generate a basic report with experiment columns."
							+ "\n");
			sb.append("Expected Observations: " + "\n");
			sb.append("\t" + "After all 6 scenarios are completed, the console should show 1" + "\n");
			sb.append("\t" + "value. You should observe a SUCCEEDED value of 6." + "\n");
			sb.append("\t" + "A folder named 'test1' should appear in the specified directory." + "\n");
			sb.append("\t" + "A file named 'report1.txt' should be in the 'test1' folder." + "\n");
			sb.append("\t" + "The file's data should be comma separated." + "\n");
			sb.append("\t" + "The header of the text file should have the following columns: " + "\n");
			sb.append("\t" + "\t" + "scenario" + "\n");
			sb.append("\t" + "\t" + "xxx" + "\n");
			sb.append("\t" + "\t" + "xyz" + "\n");
			sb.append("\t" + "\t" + "alpha" + "\n");
			sb.append("\t" + "\t" + "beta" + "\n");
			sb.append("------------------------------ CONSOLE OUTPUT ------------------------------" + "\n");
			break;
		case 2:
			sb.append(
					"This test is meant to prove that when we run a simulation, we can generate a basic report without experiment columns."
							+ "\n");
			sb.append("Expected Observations: " + "\n");
			sb.append("\t" + "After all 6 scenarios are completed, the console should show 1" + "\n");
			sb.append("\t" + "value. You should observe a SUCCEEDED value of 6." + "\n");
			sb.append("\t" + "A folder named 'test2' should appear in the specified directory." + "\n");
			sb.append("\t" + "A file named 'report1.txt' should be in the 'test2' folder." + "\n");
			sb.append("\t" + "The file's data should be tab separated." + "\n");
			sb.append("\t" + "The header of the text file should have the following columns: " + "\n");
			sb.append("\t" + "\t" + "scenario" + "\n");
			sb.append("\t" + "\t" + "alpha" + "\n");
			sb.append("\t" + "\t" + "beta" + "\n");
			sb.append("------------------------------ CONSOLE OUTPUT ------------------------------" + "\n");
			break;
		case 3:
			sb.append(
					"This test is meant to prove that when we run a simulation, we can generate a basic report as well as a progress log."
							+ "\n");
			sb.append("Expected observations: " + "\n");
			sb.append("\t" + "After all 6 scenarios are completed, the console should show 1" + "\n");
			sb.append("\t" + "value. You should observe a SUCCEEDED value of 6." + "\n");
			sb.append("\t" + "A folder named 'test3' should appear in the specified directory." + "\n");
			sb.append("\t" + "A file named 'report1.txt' should be in the 'test3' folder." + "\n");
			sb.append("\t" + "The file's data should be tab separated." + "\n");
			sb.append("\t" + "The header of the text file should have the following columns.: " + "\n");
			sb.append("\t" + "\t" + "scenario" + "\n");
			sb.append("\t" + "\t" + "xxx" + "\n");
			sb.append("\t" + "\t" + "xyz" + "\n");
			sb.append("\t" + "\t" + "alpha" + "\n");
			sb.append("\t" + "\t" + "beta" + "\n");
			sb.append("\t" + "Another file named 'progresslog.txt' should be in the folder." + "\n");
			sb.append("\t" + "The header of the progress log file should have the following columns: " + "\n");
			sb.append("\t" + "\t" + "scenario" + "\n");
			sb.append("\t" + "\t" + "xxx" + "\n");
			sb.append("\t" + "\t" + "xyz" + "\n");
			sb.append("------------------------------ CONSOLE OUTPUT ------------------------------" + "\n");
			break;
		case 4:
			sb.append(
					"This test is meant to prove that when a simulation run is interrupted, we can complete the simulation using the progress log."
							+ "\n");
			sb.append("Expected observations: " + "\n");
			sb.append("\t" + "After all 6 scenarios are completed, the console should show 2" + "\n");
			sb.append("\t" + "values. You should observe PREVIOUSLY_SUCCEEDED and SUCCEEDED values" + "\n");
			sb.append("\t" + "whose sum should total up to 6." + "\n");
			sb.append("\t" + "A folder named 'test4' should appear in the specified directory." + "\n");
			sb.append("\t" + "A file named 'report1.txt' should be in the 'test4' folder." + "\n");
			sb.append("\t" + "The file's data should be tab separated." + "\n");
			sb.append("\t" + "The header of the text file should have the following columns: " + "\n");
			sb.append("\t" + "\t" + "scenario" + "\n");
			sb.append("\t" + "\t" + "xxx" + "\n");
			sb.append("\t" + "\t" + "xyz" + "\n");
			sb.append("\t" + "\t" + "alpha" + "\n");
			sb.append("\t" + "\t" + "beta" + "\n");
			sb.append("\t" + "Another file named 'progresslog.txt' should be in the folder." + "\n");
			sb.append("\t" + "The header of the progress log file should have the following columns: " + "\n");
			sb.append("\t" + "\t" + "scenario" + "\n");
			sb.append("\t" + "\t" + "xxx" + "\n");
			sb.append("\t" + "\t" + "xyz" + "\n");
			sb.append("------------------------------ CONSOLE OUTPUT ------------------------------" + "\n");
			break;
		case 5:
			sb.append(
					"This test is meant to prove that when attempting to complete a simulation using a non existing progress log, "
							+ "\n");
			sb.append("that the proper contract exception is thrown." + "\n");
			sb.append("Expected observations: " + "\n");
			sb.append(
					"\t" + "After running test 5, you should receive an exception with the following message: " + "\n");
			sb.append("\t"
					+ "Exception in thread \"main\" util.errors.ContractException: The scenario progress file does not exist,"
					+ "\n");
			sb.append("\t" + "but is required when continuation from progress file is chosen" + "\n");
			sb.append("------------------------------ CONSOLE OUTPUT ------------------------------" + "\n");
			break;
		case 6:
			sb.append(
					"This test is meant to prove that when we can run a simulation multiple times without encountering an exception."
							+ "\n");
			sb.append("Expected Observations: " + "\n");
			sb.append("\t" + "After the first 6 scenarios are completed, the console should show 1" + "\n");
			sb.append("\t" + "value. You should observe a SUCCEEDED value of 6." + "\n");
			sb.append("\t" + "The 6 scenarios should run a second time and the same SUCCEEDED value should appear."
					+ "\n");
			sb.append("\t" + "A folder named 'test6' should appear in the specified directory." + "\n");
			sb.append("\t" + "A file named 'report1.txt' should be in the 'test6' folder." + "\n");
			sb.append("\t" + "The file's data should be comma separated." + "\n");
			sb.append("\t" + "The header of the text file should have the following columns: " + "\n");
			sb.append("\t" + "\t" + "scenario" + "\n");
			sb.append("\t" + "\t" + "xxx" + "\n");
			sb.append("\t" + "\t" + "xyz" + "\n");
			sb.append("\t" + "\t" + "alpha" + "\n");
			sb.append("\t" + "\t" + "beta" + "\n");
			sb.append("------------------------------ CONSOLE OUTPUT ------------------------------" + "\n");
			break;
		case 7:
			sb.append("This test is meant to prove that when we run a simulation, we can generate an experiment report "
					+ "\n");
			sb.append(" alongside our basic reports." + "\n");
			sb.append("Expected Observations: " + "\n");
			sb.append("\t" + "After all 6 scenarios are completed, the console should show 1" + "\n");
			sb.append("\t" + "value. You should observe a SUCCEEDED value of 6." + "\n");
			sb.append("\t" + "A folder named 'test7' should appear in the specified directory." + "\n");
			sb.append("\t" + "A file named 'report1.csv' should be in the 'test1' folder." + "\n");
			sb.append("\t" + "The file's data should be comma separated." + "\n");
			sb.append("\t" + "The header of the text file should have the following columns: " + "\n");
			sb.append("\t" + "\t" + "scenario" + "\n");
			sb.append("\t" + "\t" + "xxx" + "\n");
			sb.append("\t" + "\t" + "xyz" + "\n");
			sb.append("\t" + "\t" + "alpha" + "\n");
			sb.append("\t" + "\t" + "beta" + "\n");
			sb.append("\t" + "Another file named 'experiment_report' should also appear in the folder.");
			sb.append("\t" + "The file's data should be comma separated." + "\n");
			sb.append("\t" + "The header of the text file should have the following columns: " + "\n");
			sb.append("\t" + "\t" + "scenario" + "\n");
			sb.append("\t" + "\t" + "max_family_size" + "\n");
			sb.append("------------------------------ CONSOLE OUTPUT ------------------------------" + "\n");
			break;
		}

		System.out.println(sb);
	}

}