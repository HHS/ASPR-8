package temp.filtereventtests.plugins.model;

import nucleus.PluginData;
import nucleus.PluginDataBuilder;

public class ModelPluginData implements PluginData {

	/**
	 * Builder class for GloblaInitialData
	 * 
	 * @author Shawn Hatch
	 *
	 */
	public static class Builder implements PluginDataBuilder {
		private Data data;
		private boolean dataIsMutable;

		private Builder(Data data) {
			this.data = data;
		}

		public ModelPluginData build() {
			try {

				return new ModelPluginData(data);
			} finally {
				data = new Data();
			}
		}

		public Builder setUseEventFilters(final boolean useEventFilters) {
			ensureDataMutability();
			data.useEventFilters = useEventFilters;
			return this;
		}

		private void ensureDataMutability() {
			if (!dataIsMutable) {
				data = new Data(data);
				dataIsMutable = true;
			}
		}

		public Builder setEventCount(final int eventCount) {
			ensureDataMutability();
			data.eventCount = eventCount;
			return this;
		}

	}

	private static class Data {

		private int eventCount;
		private boolean useEventFilters;
		

		private Data() {
		}

		private Data(Data data) {
			eventCount = data.eventCount;
			useEventFilters = data.useEventFilters;
		}
	}

	/**
	 * Returns a Builder instance
	 */
	public static Builder builder() {
		return new Builder(new Data());
	}

	private final Data data;

	private ModelPluginData(final Data data) {
		this.data = data;
	}

	public int getEventCount() {
		return data.eventCount;
	}

	public boolean getUseEventFilters() {
		return data.useEventFilters;
	}

	@Override
	public PluginDataBuilder getCloneBuilder() {
		return new Builder(data);
	}

}
