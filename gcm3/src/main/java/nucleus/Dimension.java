package nucleus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import nucleus.util.TypeMap;

/**
 * A Dimension represents a single independent dimension of an experiment.
 * Dimensions are composed of a finite number of points. Each point in a
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
		List<Function<TypeMap<PluginDataBuilder>, List<String>>> points = new ArrayList<>();
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
		 * Adds a point function to the dimension. Each such function consumes a
		 * TypeMap of PluginDataBuilders and returns a list of scenario-level
		 * meta data that describes the changes performed on the
		 * PluginDataBuilders. The list of meta data is aligned to the
		 * experiment level meta data contained in the dimension and must
		 * contain the same number of elements.
		 */
		public Builder addPoint(Function<TypeMap<PluginDataBuilder>, List<String>> memberGenerator) {
			data.points.add(memberGenerator);
			return this;
		}

		/**
		 * Adds an experiment-level string meta datum value that describes the
		 * corresponding scenario-level meta data returned by the individual
		 * points of this dimension.
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
	 * Returns the number of points in this dimension
	 */
	public int size() {
		return data.points.size();
	}

	/**
	 * Returns the function(point) for the given index.  Valid indexes are zero through size()-1 inclusive.
	 */
	public Function<TypeMap<PluginDataBuilder>, List<String>> getPoint(int index) {
		return data.points.get(index);
	}

}
