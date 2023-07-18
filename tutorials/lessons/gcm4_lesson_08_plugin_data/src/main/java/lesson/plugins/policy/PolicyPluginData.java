package lesson.plugins.policy;

import net.jcip.annotations.Immutable;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;

@Immutable
public final class PolicyPluginData implements PluginData {

	private static class Data {

		private double schoolClosingInfectionRate;
		private boolean distributeVaccineLocally;

		private Data() {
		}

		private Data(final Data data) {
			schoolClosingInfectionRate = data.schoolClosingInfectionRate;
			distributeVaccineLocally = data.distributeVaccineLocally;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (distributeVaccineLocally ? 1231 : 1237);
			long temp;
			temp = Double.doubleToLongBits(schoolClosingInfectionRate);
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
			if (distributeVaccineLocally != other.distributeVaccineLocally) {
				return false;
			}
			if (Double.doubleToLongBits(schoolClosingInfectionRate) != Double
					.doubleToLongBits(other.schoolClosingInfectionRate)) {
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
		public PolicyPluginData build() {
			return new PolicyPluginData(new Data(data));
		}

		public Builder setSchoolClosingInfectionRate(double schoolClosingInfectionRate) {
			data.schoolClosingInfectionRate = schoolClosingInfectionRate;
			return this;
		}

		public Builder setDistributeVaccineLocally(boolean distributeVaccineLocally) {
			data.distributeVaccineLocally = distributeVaccineLocally;
			return this;
		}

	}

	public static Builder builder() {
		return new Builder(new Data());
	}

	private final Data data;

	private PolicyPluginData(final Data data) {
		this.data = data;
	}

	public double getSchoolClosingInfectionRate() {
		return data.schoolClosingInfectionRate;
	}

	public boolean isDistributeVaccineLocally() {
		return data.distributeVaccineLocally;
	}

	@Override
	public PluginDataBuilder getCloneBuilder() {
		return new Builder(new Data(data));
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
		if (!(obj instanceof PolicyPluginData)) {
			return false;
		}
		PolicyPluginData other = (PolicyPluginData) obj;
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
