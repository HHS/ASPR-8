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
		
	}

	public static class Builder implements PluginDataBuilder {
		private Data data;
		private boolean dataIsMutable;

		private Builder(final Data data) {
			this.data = data;
		}

		@Override
		public PolicyPluginData build() {
			try {
				return new PolicyPluginData(data);
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
	public PluginDataBuilder getCloneBuilder() {
		return new Builder(data);
	}

}
