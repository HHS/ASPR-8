package plugins.groups.support;

import java.util.Optional;

import plugins.people.support.PersonId;
import plugins.stochastics.support.RandomNumberGeneratorId;

public final class GroupSampler {
	private final PersonId excludedPerson;

	private final RandomNumberGeneratorId randomNumberGeneratorId;

	private final GroupWeightingFunction weightingFunction;

	private GroupSampler(Data data) {
		this.excludedPerson = data.excludedPerson;
		this.weightingFunction = data.weightingFunction;
		this.randomNumberGeneratorId = data.randomNumberGeneratorId;
	}

	private static class Data {

		private PersonId excludedPerson;

		private RandomNumberGeneratorId randomNumberGeneratorId;

		private GroupWeightingFunction weightingFunction;

		public Data() {
		}

		public Data(Data data) {
			excludedPerson = data.excludedPerson;

			randomNumberGeneratorId = data.randomNumberGeneratorId;

			weightingFunction = data.weightingFunction;

		}
	}

	public static Builder builder() {
		return new Builder();
	}

	public final static class Builder {
		Data data = new Data();

		private Builder() {

		}

		public GroupSampler build() {
			return new GroupSampler(new Data(data));
		}

		public Builder setExcludedPersonId(PersonId personId) {
			data.excludedPerson = personId;
			return this;
		}

		public Builder setRandomNumberGeneratorId(RandomNumberGeneratorId randomNumberGeneratorId) {
			data.randomNumberGeneratorId = randomNumberGeneratorId;
			return this;
		}

		public Builder setGroupWeightingFunction(GroupWeightingFunction weightingFunction) {
			data.weightingFunction = weightingFunction;
			return this;
		}

	}

	public Optional<PersonId> getExcludedPerson() {
		return Optional.ofNullable(excludedPerson);
	}

	public Optional<RandomNumberGeneratorId> getRandomNumberGeneratorId() {
		return Optional.ofNullable(randomNumberGeneratorId);
	}

	public Optional<GroupWeightingFunction> getWeightingFunction() {
		return Optional.ofNullable(weightingFunction);
	}

}
