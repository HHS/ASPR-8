package plugins.reports.support;

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
		sb.append("\t" + "Any number or order of the following commands are legal:" + "\n");
		sb.append("\t" + "\t" + "-c followed by any number arguments to ignore" + "\n");
		sb.append("\t" + "\t" + "-d followed by a directory name" + "\n");
		sb.append("\t" + "\t" + "-t followed by a test case number" + "\n");
		sb.append("\t" + "\t" + "-help for instructions" + "\n");
		sb.append("Example: " + "\n");
		sb.append("\t" + "-d c:\\temp\\src\\main\\java c:\\temp\\src\\test\\java" + "\n");
		sb.append("\t" + "-t 1" + "\n");
		sb.append("\t" + "-c testing" + "\n");
		sb.append("Test Cases: " + "\n");
		sb.append("\t" + "Test 1:" + "\n");
		sb.append("\t" + "\t" + "no progress log written" + "\n");
		sb.append("\t" + "\t" + "no progress log read" + "\n");
		sb.append("\t" + "\t" + "use experiment columns" + "\n");
		sb.append("\t" + "Test 2:" + "\n");
		sb.append("\t" + "\t" + "no progress log written" + "\n");
		sb.append("\t" + "\t" + "no progress log read" + "\n");
		sb.append("\t" + "\t" + "no experiment columns" + "\n");
		sb.append("\t" + "Test 3:" + "\n");
		sb.append("\t" + "\t" + "progress log written" + "\n");
		sb.append("\t" + "\t" + "no progress log read" + "\n");
		sb.append("\t" + "\t" + "no experiment columns" + "\n");
		sb.append("\t" + "Test 4:" + "\n");
		sb.append("\t" + "\t" + "progress log written" + "\n");
		sb.append("\t" + "\t" + "progress log read" + "\n");
		sb.append("\t" + "\t" + "use experiment columns" + "\n");
		sb.append("\t" + "Test 5:" + "\n");
		sb.append("\t" + "\t" + "no progress log written" + "\n");
		sb.append("\t" + "\t" + "progress log read" + "\n");
		sb.append("\t" + "\t" + "use experiment columns" + "\n");

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

		Map<Command,List<CommandBlock>> commandBlocks = new LinkedHashMap<>();
		for(Command command : Command.values()) {
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
		
		//HELP
		int directoryCount = commandBlocks.get(Command.DIRECTORY).size();
		int testCount = commandBlocks.get(Command.TEST).size();
		
		if(directoryCount==0 && testCount==0) {
			printInstructions();
			return;
		}
		
		List<CommandBlock> blocks = commandBlocks.get(Command.HELP);
		if(!blocks.isEmpty()) {
			printInstructions();
			return;
		}
		
		//DIRECTORY
		blocks = commandBlocks.get(Command.DIRECTORY);
		if(blocks.isEmpty()) {
			throw new RuntimeException("requires a directory");
		}
		
		if(blocks.size()>1) {
			throw new RuntimeException("too many directories listed");
		}
		
		CommandBlock commandBlock = blocks.get(0);
		if(commandBlock.arguments.isEmpty()) {
			throw new RuntimeException("requires a directory");
		}
		
		if(commandBlock.arguments.size()>1) {
			throw new RuntimeException("too many directories listed");
		}		
		
		String directoryName = commandBlock.arguments.get(0);
		basePath = Paths.get(directoryName);
		if (!basePath.toFile().exists()) {
			throw new RuntimeException("base directory does not exist");
		}
		if (!basePath.toFile().isDirectory()) {
			throw new RuntimeException("base directory is not a directory");
		}
		
		//TEST
		
		blocks = commandBlocks.get(Command.TEST);
		if(blocks.isEmpty()) {
			throw new RuntimeException("requires a test command");
		}
		
		if(blocks.size()>1) {
			throw new RuntimeException("too many test commands");
		}
		
		commandBlock = blocks.get(0);
		if(commandBlock.arguments.isEmpty()) {
			throw new RuntimeException("requires exactly one test number");
		}
		
		if(commandBlock.arguments.size()>1) {
			throw new RuntimeException("requires exactly one test number");
		}		
		try {
			testIndex = Integer.parseInt(commandBlock.arguments.get(0));			
		} catch (NumberFormatException e) {
			throw new RuntimeException("test index needs to be a integer", e);
		}
		
		if(testIndex<1||testIndex>5) {
			throw new RuntimeException("test index out of bounds");
		}
		
		//UNKNOWN
		blocks = commandBlocks.get(Command.UNKNOWN);
		if(blocks.size()>1) {			
			//stringbuiler up the command strings for the unknown commands
			throw new RuntimeException("encounted an unknown command");
		}else {
			commandBlock = blocks.get(0);
			if(!commandBlock.arguments.isEmpty()) {
				throw new RuntimeException("encounted an unknown command");
			}
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
					.setContinueFromProgressLog(true)//
					.addExperimentContextConsumer(experimentStatusConsole)//
					.build()//
					.execute();

	}

	/*
	 * write progress log
	 *
	 * no progress log read
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
					.setContinueFromProgressLog(true)//
					.addExperimentContextConsumer(experimentStatusConsole)//
					.build()//
					.execute();

	}

	private void printExpected(Integer testNum) {

		StringBuilder sb = new StringBuilder();

		switch (testNum) {
		case 1:
			sb.append("expected observations: " + "\n");
			sb.append("\t" + "a folder named 'test1' should appear in the specified directory" + "\n");
			sb.append("\t" + "a file named 'report1.txt' should be in the folder" + "\n");
			sb.append("\t" + "the header of the text file should have the following columns: " + "\n");
			sb.append("\t" + "\t" + "scenario" + "\n");
			sb.append("\t" + "\t" + "xxx" + "\n");
			sb.append("\t" + "\t" + "xyz" + "\n");
			sb.append("\t" + "\t" + "alpha" + "\n");
			sb.append("\t" + "\t" + "beta" + "\n");
			break;
		case 2:
			sb.append("expected observations: " + "\n");
			sb.append("\t" + "a folder named 'test2' should appear in the specified directory" + "\n");
			sb.append("\t" + "a file named 'report1.txt' should be in the folder" + "\n");
			sb.append("\t" + "the header of the text file should have the following columns: " + "\n");
			sb.append("\t" + "\t" + "scenario" + "\n");
			sb.append("\t" + "\t" + "alpha" + "\n");
			sb.append("\t" + "\t" + "beta" + "\n");
			break;
		case 3:
			sb.append("expected observations: " + "\n");
			sb.append("\t" + "a folder named 'test3' should appear in the specified directory" + "\n");
			sb.append("\t" + "a file named 'report1.txt' should be in the folder" + "\n");
			sb.append("\t" + "the header of the text file should have the following columns: " + "\n");
			sb.append("\t" + "\t" + "scenario" + "\n");
			sb.append("\t" + "\t" + "xxx" + "\n");
			sb.append("\t" + "\t" + "xyz" + "\n");
			sb.append("\t" + "\t" + "alpha" + "\n");
			sb.append("\t" + "\t" + "beta" + "\n");
			sb.append("\t" + "another file named 'progresslog.txt' should be in the folder" + "\n");
			sb.append("\t" + "the header of the text file should have the following columns: " + "\n");
			sb.append("\t" + "\t" + "scenario" + "\n");
			sb.append("\t" + "\t" + "xxx" + "\n");
			sb.append("\t" + "\t" + "xyz" + "\n");
			break;
		case 4:
			sb.append("expected observations: " + "\n");
			sb.append("\t" + "after all 6 scenarios are completed, the compiler should show 2" + "\n");
			sb.append("\t" + "values. You should have PREVIOUSLY_SUCCEEDED and SUCCEEDED values" + "\n");
			sb.append("\t" + "whose sum should total up to 6." + "\n");
			sb.append("\t" + "a folder named 'test4' should appear in the specified directory" + "\n");
			sb.append("\t" + "a file named 'report1.txt' should be in the folder" + "\n");
			sb.append("\t" + "the header of the text file should have the following columns: " + "\n");
			sb.append("\t" + "\t" + "scenario" + "\n");
			sb.append("\t" + "\t" + "xxx" + "\n");
			sb.append("\t" + "\t" + "xyz" + "\n");
			sb.append("\t" + "\t" + "alpha" + "\n");
			sb.append("\t" + "\t" + "beta" + "\n");
			sb.append("\t" + "another file named 'progresslog.txt' should be in the folder" + "\n");
			sb.append("\t" + "the header of the text file should have the following columns: " + "\n");
			sb.append("\t" + "\t" + "scenario" + "\n");
			sb.append("\t" + "\t" + "xxx" + "\n");
			sb.append("\t" + "\t" + "xyz" + "\n");
			break;
		case 5:
			sb.append("expected observations: " + "\n");
			sb.append("\t" + "after running test 5, you should recieve an exception with the following message:" + "\n");
			sb.append("\t" + "Exception in thread \"main\" util.errors.ContractException: The scenario progress file does not exist," + "\n");
			sb.append("\t" + "but is required when continuation from progress file is chosen" + "\n");
			break;
		}

		System.out.println(sb);
	}

}