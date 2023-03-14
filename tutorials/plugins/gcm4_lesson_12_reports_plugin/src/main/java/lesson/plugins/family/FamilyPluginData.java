package lesson.plugins.family;

import net.jcip.annotations.Immutable;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;

@Immutable
public final class FamilyPluginData implements PluginData {

	private static class Data {

		private int familyCount;

		private int maxFamilySize;

		private Data() {
		}

		private Data(final Data data) {
			familyCount = data.familyCount;
			maxFamilySize = data.maxFamilySize;
		}
	}

	public static class Builder implements PluginDataBuilder {
		private Data data;

		private Builder(final Data data) {
			this.data = data;
		}

		@Override
		public FamilyPluginData build() {
			return new FamilyPluginData(new Data(data));
		}

		/**
		 * Sets the family count
		 * 
		 * @throws IllegalArgumentException
		 *             <li>if the family count is negative</li>
		 */
		public Builder setFamilyCount(final int familyCount) {
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
		 *             <li>if the max family size is negative</li>
		 */
		public Builder setMaxFamilySize(final int maxFamilySize) {
			if (maxFamilySize < 0) {
				throw new IllegalArgumentException("negative max family count");
			}
			data.maxFamilySize = maxFamilySize;
			return this;
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
	public PluginDataBuilder getCloneBuilder() {
		return new Builder(new Data(data));
	}

	@Override
	public PluginDataBuilder getEmptyBuilder() {
		return new Builder(new Data());
	}

}
