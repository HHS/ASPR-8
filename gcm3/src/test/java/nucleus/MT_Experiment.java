package nucleus;

import java.util.ArrayList;

import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;

/**
 * Manual experiment test focusing on the generation and handling of exceptions.
 * 
 * @author Shawn Hatch
 *
 */
public class MT_Experiment {

	private MT_Experiment() {

	}

	public static void main(String[] args) {
		excecute();

	}

	private static int counter;

	private static synchronized int incrementCounter() {
		counter++;
		return counter;
	}

	private static void excecute() {

		// MutableInteger scenarioId = new MutableInteger(-1);

		// add two dimension to create six scenarios
		Dimension dimension1 = Dimension.builder()//
										.addLevel((c) -> {
											return new ArrayList<>();//
										}).addLevel((c) -> {
											return new ArrayList<>();//
										}).build();

		Dimension dimension2 = Dimension.builder()//
										.addLevel((c) -> {
											return new ArrayList<>();
										}).addLevel((c) -> {
											return new ArrayList<>();
										}).addLevel((c) -> {
											return new ArrayList<>();
										}).build();

		// use the test plugin to generate an agent
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// have one of the six actors throw an exception
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			int index = incrementCounter();
			if (index == 3) {
				throw new RuntimeException("test exception");
			}
		}));

		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// build and execute the experiment
		Experiment	.builder()//
					.addPlugin(testPlugin)//
					.addDimension(dimension1)//
					.addDimension(dimension2)//
					.reportProgressToConsole(false)//
					.reportFailuresToConsole(false)//
					.setThreadCount(4)//
					.build()//
					.execute();

	}

}
