package lessons.lesson_11.plugins.disease;

import net.jcip.annotations.Immutable;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;

@Immutable
public final class DiseasePluginData implements PluginData {

	private static class Data {

		private double r0;

		private double asymptomaticDays;

		private double symptomaticDays;

		private Data() {
		}

		private Data(final Data data) {
			r0 = data.r0;
			asymptomaticDays = data.asymptomaticDays;
			symptomaticDays = data.symptomaticDays;
		}
	}

	public static class Builder implements PluginDataBuilder {
		private Data data;
		private boolean dataIsMutable;

		private Builder(final Data data) {
			this.data = data;
		}

		@Override
		public DiseasePluginData build() {
			try {
				return new DiseasePluginData(data);
			} finally {
				data = new Data();
			}
		}

		private void ensureDataMutability() {
			if (!dataIsMutable) {
				data = new Data(data);
				dataIsMutable = true;
			}
		}

		public Builder setAsymptomaticDays(final double asymptomaticDays) {
			ensureDataMutability();
			data.asymptomaticDays = asymptomaticDays;
			return this;
		}

		public Builder setR0(final double r0) {
			ensureDataMutability();
			data.r0 = r0;
			return this;
		}

		public Builder setSymptomaticDays(final double symptomaticDays) {
			ensureDataMutability();
			data.symptomaticDays = symptomaticDays;
			return this;
		}

	}

	public static Builder builder() {
		return new Builder(new Data());
	}

	private final Data data;

	private DiseasePluginData(final Data data) {
		this.data = data;
	}

	public double getAsymptomaticDays() {
		return data.asymptomaticDays;
	}

	public double getR0() {
		return data.r0;
	}

	public double getSymptomaticDays() {
		return data.symptomaticDays;
	}

	@Override
	public PluginDataBuilder getCloneBuilder() {
		return new Builder(data);
	}

}
