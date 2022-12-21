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

	//@Test
	public void test() {
		main(new String[] {});
	}

	private MT_Experiment() {

	}

	public static void main(String[] args) {
		new MT_Experiment().excecute();
	}

	private int counter;

	private final Object LOCK = new Object();

	private Dimension getDimension(final int dimSize) {
		Dimension.Builder builder = Dimension.builder();
		for (int i = 0; i < dimSize; i++) {
			builder.addLevel((c) -> new ArrayList<>());
		}
		return builder.build();
	}

	private void excecute() {
		
		// use the test plugin to generate an agent
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// have one of the six actors throw an exception
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			synchronized (LOCK) {
				counter++;				
				if (counter % 5 == 1 ) {
					throw new RuntimeException("test exception");
				}
			}
		}));

		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);


		ExperimentStatusConsole experimentStatusConsole = //
				ExperimentStatusConsole	.builder()//
										.setImmediateErrorReporting(true)//
										.setReportScenarioProgress(false)//
										.setStackTraceReportLimit(3)//
										.build();//

		Experiment	.builder()//
					.addPlugin(testPlugin)//
					.addDimension(getDimension(100))//					
					.addExperimentContextConsumer(experimentStatusConsole)//
					.setHaltOnException(true)//
					.setThreadCount(0)//
					.build()//
					.execute();//
	}

}
