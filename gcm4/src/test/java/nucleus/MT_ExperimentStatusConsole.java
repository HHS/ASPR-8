package nucleus;

import java.util.ArrayList;

import tools.annotations.UnitTest;

@UnitTest(target = ExperimentStatusConsole.class)
public class MT_ExperimentStatusConsole {
	private MT_ExperimentStatusConsole() {
	}

	private Dimension getDimension(int size) {
		Dimension.Builder builder = Dimension.builder();
		for (int i = 0; i < size; i++) {
			builder.addLevel((c) -> new ArrayList<>());
		}
		return builder.build();
	}

	private void execute() {
		// uncomment each test method individually to perform the tests

		/*
		 * tests for reporting scenario progress
		 */

		// testSetReportScenarioProgressDefault();
		// testSetReportScenarioProgressOn();
		// testSetReportScenarioProgressOff();

		/*
		 * tests for immediate error reporting
		 */

		// testSetImmediateErrorReporting_Default();
		// testSetImmediateErrorReporting_False();
		// testSetImmediateErrorReporting_True();

		/*
		 * tests for stack trace report limit
		 */
		//testSetStackTraceReportLimit_Default();
		//testSetStackTraceReportLimit_5();
		//testSetStackTraceReportLimit_0();

	}

	@SuppressWarnings("unused")
	private void testSetReportScenarioProgress_Default() {
		ExperimentStatusConsole experimentStatusConsole = //
				ExperimentStatusConsole//
										.builder()//
										.build();

		Experiment	.builder()//
					.addExperimentContextConsumer(experimentStatusConsole)//
					.addDimension(getDimension(5))//
					.build()//
					.execute();//

		/*
		 * Observe: the five scenarios should be reported in the console.
		 * 
		 */

	}

	@SuppressWarnings("unused")
	private void testSetReportScenarioProgress_On() {
		ExperimentStatusConsole experimentStatusConsole = //
				ExperimentStatusConsole//
										.builder()//
										.setReportScenarioProgress(true)//
										.build();

		Experiment	.builder()//
					.addExperimentContextConsumer(experimentStatusConsole)//
					.addDimension(getDimension(5))//
					.build()//
					.execute();//
		/*
		 * Observe: the five scenarios should be reported in the console.
		 * 
		 */

	}

	@SuppressWarnings("unused")
	private void testSetReportScenarioProgress_Off() {
		ExperimentStatusConsole experimentStatusConsole = //
				ExperimentStatusConsole//
										.builder()//
										.setReportScenarioProgress(false)//
										.build();

		Experiment	.builder()//
					.addExperimentContextConsumer(experimentStatusConsole)//
					.addDimension(getDimension(5))//
					.build()//
					.execute();//

		/*
		 * Observe: the five scenarios are not reported in the console. Only the
		 * summary and exit of the console are printed.
		 * 
		 */
	}

	@SuppressWarnings("unused")
	private void testSetImmediateErrorReporting_Default() {
		ExperimentStatusConsole experimentStatusConsole = //
				ExperimentStatusConsole//
										.builder()//
										.build();

		Plugin plugin = Plugin	.builder()//
								.setPluginId(new SimplePluginId("plugin"))//
								.setInitializer((c) -> {
									c.addActor((c2) -> {
										throw new RuntimeException();
									});
								}).build();

		Experiment	.builder()//
					.addExperimentContextConsumer(experimentStatusConsole)//
					.addDimension(getDimension(1000))//
					.addPlugin(plugin)//
					.build()//
					.execute();//

		/*
		 * Observe: The errors are not reported during the run, but are reported
		 * as part of the summary.
		 * 
		 */
	}

	@SuppressWarnings("unused")
	private void testSetImmediateErrorReporting_False() {
		ExperimentStatusConsole experimentStatusConsole = //
				ExperimentStatusConsole//
										.builder()//
										.setImmediateErrorReporting(false)//
										.build();

		Plugin plugin = Plugin	.builder()//
								.setPluginId(new SimplePluginId("plugin"))//
								.setInitializer((c) -> {
									c.addActor((c2) -> {
										throw new RuntimeException();
									});
								}).build();

		Experiment	.builder()//
					.addExperimentContextConsumer(experimentStatusConsole)//
					.addDimension(getDimension(1000))//
					.addPlugin(plugin)//
					.build()//
					.execute();//

		/*
		 * Observe: The errors are not reported during the run, but are reported
		 * as part of the summary.
		 * 
		 */
	}

