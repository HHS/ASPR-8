package plugins.gcm.support.experimentspace;

import java.util.ArrayList;
import java.util.List;

import plugins.gcm.input.Scenario;
import util.MultiKey;

/**
 * 
 * A support class that represents the values associated with a variable used to
 * define an experiment space such as a compartment property. It also contains a
 * value extractor to help extract the corresponding value from a scenario.
 * 
 * @author Shawn Hatch
 */
public final class ExperimentTestVariable {

	public static class ExperimentTestVariableBuilder {

		private ExperimentTestVariable experimentTestVariable = new ExperimentTestVariable();

		public void setValueExtractor(ValueExtractor valueExtractor) {
			experimentTestVariable.valueExtractor = valueExtractor;
		}

		public void addValue(Object value) {
			experimentTestVariable.values.add(value);
		}

		/**
		 * Builds the experiment variable
		 * 
		 * @throws RuntimeException
		 *             <li>if the variable contains no values
		 *             <li>if the variable contains no value extractor
		 */
		public ExperimentTestVariable build() {
			try {
				if (experimentTestVariable.values.size() == 0) {
					throw new RuntimeException("no values");
				}
				if (experimentTestVariable.valueExtractor == null) {
					throw new RuntimeException("no value extractor");
				}
				return experimentTestVariable;
			} finally {
				experimentTestVariable = new ExperimentTestVariable();
			}
		}

	}

	int size() {
		return values.size();
	}

	private ValueExtractor valueExtractor;

	private List<Object> values = new ArrayList<>();

	private ExperimentTestVariable() {

	}

	Object getValue(int index) {
		return values.get(index);
	}

	private static MultiKey getAppendedMultiKey(MultiKey multiKey, Object value) {
		MultiKey.Builder multiKeyBuilder = MultiKey.builder();
		for (Object key : multiKey.getKeys()) {
			multiKeyBuilder.addKey(key);
		}
		multiKeyBuilder.addKey(value);

		return multiKeyBuilder.build();
	}

	MultiKey getActualMultiKey(MultiKey baseKey, Scenario scenario) {
		MultiKey.Builder multiKeyBuilder = MultiKey.builder();
		for (Object key : baseKey.getKeys()) {
			multiKeyBuilder.addKey(key);
		}
		multiKeyBuilder.addKey(valueExtractor.extractValue(scenario));
		return multiKeyBuilder.build();
	}

	List<MultiKey> buildExpectedMultiKeys(List<MultiKey> list) {
		List<MultiKey> result = new ArrayList<>();
		for (Object expectedValue : values) {
			for (MultiKey multiKey : list) {
				result.add(getAppendedMultiKey(multiKey, expectedValue));
			}
		}
		return result;
	}
}