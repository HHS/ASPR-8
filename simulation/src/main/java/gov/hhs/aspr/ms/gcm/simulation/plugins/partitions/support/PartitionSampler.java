package gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support;

import java.util.Optional;

import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.support.RandomNumberGeneratorId;

/**
 * A {@link PartitionSampler} represents the details of a sample query for a
 * {@link Partition}. All inputs to the {@link PartitionSampler} are optional.
 */
public final class PartitionSampler {

	private final Data data;

	private static class Data {

		private PersonId excludedPersonId;

		private RandomNumberGeneratorId randomNumberGeneratorId;

		private LabelSet labelSet;

		private LabelSetWeightingFunction labelSetWeightingFunction;

		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			excludedPersonId = data.excludedPersonId;
			randomNumberGeneratorId = data.randomNumberGeneratorId;
			labelSet = data.labelSet;
			labelSetWeightingFunction = data.labelSetWeightingFunction;
			locked = data.locked;
		}
	}

	/**
	 * Returns a new Builder instance
	 */
	public static Builder builder() {
		return new Builder(new Data());
	}

	/**
	 * Standard builder class for partition samplers. All inputs are optional.
	 */
	public static class Builder {
		private Data data;

		private Builder(Data data) {
			this.data = data;
		}

		/**
		 * Returns the {@linkplain PartitionSampler} formed from the inputs collected by
		 * this builder and resets the state of the builder to empty.
		 */
		public PartitionSampler build() {
			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new PartitionSampler(data);
		}

		/**
		 * Sets the {@link PersonId} to be excluded as a sample result.
		 */
		public Builder setExcludedPerson(PersonId excludedPersonId) {
			ensureDataMutability();
			data.excludedPersonId = excludedPersonId;
			return this;
		}

		/**
		 * Sets the {@link RandomNumberGeneratorId} to be used when sampling from a
		 * {@link Partition}. If no {@link RandomNumberGeneratorId} is provided, the
		 * default random number generator for the simulation is used.
		 */
		public Builder setRandomNumberGeneratorId(RandomNumberGeneratorId randomNumberGeneratorId) {
			ensureDataMutability();
			data.randomNumberGeneratorId = randomNumberGeneratorId;
			return this;
		}

		/**
		 * Sets the {@link LabelSet} for this {@link PartitionSampler}. If no
		 * {@link LabelSet} is provided, all cells of the {@link Partition} participate
		 * in the sampling.
		 */
		public Builder setLabelSet(LabelSet labelSet) {
			ensureDataMutability();
			data.labelSet = labelSet;
			return this;
		}

		/**
		 * Sets the {@link LabelSetWeightingFunction} for this {@link PartitionSampler}.
		 * If no {@link LabelSetWeightingFunction} is provided, all cells of the
		 * {@link Partition} are weighted uniformly.
		 */
		public Builder setLabelSetWeightingFunction(LabelSetWeightingFunction labelSetWeightingFunction) {
			ensureDataMutability();
			data.labelSetWeightingFunction = labelSetWeightingFunction;
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

	/**
	 * Returns the PersonId to be excluded as a result of sampling a
	 * {@link Partition}
	 */
	public Optional<PersonId> getExcludedPerson() {
		return Optional.ofNullable(data.excludedPersonId);
	}

	/**
	 * Returns the {@link RandomNumberGeneratorId} to be used when generating the
	 * random sample. If no {@link RandomNumberGeneratorId} is provided, the default
	 * random number generator for the simulation is used.
	 */
	public Optional<RandomNumberGeneratorId> getRandomNumberGeneratorId() {
		return Optional.ofNullable(data.randomNumberGeneratorId);
	}

	/**
	 * Returns the {@link LabelSet} for this {@link PartitionSampler}. If no
	 * {@link LabelSet} is provided, all cells of the {@link Partition} participate
	 * in the sampling.
	 */
	public Optional<LabelSet> getLabelSet() {
		return Optional.ofNullable(data.labelSet);
	}

	/**
	 * Returns the {@link LabelSetWeightingFunction} for this
	 * {@link PartitionSampler}. If no {@link LabelSetWeightingFunction} is
	 * provided, all cells of the {@link Partition} are weighted uniformly.
	 */
	public Optional<LabelSetWeightingFunction> getLabelSetWeightingFunction() {
		return Optional.ofNullable(data.labelSetWeightingFunction);
	}

	private PartitionSampler(Data data) {
		this.data = data;
	}

	/**
	 * Returns a new builder instance that is pre-filled with the current state of
	 * this instance.
	 */
	public Builder toBuilder() {
		return new Builder(data);
	}

}