	@SuppressWarnings("unused")
	private void testSetImmediateErrorReporting_True() {
		ExperimentStatusConsole experimentStatusConsole = //
				ExperimentStatusConsole//
										.builder()//
										.setImmediateErrorReporting(true)//
										.build();

		Plugin plugin = Plugin	.builder()//
								.setPluginId(new SimplePluginId("plugin"))//
								.setInitializer((c) -> {
									c.addActor((c2) -> {
										throw new RuntimeException();
									});
								}).build();

		Experiment	.builder()//
					.addExperimentContextConsumer(experimentStatusConsole)//
					.addDimension(getDimension(1000))//
					.addPlugin(plugin)//
					.build()//
					.execute();//

		/*
		 * Observe: The errors are reported during the run as well as part of
		 * the summary.
		 * 
		 */
	}

	@SuppressWarnings("unused")
	private void testSetStackTraceReportLimit_Default() {
		ExperimentStatusConsole experimentStatusConsole = //
				ExperimentStatusConsole//
										.builder()//
										.setImmediateErrorReporting(true)//
										.build();

		Plugin plugin = Plugin	.builder()//
								.setPluginId(new SimplePluginId("plugin"))//
								.setInitializer((c) -> {
									c.addActor((c2) -> {
										throw new RuntimeException();
									});
								}).build();

		Experiment	.builder()//
					.addExperimentContextConsumer(experimentStatusConsole)//
					.addDimension(getDimension(1000))//
					.addPlugin(plugin)//
					.build()//
					.execute();//

		/*
		 * Observe: There are 1000 exceptions but reporting is limited to 100
		 * stack traces. The are 100 stack traces reported immediately. There
		 * are 100 stack traces reported in the summary.
		 */
	}
	
	@SuppressWarnings("unused")
	private void testSetStackTraceReportLimit_5() {
		ExperimentStatusConsole experimentStatusConsole = //
				ExperimentStatusConsole//
										.builder()//
										.setImmediateErrorReporting(true)//
										.setStackTraceReportLimit(5)//
										.build();

		Plugin plugin = Plugin	.builder()//
								.setPluginId(new SimplePluginId("plugin"))//
								.setInitializer((c) -> {
									c.addActor((c2) -> {
										throw new RuntimeException();
									});
								}).build();

		Experiment	.builder()//
					.addExperimentContextConsumer(experimentStatusConsole)//
					.addDimension(getDimension(1000))//
					.addPlugin(plugin)//
					.build()//
					.execute();//

		/*
		 * Observe: There are 1000 exceptions but reporting is limited to 5
		 * stack traces. The are 5 stack traces reported immediately. There
		 * are 5 stack traces reported in the summary.
		 */
	}
	
	@SuppressWarnings("unused")
	private void testSetStackTraceReportLimit_0() {
		ExperimentStatusConsole experimentStatusConsole = //
				ExperimentStatusConsole//
										.builder()//
										.setImmediateErrorReporting(true)//
										.setStackTraceReportLimit(0)//
										.build();

		Plugin plugin = Plugin	.builder()//
								.setPluginId(new SimplePluginId("plugin"))//
								.setInitializer((c) -> {
									c.addActor((c2) -> {
										throw new RuntimeException();
									});
								}).build();

		Experiment	.builder()//
					.addExperimentContextConsumer(experimentStatusConsole)//
					.addDimension(getDimension(1000))//
					.addPlugin(plugin)//
					.build()//
					.execute();//

		/*
		 * Observe: There are 1000 exceptions but reporting is limited to 0
		 * stack traces. The are no stack traces reported.
		 */
	}

	public static void main(String[] args) {
		// go to execute() method for instructions
		new MT_ExperimentStatusConsole().execute();
	}

}
