package plugins.gcm.support.experimentspace;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import plugins.gcm.input.Scenario;
import util.MultiKey;

/**
 * 
 * A support class that represents one or more joined covariant experiment
 * variables.
 * 
 * 
 * @author Shawn Hatch
 */
public final class ExperimentTestDimension {
	public static class ExperimentTestDimensionBuilder {
		private ExperimentTestDimension experimentTestDimension = new ExperimentTestDimension();

		public void addExperimentVariable(ExperimentTestVariable experimentTestVariable) {
			experimentTestDimension.experimentTestVariables.add(experimentTestVariable);
		}

		/**
		 * Builds the experiment dimension.
		 * 
		 * @throws RuntimeException
		 *             <li>if the dimension contains no variables
		 *             <li>if the dimension variables that have different
		 *             numbers of values
		 *             <li>if the dimension contains only empty variables
		 */
		public ExperimentTestDimension build() {
			try {
				if (experimentTestDimension.experimentTestVariables.size() == 0) {
					throw new RuntimeException("dimension contains no variables");
				}
				experimentTestDimension.size = -1;
				for (ExperimentTestVariable experimentTestVariable : experimentTestDimension.experimentTestVariables) {
					if (experimentTestDimension.size < 0) {
						experimentTestDimension.size = experimentTestVariable.size();
					} else {
						if (experimentTestDimension.size != experimentTestVariable.size()) {
							throw new RuntimeException("size of variable inconsistent with dimension");
						}
					}
				}
				if (experimentTestDimension.size < 1) {
					throw new RuntimeException("no values ");
				}
				return experimentTestDimension;
			} finally {
				experimentTestDimension = new ExperimentTestDimension();
			}
		}
	}

	private ExperimentTestDimension() {
	}

	private int size;

	private List<ExperimentTestVariable> experimentTestVariables = new ArrayList<>();

	List<ExperimentTestVariable> getExperimentVariables() {
		return new ArrayList<>(experimentTestVariables);
	}

	Set<MultiKey> getExpectedMultiKeys(Set<MultiKey> baseKeys) {
		Set<MultiKey> result = new LinkedHashSet<>();
		for (int i = 0; i < size; i++) {
			List<Object> values = new ArrayList<>();
			for (ExperimentTestVariable experimentTestVariable : experimentTestVariables) {
				Object value = experimentTestVariable.getValue(i);
				values.add(value);
			}
			MultiKey.Builder multiKeyBuilder = MultiKey.builder();
			for (MultiKey baseKey : baseKeys) {
				for (Object key : baseKey.getKeys()) {
					multiKeyBuilder.addKey(key);
				}
				for (Object value : values) {
					multiKeyBuilder.addKey(value);
				}
				MultiKey appendedBaseKey = multiKeyBuilder.build();
				result.add(appendedBaseKey);
			}
		}
		return result;
	}

	MultiKey getActualMultiKey(MultiKey baseKey, Scenario scenario) {
		MultiKey result = baseKey;
		for (ExperimentTestVariable experimentTestVariable : experimentTestVariables) {
			result = experimentTestVariable.getActualMultiKey(result, scenario);
		}
		return result;
	}

}