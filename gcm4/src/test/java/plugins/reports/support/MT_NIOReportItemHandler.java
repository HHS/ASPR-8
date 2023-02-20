package plugins.reports.support;

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

	private static enum Command {
		COMMENT("-c"), //
		HELP("-help"), //
		DIRECTORY("-d"), //
		TEST_1("-t1"), //
		TEST_2("-t2"), //
		TEST_3("-t3"), //
		;

		private final String commandString;

		private Command(String commandString) {
			this.commandString = commandString;
		}

		public static Command getCommand(String value) {
			for (Command command : Command.values()) {
				if (command.commandString.equals(value)) {
					return command;
				}
			}
			return null;
		}
	}

	private static void printInstructions() {
		System.out.println("Usage: ");
		System.out.println("\t" + "Any number or order of the following commands are legal:");
		System.out.println("\t" + "\t" + "-c followed by any number arguments to ignore");
		System.out.println("\t" + "\t" + "-d followed by a directory name");
		System.out.println("\t" + "\t" + "-t1 to run the first test case");
		System.out.println("\t" + "\t" + "-t2 to run the second test case");
		System.out.println("\t" + "\t" + "-t3 to run the third test case");
		System.out.println("\t" + "\t" + "-help for instructions");
		System.out.println();
		System.out.println("Example: ");
		System.out.println("\t" + "-d c:\\temp\\src\\main\\java c:\\temp\\src\\test\\java");
		System.out.println("\t" + "-t1");
		System.out.println("\t" + "-t2");
		System.out.println("\t" + "-c geo");
		System.out.println();
		System.out.println("Test Cases: ");
		System.out.println("\t" + "Test 1:");
		System.out.println("\t" + "\t" + "no progress log written");
		System.out.println("\t" + "\t" + "no progress log read");
		System.out.println("\t" + "\t" + "use experiment columns");
		System.out.println("\t" + "Test 2:");
		System.out.println("\t" + "\t" + "no progress log written");
		System.out.println("\t" + "\t" + "no progress log read");
		System.out.println("\t" + "\t" + "no experiment columns");
		System.out.println("\t" + "Test 3:");
		System.out.println("\t" + "\t" + "progress log written");
		System.out.println("\t" + "\t" + "no progress log read");
		System.out.println("\t" + "\t" + "no experiment columns");
//		System.out.println("\t" + "Test 4:");
//		System.out.println("\t" + "\t" + "progress log written");
//		System.out.println("\t" + "\t" + "progress log read");
//		System.out.println("\t" + "\t" + "use experiment columns");

	}

	private static class CommandBlock {
		private final Command command;
		private final List<String> arguments = new ArrayList<>();

		public CommandBlock(Command command) {
			this.command = command;
		}

	}

	private final Path basePath;
	
	private MT_NIOReportItemHandler(Path dirPath) {
		this.basePath = dirPath;
	}
	
	public static void main(String[] args) throws IOException {

		List<CommandBlock> commandBlocks = new ArrayList<>();

		assertNotNull(args);

		CommandBlock currentCommandBlock = null;
		for (String arg : args) {
			Command command = Command.getCommand(arg);
			if (command != null) {
				currentCommandBlock = new CommandBlock(command);
				commandBlocks.add(currentCommandBlock);
			} else {
				if (arg.startsWith("-")) {
					throw new IllegalArgumentException(arg + " is not a valid command");
				}
				if (currentCommandBlock != null) {
					currentCommandBlock.arguments.add(arg);
				} else {
					throw new IllegalArgumentException(arg + " is not a valid command");
				}
			}
		}

		boolean helpCommandPresent = false;
		boolean directorySpecified = false;
		Path basePath = null;
		List<Integer> testsToRun = new ArrayList<>();

		for (CommandBlock commandBlock : commandBlocks) {
			switch (commandBlock.command) {
				case COMMENT:
					// do nothing
					break;
				case HELP:
					helpCommandPresent = true;
					if(!commandBlock.arguments.isEmpty()) {
						throw new IllegalArgumentException(commandBlock.command.name()+" cannot accept arguments");
					}
					break;
				case DIRECTORY:
					String directoryName = commandBlock.arguments.get(0);
					basePath = Paths.get(directoryName);
					if (!basePath.toFile().exists()) {
						throw new RuntimeException("base directory does not exist");
					}
					if (!basePath.toFile().isDirectory()) {
						throw new RuntimeException("base directory is not a directory");
					}
					directorySpecified = true;
					break;
				case TEST_1:
					testsToRun.add(1);
					break;
				case TEST_2:
					testsToRun.add(2);
					break;
				case TEST_3:
					testsToRun.add(3);
					break;
				default:
					throw new RuntimeException("unknown command " + commandBlock.command);

			}
		}


		if (commandBlocks.size() == 0 || helpCommandPresent || !directorySpecified) {
			printInstructions();
			return;
		} else if (helpCommandPresent) {
			printInstructions();
		}

		new MT_NIOReportItemHandler(basePath).execute(testsToRun);
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

	private void execute(List<Integer> testsToRun) throws IOException {

		for (Integer testNumber : testsToRun) {
			switch (testNumber) {
				case 1:
					Path subPath = basePath.resolve("test1");
					createDirectory(subPath);
					test1(subPath);
					break;
				case 2:
					subPath = basePath.resolve("test2");
					createDirectory(subPath);
					test2(subPath);
					break;
				case 3:
					subPath = basePath.resolve("test3");
					createDirectory(subPath);
					test3(subPath);
					break;
				case 4:
					subPath = basePath.resolve("test4");
					createDirectory(subPath);
					test4(subPath);
					break;
				default:
					throw new RuntimeException("unknown test number: " + testNumber);
			}

		}
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

	/*
	 * write progress log
	 *
	 * read progress log
	 *
	 * write three reports
	 *
	 * expected observations :
	 *
	 *
	 */
	private void test4(Path subPath) {

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

		Experiment	.builder()//
				.addPlugin(testPlugin)//
				.addDimension(dimension1)//
				.addDimension(dimension2)//
				.addExperimentContextConsumer(nioReportItemHandler)//
				.setExperimentProgressLog(subPath.resolve("progresslog.txt"))//
				.setContinueFromProgressLog(true)//
				.addExperimentContextConsumer(experimentStatusConsole)//
				.build()//
				.execute();

	}

}