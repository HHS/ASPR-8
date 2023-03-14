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

	public boolean distributeVaccineLocally() {
		return data.distributeVaccineLocally;
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
