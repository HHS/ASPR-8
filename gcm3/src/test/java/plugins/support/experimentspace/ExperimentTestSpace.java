package plugins.support.experimentspace;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import plugins.gcm.input.Scenario;
import plugins.support.experimentspace.ExperimentTestDimension.ExperimentTestDimensionBuilder;
import util.MultiKey;

/**
 * A utility class that represents an experiment space and is used to compare an
 * expected experiment space configuration to the actual experiment space
 * configuration supplied by a list of scenarios.
 * 
 * @author Shawn Hatch
 *
 */
public final class ExperimentTestSpace {
	public static class ExperimentTestSpaceBuilder {
		private ExperimentTestSpace experimentSpaceTest = new ExperimentTestSpace();

		/**
		 * Adds a dimension -- each dimension will have its own associated
		 * Experiment Test Variables
		 */
		public void addExperimentDimension(ExperimentTestDimension experimentTestDimension) {
			experimentSpaceTest.experimentTestDimensions.add(experimentTestDimension);
		}

		/**
		 * Adds an Experiment Test Variable that is not aligned to a dimension.
		 */
		public void addExperimentVariable(ExperimentTestVariable experimentTestVariable) {
			ExperimentTestDimensionBuilder experimentTestDimensionBuilder = new ExperimentTestDimensionBuilder();
			experimentTestDimensionBuilder.addExperimentVariable(experimentTestVariable);
			experimentSpaceTest.experimentTestDimensions.add(experimentTestDimensionBuilder.build());
		}

		/**
		 * Builds the experiment space
		 * 
		 * @throws RuntimeException
		 *             if the experiment contains no dimensions
		 * 
		 */
		public ExperimentTestSpace build() {
			try {
				if (experimentSpaceTest.experimentTestDimensions.size() == 0) {
					throw new RuntimeException("Empty experiment space");
				}
				return experimentSpaceTest;
			} finally {
				experimentSpaceTest = new ExperimentTestSpace();
			}
		}

	}

	private ExperimentTestSpace() {
	}

	private List<ExperimentTestDimension> experimentTestDimensions = new ArrayList<>();

	private Set<MultiKey> getExpectedMultiKeys() {
		Set<MultiKey> result = new LinkedHashSet<>();
		result.add(new MultiKey());
		for (ExperimentTestDimension experimentTestDimension : experimentTestDimensions) {
			result = experimentTestDimension.getExpectedMultiKeys(result);
		}
		return result;
	}

	private MultiKey getActualMultiKey(Scenario scenario) {

		MultiKey result = new MultiKey();
		for (ExperimentTestDimension experimentTestDimension : experimentTestDimensions) {
			result = experimentTestDimension.getActualMultiKey(result, scenario);
		}
		return result;
	}

	private Set<MultiKey> getActualMultiKeys(List<Scenario> scenarios) {
		Set<MultiKey> result = new LinkedHashSet<>();
		for (Scenario scenario : scenarios) {
			result.add(getActualMultiKey(scenario));
		}
		return result;
	}

	/**
	 * Tests that the given scenarios create the same experiment space as the
	 * inputs to this ExperimentSpaceTester
	 */
	public void assertEqualSpaces(List<Scenario> scenarios) {
		/*
		 * We will represent the points in the experiment space with MultiKey
		 * objects
		 */
		Set<MultiKey> expectedMultiKeys = getExpectedMultiKeys();
		Set<MultiKey> actualMultiKeys = getActualMultiKeys(scenarios);

		/*
		 * We show that the expected space and the actual space are the same
		 */
		assertEquals(expectedMultiKeys, actualMultiKeys);

		/*
		 * We form the actual space by using the same dimensions found in the
		 * expected space. If the number of scenarios does not match the size of
		 * the expected space, then we know that (since we passed the assertion
		 * above) the scenario is actually using a larger space with other
		 * dimensions.
		 */
		assertEquals(scenarios.size(), actualMultiKeys.size(), "Number of scenarios does not match size of space");

	}

}