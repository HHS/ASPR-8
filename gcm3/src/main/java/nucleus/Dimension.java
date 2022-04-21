package nucleus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import nucleus.util.TypeMap;

/**
 * A Dimension represents a single independent dimension of an experiment.
 * Dimensions are composed of a finite number of levels. Each level in a
 * dimension is a function that 1) consumes a type map of plugin data builders,
 * 2) updates those builders to create alternate inputs for each scenario and 3)
 * returns a list of strings representing the variant values in the scenario.
 * 
 * @author Shawn Hatch
 *
 */

public final class Dimension {

	private static class Data {
		List<String> metaData = new ArrayList<>();
		List<Function<TypeMap<PluginDataBuilder>, List<String>>> levels = new ArrayList<>();
	}

	/**
	 * Returns a builder class for Dimension
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * A builder class for Dimension
	 */

	public static class Builder {

		private Data data = new Data();

		private Builder() {
		}

		/**
		 * Returns a Dimension from the collected data
		 */
		public Dimension build() {
			try {
				return new Dimension(data);
			} finally {
				data = new Data();
			}
		}

		/**
		 * Adds a level function to the dimension. Each such function consumes a
		 * TypeMap of PluginDataBuilders and returns a list of scenario-level
		 * meta data that describes the changes performed on the
		 * PluginDataBuilders. The list of meta data is aligned to the
		 * experiment level meta data contained in the dimension and must
		 * contain the same number of elements.
		 */
		public Builder addLevel(Function<TypeMap<PluginDataBuilder>, List<String>> memberGenerator) {
			data.levels.add(memberGenerator);
			return this;
		}

		/**
		 * Adds an experiment-level string meta datum value that describes the
		 * corresponding scenario-level meta data returned by the individual
		 * levels of this dimension.
		 */
		public Builder addMetaDatum(String idValue) {
			data.metaData.add(idValue);
			return this;
		}
	}

	private Dimension(Data data) {
		this.data = data;
	}

	private final Data data;

	/**
	 * Returns the meta data for the experiment that describes the
	 * scenario-level meta data.
	 */
	public List<String> getMetaData() {
		return new ArrayList<>(data.metaData);
	}

	/**
	 * Returns the number of levels in this dimension
	 */
	public int size() {
		return data.levels.size();
	}

	/**
	 * Returns the function(level) for the given index.  Valid indexes are zero through size()-1 inclusive.
	 */
	public Function<TypeMap<PluginDataBuilder>, List<String>> getLevel(int index) {
		return data.levels.get(index);
	}

}
