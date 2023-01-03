package plugins.partitions.support;

import java.util.Optional;

import plugins.people.support.PersonId;
import plugins.stochastics.support.RandomNumberGeneratorId;

/**
 * 
 * A {@link PartitionSampler} represents the details of a sample query for a
 * {@link Partition}. All inputs to the {@link PartitionSampler} are optional.
 * 
 * 
 *
 */
public final class PartitionSampler {

	private final Scaffold scaffold;

	private static class Scaffold {

		private PersonId excludedPersonId;

		private RandomNumberGeneratorId randomNumberGeneratorId;

		private LabelSet labelSet;

		private LabelSetWeightingFunction labelSetWeightingFunction;
	}

	/**
	 * Returns a new Builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Standard builder class for partition samplers. All inputs are optional.
	 * 
	 *
	 */
	public static class Builder {
		private Scaffold scaffold = new Scaffold();

		private Builder() {
		}

		/**
		 * Returns the {@linkplain PartitionSampler} formed from the inputs
		 * collected by this builder and resets the state of the builder to
		 * empty.
		 */
		public PartitionSampler build() {
			try {
				return new PartitionSampler(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}

		/**
		 * Sets the {@link PersonId} to be excluded as a sample result.
		 */
		public Builder setExcludedPerson(PersonId excludedPersonId) {
			scaffold.excludedPersonId = excludedPersonId;
			return this;
		}

		/**
		 * Sets the {@link RandomNumberGeneratorId} to be used when sampling
		 * from a {@link Partition}. If no {@link RandomNumberGeneratorId} is
		 * provided, the default random number generator for the simulation is
		 * used.
		 */
		public Builder setRandomNumberGeneratorId(RandomNumberGeneratorId randomNumberGeneratorId) {
			scaffold.randomNumberGeneratorId = randomNumberGeneratorId;
			return this;
		}

		/**
		 * Sets the {@link LabelSet} for this {@link PartitionSampler}. If no
		 * {@link LabelSet} is provided, all cells of the {@link Partition}
		 * participate in the sampling.
		 */
		public Builder setLabelSet(LabelSet labelSet) {
			scaffold.labelSet = labelSet;
			return this;
		}

		/**
		 * Sets the {@link LabelSetWeightingFunction} for this
		 * {@link PartitionSampler}. If no {@link LabelSetWeightingFunction} is
		 * provided, all cells of the {@link Partition} are weighted uniformly.
		 */
		public Builder setLabelSetWeightingFunction(LabelSetWeightingFunction labelSetWeightingFunction) {
			scaffold.labelSetWeightingFunction = labelSetWeightingFunction;
			return this;
		}

	}

	/**
	 * Returns the PersonId to be excluded as a result of sampling a
	 * {@link Partition}
	 */
	public Optional<PersonId> getExcludedPerson() {
		return Optional.ofNullable(scaffold.excludedPersonId);
	}

	/**
	 * Returns the {@link RandomNumberGeneratorId} to be used when generating
	 * the random sample. If no {@link RandomNumberGeneratorId} is provided, the
	 * default random number generator for the simulation is used.
	 */
	public Optional<RandomNumberGeneratorId> getRandomNumberGeneratorId() {
		return Optional.ofNullable(scaffold.randomNumberGeneratorId);
	}

	/**
	 * Returns the {@link LabelSet} for this {@link PartitionSampler}. If no
	 * {@link LabelSet} is provided, all cells of the {@link Partition}
	 * participate in the sampling.
	 */
	public Optional<LabelSet> getLabelSet() {
		return Optional.ofNullable(scaffold.labelSet);
	}

	/**
	 * Returns the {@link LabelSetWeightingFunction} for this
	 * {@link PartitionSampler}. If no {@link LabelSetWeightingFunction} is
	 * provided, all cells of the {@link Partition} are weighted uniformly.
	 */
	public Optional<LabelSetWeightingFunction> getLabelSetWeightingFunction() {
		return Optional.ofNullable(scaffold.labelSetWeightingFunction);
	}

	private PartitionSampler(Scaffold scaffold) {
		this.scaffold = scaffold;
	}

}
