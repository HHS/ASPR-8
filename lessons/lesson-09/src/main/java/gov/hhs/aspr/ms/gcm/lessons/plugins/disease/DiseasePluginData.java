package gov.hhs.aspr.ms.gcm.lessons.plugins.disease;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginDataBuilder;
import net.jcip.annotations.Immutable;

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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(asymptomaticDays);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(r0);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(symptomaticDays);
			result = prime * result + (int) (temp ^ (temp >>> 32));
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
			if (Double.doubleToLongBits(asymptomaticDays) != Double.doubleToLongBits(other.asymptomaticDays)) {
				return false;
			}
			if (Double.doubleToLongBits(r0) != Double.doubleToLongBits(other.r0)) {
				return false;
			}
			if (Double.doubleToLongBits(symptomaticDays) != Double.doubleToLongBits(other.symptomaticDays)) {
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
		public DiseasePluginData build() {
			return new DiseasePluginData(new Data(data));
		}

		public Builder setAsymptomaticDays(final double asymptomaticDays) {
			data.asymptomaticDays = asymptomaticDays;
			return this;
		}

		public Builder setR0(final double r0) {
			data.r0 = r0;
			return this;
		}

		public Builder setSymptomaticDays(final double symptomaticDays) {
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
		if (!(obj instanceof DiseasePluginData)) {
			return false;
		}
		DiseasePluginData other = (DiseasePluginData) obj;
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
