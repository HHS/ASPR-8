package gov.hhs.aspr.ms.gcm.nucleus.testsupport.runcontinuityplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.nucleus.Simulation;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestOutputConsumer;
import util.annotations.UnitTag;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_RunContinuityActor {

	@Test
	@UnitTestConstructor(target = RunContinuityActor.class, args = { RunContinuityPluginData.class }, tags = {
			UnitTag.LOCAL_PROXY })
	public void testRunContinuityPluginData() {
		// test covered by test of RunContinuityActor.accept()
	}

	@Test
	@UnitTestMethod(target = RunContinuityActor.class, name = "accept", args = { ActorContext.class })
	public void testAccept() {

		Set<Double> expectedOutput = new LinkedHashSet<>();
		expectedOutput.add(0.0);
		expectedOutput.add(1.0);
		expectedOutput.add(4.5);
		expectedOutput.add(7.23);
		expectedOutput.add(90.60);
		expectedOutput.add(100.0);

		RunContinuityPluginData.Builder builder = RunContinuityPluginData.builder();//
		for (Double time : expectedOutput) {
			builder.addContextConsumer(time, (c) -> c.releaseOutput(c.getTime()));
		}
		RunContinuityPluginData runContinuityPluginData = builder.build();

		Plugin plugin = RunContinuityPlugin.builder()//
				.setRunContinuityPluginData(runContinuityPluginData)//
				.build();

		TestOutputConsumer outputConsumer = new TestOutputConsumer();
		Simulation.builder()//
				.addPlugin(plugin)//
				.setOutputConsumer(outputConsumer)//
				.build()//
				.execute();

		runContinuityPluginData = outputConsumer.getOutputItem(RunContinuityPluginData.class).get();
		assertTrue(runContinuityPluginData.allPlansComplete());
		
		
		Set<Double> actualOutput = new LinkedHashSet<>(outputConsumer.getOutputItems(Double.class));

		assertEquals(expectedOutput, actualOutput);

	}
}
