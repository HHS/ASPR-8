package plugins.groups.support;

import java.util.Optional;

import plugins.people.support.PersonId;
import plugins.stochastics.support.RandomNumberGeneratorId;

public final class GroupSampler {
	private final PersonId excludedPerson;

	private final RandomNumberGeneratorId randomNumberGeneratorId;

	private final GroupWeightingFunction weightingFunction;

	private GroupSampler(Scaffold scaffold) {
		this.excludedPerson = scaffold.excludedPerson;
		this.weightingFunction = scaffold.weightingFunction;
		this.randomNumberGeneratorId = scaffold.randomNumberGeneratorId;
	}

	private static class Scaffold {

		private PersonId excludedPerson;

		private RandomNumberGeneratorId randomNumberGeneratorId;

		private GroupWeightingFunction weightingFunction;
	}

	public static Builder builder() {
		return new Builder();
	}

	public final static class Builder {
		Scaffold scaffold = new Scaffold();

		private Builder() {

		}

		public GroupSampler build() {
			try {
				return new GroupSampler(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}

		public Builder setExcludedPersonId(PersonId personId) {
			scaffold.excludedPerson = personId;
			return this;
		}

		public Builder setRandomNumberGeneratorId(RandomNumberGeneratorId randomNumberGeneratorId) {
			scaffold.randomNumberGeneratorId = randomNumberGeneratorId;
			return this;
		}

		public Builder setGroupWeightingFunction(GroupWeightingFunction weightingFunction) {
			scaffold.weightingFunction = weightingFunction;
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
