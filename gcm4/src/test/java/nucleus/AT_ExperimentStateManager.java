package nucleus;


import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;

@UnitTest(target = ExperimentStateManager.class)
public class AT_ExperimentStateManager {

	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}
	
	@Test
	@UnitTestMethod(name = "closeExperiment", args = {})
	public void testCloseExperiment() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}

	@Test
	@UnitTestMethod(name = "closeScenarioAsFailure", args = {Integer.class, Exception.class})
	public void testCloseScenarioAsFailure() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}

	@Test
	@UnitTestMethod(name = "closeScenarioAsSuccess", args = {Integer.class})
	public void testCloseScenarioAsSuccess() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}
	
	@Test
	@UnitTestMethod(name = "getElapsedSeconds", args = {})
	public void testGetElapsedSeconds() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}

	@Test
	@UnitTestMethod(name = "getExperimentMetaData", args = {})
	public void testGetExperimentMetaData() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}
	
	@Test
	@UnitTestMethod(name = "getOutputConsumer", args = {Integer.class})
	public void testGetOutputConsumer() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}
	
	@Test
	@UnitTestMethod(name = "getScenarioCount", args = {})
	public void testGetScenarioCount() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}

	@Test
	@UnitTestMethod(name = "getScenarioFailureCause", args = {int.class})
	public void testGetScenarioFailureCause() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}

	@Test
	@UnitTestMethod(name = "getScenarioMetaData", args = {Integer.class})
	public void testGetScenarioMetaData() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}

	@Test
	@UnitTestMethod(name = "getScenarios", args = {ScenarioStatus.class})
	public void testGetScenarios() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}

	@Test
	@UnitTestMethod(name = "getScenarioStatus", args = {int.class})
	public void testGetScenarioStatus() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}
	
	@Test
	@UnitTestMethod(name = "getStatusCount", args = {ScenarioStatus.class})
	public void testGetStatusCount() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}

	@Test
	@UnitTestMethod(name = "openExperiment", args = {})
	public void testOpenExperiment() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}
	
	

	@Test
	@UnitTestMethod(name = "openScenario", args = {Integer.class, List.class})
	public void testOpenScenario() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.Builder.class ,name ="addExperimentContextConsumer", args = {Consumer.class})
	public void testAddExperimentContextConsumer() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.Builder.class ,name ="build", args = {})
	public void testBuild() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}
	
	@Test
	@UnitTestMethod(target = ExperimentStateManager.Builder.class ,name ="setContinueFromProgressLog", args = {boolean.class})
	public void testSetContinueFromProgressLog() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}


	@Test
	@UnitTestMethod(target = ExperimentStateManager.Builder.class ,name ="setExperimentMetaData", args = {List.class})
	public void testSetExperimentMetaData() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.Builder.class ,name ="setScenarioCount", args = {Integer.class})
	public void testSetScenarioCount() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.Builder.class ,name ="setScenarioProgressLogFile", args = {Path.class})
	public void testSetScenarioProgressLogFile() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}


}
