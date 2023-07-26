package gov.hhs.aspr.ms.gcm.nucleus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A Dimension implementation based on Functions as level implementations.
 */

public final class FunctionalDimension implements Dimension {

	private static class Data {
		List<String> metaData = new ArrayList<>();
		List<Function<DimensionContext, List<String>>> levels = new ArrayList<>();

		public Data() {
		}

		public Data(Data data) {
			metaData.addAll(data.metaData);
			levels.addAll(data.levels);
		}
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
		public FunctionalDimension build() {
			return new FunctionalDimension(new Data(data));
		}

		/**
		 * Adds a level function to the dimension. Each such function consumes a
		 * DimensionContext of PluginDataBuilders and returns a list of
		 * scenario-level meta data that describes the changes performed on the
		 * PluginDataBuilders. The list of meta data is aligned to the
		 * experiment level meta data contained in the dimension and must
		 * contain the same number of elements.
		 */
		public Builder addLevel(Function<DimensionContext, List<String>> memberGenerator) {
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

	private FunctionalDimension(Data data) {
		this.data = data;
	}

	private final Data data;

	@Override
	public List<String> getExperimentMetaData() {
		return new ArrayList<>(data.metaData);
	}

	@Override
	public int levelCount() {
		return data.levels.size();
	}

	@Override
	public List<String> executeLevel(DimensionContext dimensionContext, int level) {
		return data.levels.get(level).apply(dimensionContext);
	}

}
