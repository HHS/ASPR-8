package gov.hhs.aspr.ms.gcm.lessons.plugins.family;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginDataBuilder;
import net.jcip.annotations.Immutable;

@Immutable
public final class FamilyPluginData implements PluginData {

	private static class Data {

		private int familyCount;

		private int maxFamilySize;

		private boolean locked;

		private Data() {
		}

		private Data(final Data data) {
			familyCount = data.familyCount;
			maxFamilySize = data.maxFamilySize;
			locked = data.locked;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + familyCount;
			result = prime * result + maxFamilySize;
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
			if (familyCount != other.familyCount) {
				return false;
			}
			if (maxFamilySize != other.maxFamilySize) {
				return false;
			}
			return true;
		}
	}

	public static class Builder implements PluginDataBuilder {
		private Data data;

		private Builder(final Data data) {
			this.data = data;
		}

		@Override
		public FamilyPluginData build() {
			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new FamilyPluginData(data);
		}

		/**
		 * Sets the family count
		 * 
		 * @throws IllegalArgumentException
		 *                                  <li>if the family count is negative</li>
		 */
		public Builder setFamilyCount(final int familyCount) {
			ensureDataMutability();
			if (familyCount < 0) {
				throw new IllegalArgumentException("negative family count");
			}
			data.familyCount = familyCount;
			return this;
		}

		/**
		 * Sets the maximum family size
		 * 
		 * @throws IllegalArgumentException
		 *                                  <li>if the max family size is negative</li>
		 */
		public Builder setMaxFamilySize(final int maxFamilySize) {
			ensureDataMutability();
			if (maxFamilySize < 0) {
				throw new IllegalArgumentException("negative max family count");
			}
			data.maxFamilySize = maxFamilySize;
			return this;
		}

		private void ensureDataMutability() {
			if (data.locked) {
				data = new Data(data);
				data.locked = false;
			}
		}

		private void ensureImmutability() {
			if (!data.locked) {
				data.locked = true;
			}
		}

		private void validateData() {
		}

	}

	public static Builder builder() {
		return new Builder(new Data());
	}

	private final Data data;

	private FamilyPluginData(final Data data) {
		this.data = data;
	}

	public int getFamilyCount() {
		return data.familyCount;
	}

	public int getMaxFamilySize() {
		return data.maxFamilySize;
	}

	@Override
	public PluginDataBuilder toBuilder() {
		return new Builder(data);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof FamilyPluginData)) {
			return false;
		}
		FamilyPluginData other = (FamilyPluginData) obj;
		if (data == null) {
			if (other.data != null) {
				return false;
			}
		} else if (!data.equals(other.data)) {
			return false;
		}
		return true;
	}

}
