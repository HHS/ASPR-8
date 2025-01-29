package gov.hhs.aspr.ms.gcm.lessons.plugins.policy;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginDataBuilder;
import net.jcip.annotations.Immutable;

@Immutable
public final class PolicyPluginData implements PluginData {

	private static class Data {

		private double schoolClosingInfectionRate;
		private boolean distributeVaccineLocally;
		private boolean locked;

		private Data() {
		}

		private Data(final Data data) {
			schoolClosingInfectionRate = data.schoolClosingInfectionRate;
			distributeVaccineLocally = data.distributeVaccineLocally;
			locked = data.locked;
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
			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new PolicyPluginData(data);
		}

		public Builder setSchoolClosingInfectionRate(double schoolClosingInfectionRate) {
			ensureDataMutability();
			data.schoolClosingInfectionRate = schoolClosingInfectionRate;
			return this;
		}

		public Builder setDistributeVaccineLocally(boolean distributeVaccineLocally) {
			ensureDataMutability();
			data.distributeVaccineLocally = distributeVaccineLocally;
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

	private PolicyPluginData(final Data data) {
		this.data = data;
	}

	public double getSchoolClosingInfectionRate() {
		return data.schoolClosingInfectionRate;
	}

	public boolean distributeVaccineLocally() {
		return data.distributeVaccineLocally;
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
