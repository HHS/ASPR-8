package gov.hhs.aspr.ms.gcm.plugins.partitions.datamanagers;

import gov.hhs.aspr.ms.gcm.nucleus.PluginData;
import gov.hhs.aspr.ms.gcm.nucleus.PluginDataBuilder;
import net.jcip.annotations.Immutable;

/**
 * An immutable container of the initial state of partitions. It contains: <BR>
 * <ul>
 * <li>partitions</li>
 * </ul>
 * 
 *
 */
@Immutable
public final class PartitionsPluginData implements PluginData {

	/**
	 * Builder class for PartitionsPluginData
	 * 
	 *
	 */
	public static class Builder implements PluginDataBuilder {
		private Data data;

		private Builder(Data data) {
			this.data = data;
		}

		/**
		 * Returns the {@link PartitionsPluginData} from the collected
		 * information supplied to this builder.
		 */
		public PartitionsPluginData build() {
			return new PartitionsPluginData(new Data(data));
		}

		/**
		 * Set the run continuity support policy. Defaults to false. Supporting
		 * run continuity may increase the memory requirements for partitions,
		 * but will guarantee run continuity.
		 *
		 */
		public Builder setRunContinuitySupport(final boolean supportRunContinuity) {
		
			data.supportRunContinuity = supportRunContinuity;
			return this;
		}
	}

	private static class Data {

		private boolean supportRunContinuity;

		private Data() {
		}

		private Data(Data data) {
			supportRunContinuity = data.supportRunContinuity;			
		}

		@Override
		public int hashCode() {

			final int prime = 31;
			int result = 1;
			result = prime * result + (supportRunContinuity ? 1231 : 1237);
			return result;
		}

		@Override
		public boolean equals(Object obj) {

			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Data)) {
				return false;
			}
			Data other = (Data) obj;
			if (supportRunContinuity != other.supportRunContinuity) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Data [supportRunContinuity=");
			builder.append(supportRunContinuity);
			builder.append("]");
			return builder.toString();
		}

	}

	/**
	 * Returns a Builder instance
	 */
	public static Builder builder() {
		return new Builder(new Data());
	}
	
	private final Data data;

	private PartitionsPluginData(final Data data) {
		this.data = data;
	}

	/**
	 * Returns the run continuity support policy
	 * 
	 */
	public boolean supportsRunContinuity() {
		return data.supportRunContinuity;
	}

	@Override
	public PluginDataBuilder getCloneBuilder() {
		return new Builder(new Data(data));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + data.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof PartitionsPluginData)) {
			return false;
		}
		PartitionsPluginData other = (PartitionsPluginData) obj;
		if (!data.equals(other.data)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder2 = new StringBuilder();
		builder2.append("PartitionsPluginData [data=");
		builder2.append(data);
		builder2.append("]");
		return builder2.toString();
	}

}
