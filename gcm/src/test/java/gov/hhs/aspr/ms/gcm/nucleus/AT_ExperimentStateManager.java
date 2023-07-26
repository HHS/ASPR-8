package nucleus;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTag;
import util.annotations.UnitTestMethod;

public class AT_ExperimentStateManager {

	@Test
	@UnitTestMethod(target = ExperimentStateManager.class, name = "builder", args = {}, tags = {UnitTag.CLASS_PROXY})
	public void testBuilder() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.class, name = "closeExperiment", args = {}, tags = {UnitTag.CLASS_PROXY})
	public void testCloseExperiment() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.class, name = "closeScenarioAsFailure", args = { Integer.class, Exception.class }, tags = {UnitTag.CLASS_PROXY})
	public void testCloseScenarioAsFailure() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.class, name = "closeScenarioAsSuccess", args = { Integer.class }, tags = {UnitTag.CLASS_PROXY})
	public void testCloseScenarioAsSuccess() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.class, name = "getElapsedSeconds", args = {}, tags = {UnitTag.CLASS_PROXY})
	public void testGetElapsedSeconds() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.class, name = "getExperimentMetaData", args = {}, tags = {UnitTag.CLASS_PROXY})
	public void testGetExperimentMetaData() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.class, name = "getOutputConsumer", args = { Integer.class }, tags = {UnitTag.CLASS_PROXY})
	public void testGetOutputConsumer() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.class, name = "getScenarioCount", args = {}, tags = {UnitTag.CLASS_PROXY})
	public void testGetScenarioCount() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.class, name = "getScenarioFailureCause", args = { int.class }, tags = {UnitTag.CLASS_PROXY})
	public void testGetScenarioFailureCause() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.class, name = "getScenarioMetaData", args = { Integer.class }, tags = {UnitTag.CLASS_PROXY})
	public void testGetScenarioMetaData() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.class, name = "getScenarios", args = { ScenarioStatus.class }, tags = {UnitTag.CLASS_PROXY})
	public void testGetScenarios() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.class, name = "getScenarioStatus", args = { int.class }, tags = {UnitTag.CLASS_PROXY})
	public void testGetScenarioStatus() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.class, name = "getStatusCount", args = { ScenarioStatus.class }, tags = {UnitTag.CLASS_PROXY})
	public void testGetStatusCount() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.class, name = "openExperiment", args = {}, tags = {UnitTag.CLASS_PROXY})
	public void testOpenExperiment() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.class, name = "openScenario", args = { Integer.class, List.class }, tags = {UnitTag.CLASS_PROXY})
	public void testOpenScenario() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.Builder.class, name = "addExperimentContextConsumer", args = { Consumer.class }, tags = {UnitTag.CLASS_PROXY})
	public void testAddExperimentContextConsumer() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.Builder.class, name = "build", args = {}, tags = {UnitTag.CLASS_PROXY})
	public void testBuild() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.Builder.class, name = "setContinueFromProgressLog", args = { boolean.class }, tags = {UnitTag.CLASS_PROXY})
	public void testSetContinueFromProgressLog() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.Builder.class, name = "setExperimentMetaData", args = { List.class }, tags = {UnitTag.CLASS_PROXY})
	public void testSetExperimentMetaData() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.Builder.class, name = "setScenarioCount", args = { Integer.class }, tags = {UnitTag.CLASS_PROXY})
	public void testSetScenarioCount() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.Builder.class, name = "setScenarioProgressLogFile", args = { Path.class }, tags = {UnitTag.CLASS_PROXY})
	public void testSetScenarioProgressLogFile() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}
	
	 
	@Test
	@UnitTestMethod(target = ExperimentStateManager.Builder.class, name = "addExplicitScenarioId", args = { Integer.class }, tags = {UnitTag.CLASS_PROXY})
	public void testAddExplicitScenarioId() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */
	}
}
