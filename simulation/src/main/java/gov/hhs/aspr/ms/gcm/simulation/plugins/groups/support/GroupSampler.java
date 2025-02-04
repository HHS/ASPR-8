package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support;

import java.util.Optional;

import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.support.RandomNumberGeneratorId;

public final class GroupSampler {

	private final Data data;

	private GroupSampler(Data data) {
		this.data = data;
	}

	private static class Data {

		private PersonId excludedPerson;

		private RandomNumberGeneratorId randomNumberGeneratorId;

		private GroupWeightingFunction weightingFunction;

		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			excludedPerson = data.excludedPerson;

			randomNumberGeneratorId = data.randomNumberGeneratorId;

			weightingFunction = data.weightingFunction;

			locked = data.locked;
		}
	}

	public static Builder builder() {
		return new Builder(new Data());
	}

	public final static class Builder {
		private Data data;

		private Builder(Data data) {
			this.data = data;
		}

		public GroupSampler build() {
			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new GroupSampler(data);
		}

		public Builder setExcludedPersonId(PersonId personId) {
			ensureDataMutability();
			data.excludedPerson = personId;
			return this;
		}

		public Builder setRandomNumberGeneratorId(RandomNumberGeneratorId randomNumberGeneratorId) {
			ensureDataMutability();
			data.randomNumberGeneratorId = randomNumberGeneratorId;
			return this;
		}

		public Builder setGroupWeightingFunction(GroupWeightingFunction weightingFunction) {
			ensureDataMutability();
			data.weightingFunction = weightingFunction;
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

	public Optional<PersonId> getExcludedPerson() {
		return Optional.ofNullable(data.excludedPerson);
	}

	public Optional<RandomNumberGeneratorId> getRandomNumberGeneratorId() {
		return Optional.ofNullable(data.randomNumberGeneratorId);
	}

	public Optional<GroupWeightingFunction> getWeightingFunction() {
		return Optional.ofNullable(data.weightingFunction);
	}

	public Builder toBuilder() {
		return new Builder(data);
	}

}
