package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import java.util.ArrayList;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestActorPlan;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestPlugin;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestPluginData;

/**
 * Manual experiment test focusing on the generation and handling of exceptions.
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
		FunctionalDimension.Builder builder = FunctionalDimension.builder();
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
		
		ExperimentParameterData experimentParameterData = ExperimentParameterData.builder()//
				.setHaltOnException(false)//
				.setThreadCount(10)//
				.build();

		Experiment	.builder()//
					.addPlugin(testPlugin)//
					.addDimension(getDimension(100))//					
					.addExperimentContextConsumer(experimentStatusConsole)//
					.setExperimentParameterData(experimentParameterData)//					
					.build()//
					.execute();//
	}

}
