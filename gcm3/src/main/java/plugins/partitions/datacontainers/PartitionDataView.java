package plugins.partitions.datacontainers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.math3.random.RandomGenerator;

import nucleus.AgentId;
import nucleus.Context;
import nucleus.DataView;
import nucleus.NucleusError;
import plugins.partitions.support.LabelSet;
import plugins.partitions.support.PartitionError;
import plugins.partitions.support.PartitionSampler;
import plugins.partitions.support.PopulationPartition;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.stochastics.StochasticsDataView;
import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.support.StochasticsError;
import util.ContractException;

/**
 * Published data view that provides partition membership information.
 *
 * @author Shawn Hatch
 *
 */

public final class PartitionDataView implements DataView {

	private final PartitionDataManager partitionDataManager;
	private final Context context;
	private PersonDataView personDataView;
	private StochasticsDataView stochasticsDataView;

	/**
	 * Constructs this partition data view from the given context and partition
	 * data manager.
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain NucleusError#NULL_CONTEXT} if the context is
	 *             null</li>
	 * 
	 *             <li>{@linkplain PartitionError#NULL_PARTITION_DATA_MANAGER}
	 *             if the partition data manager is null</li>
	 */
	public PartitionDataView(final Context context, PartitionDataManager partitionDataManager) {
		if (context == null) {
			throw new ContractException(NucleusError.NULL_CONTEXT);
		}
		if (partitionDataManager == null) {
			throw new ContractException(PartitionError.NULL_PARTITION_DATA_MANAGER);
		}
		this.context = context;
		this.partitionDataManager = partitionDataManager;
		personDataView = context.getDataView(PersonDataView.class).get();
		stochasticsDataView = context.getDataView(StochasticsDataView.class).get();
	}

	/**
	 * Returns the list of person identifiers in the population partition for
	 * the given key.
	 *
	 * @throws ContractException
	 * 
	 *             <li>{@link PartitionError.NULL_PARTITION_KEY} if the key is
	 *             null</li>
	 * 
	 *             <li>{@link PartitionError.UNKNOWN_POPULATION_PARTITION_KEY}
	 *             if the key is unknown</li>
	 */
	public List<PersonId> getPeople(final Object key) {
		validateKeyExists(key);
		return partitionDataManager.getPopulationPartition(key).getPeople();
	}

	/**
	 * Returns the list of person identifiers in the population partition for
	 * the given key. A person is included if every label in the label set is
	 * equal to the corresponding label for the person.
	 *
	 * @throws ContractException
	 * 
	 *             <li>{@link PartitionError.NULL_PARTITION_KEY} if the key is
	 *             null</li>
	 * 
	 *             <li>{@link PartitionError.UNKNOWN_POPULATION_PARTITION_KEY}
	 *             if the key is unknown</li>
	 * 
	 *             <li>{@link PartitionError.NULL_LABEL_SET} if the label set is
	 *             null</li>
	 * 
	 *             <li>{@link PartitionError.INCOMPATIBLE_LABEL_SET} if the
	 *             label set contains dimensions not contained in the population
	 *             partition</li>
	 */
	public List<PersonId> getPeople(final Object key, LabelSet labelSet) {
		validateKeyExists(key);
		PopulationPartition populationPartition = partitionDataManager.getPopulationPartition(key);
		validateLabelSet(populationPartition, labelSet);
		return populationPartition.getPeople(labelSet);
	}

	/**
	 * Returns the number of people in the population partition for the given
	 * key.
	 *
	 * <li>{@link PartitionError.NULL_PARTITION_KEY} if the key is null</li>
	 * 
	 * <li>{@link PartitionError.UNKNOWN_POPULATION_PARTITION_KEY} if the key is
	 * unknown</li>
	 */
	public int getPersonCount(final Object key) {
		validateKeyExists(key);
		PopulationPartition populationPartition = partitionDataManager.getPopulationPartition(key);
		return populationPartition.getPeopleCount();
	}

	/**
	 * Returns the number of people in the population partition for the given
	 * key under the given label set. A person is counted if every label in the
	 * label set is equal to the corresponding label for the person.
	 *
	 * @throws ContractException
	 * 
	 *             <li>{@link PartitionError.NULL_PARTITION_KEY} if the key is
	 *             null</li>
	 * 
	 *             <li>{@link PartitionError.UNKNOWN_POPULATION_PARTITION_KEY}
	 *             if the key is unknown</li>
	 * 
	 *             <li>{@link PartitionError.NULL_LABEL_SET} if the label set is
	 *             null</li>
	 * 
	 *             <li>{@link PartitionError.INCOMPATIBLE_LABEL_SET} if the
	 *             label set contains dimensions not contained in the population
	 *             partition</li>
	 * 
	 */
	public int getPersonCount(final Object key, LabelSet labelSet) {
		validateKeyExists(key);
		PopulationPartition populationPartition = partitionDataManager.getPopulationPartition(key);
		validateLabelSet(populationPartition, labelSet);
		return populationPartition.getPeopleCount(labelSet);
	}

	/**
	 * Returns a mapping from LabelSet to Integer whose keys are all the label
	 * sets in the population partition that match the given label set. The
	 * values are the number of people matching that label set. A person is
	 * counted if every label in the label set is equal to the corresponding
	 * label for the person. All values will be positive.
	 *
	 * @throws ContractException
	 * 
	 *             <li>{@link PartitionError.NULL_PARTITION_KEY} if the key is
	 *             null</li>
	 * 
	 *             <li>{@link PartitionError.UNKNOWN_POPULATION_PARTITION_KEY}
	 *             if the key is unknown</li>
	 * 
	 *             <li>{@link PartitionError.NULL_LABEL_SET} if the label set is
	 *             null</li>
	 * 
	 *             <li>{@link PartitionError.INCOMPATIBLE_LABEL_SET} if the
	 *             label set contains dimensions not contained in the population
	 *             partition</li>
	 * 
	 */
	public Map<LabelSet, Integer> getPeopleCountMap(final Object key, LabelSet labelSet) {
		validateKeyExists(key);
		PopulationPartition populationPartition = partitionDataManager.getPopulationPartition(key);
		validateLabelSet(populationPartition, labelSet);
		return populationPartition.getPeopleCountMap(labelSet);
	}

	/**
	 * Returns a randomly selected person from the given population partition
	 * using the given PartitionSampler.
	 *
	 * @throws ContractException
	 * 
	 *             <li>{@link PartitionError.NULL_PARTITION_KEY} if the key is
	 *             null</li>
	 * 
	 *             <li>{@link PartitionError.UNKNOWN_POPULATION_PARTITION_KEY}
	 *             if the key is unknown</li>
	 * 
	 *             <li>{@link PartitionError.NULL_PARTITION_SAMPLER} if the
	 *             partition sampler is null</li>
	 * 
	 *             <li>{@link PartitionError.INCOMPATIBLE_LABEL_SET} if the
	 *             partition sampler has a label set containing dimensions not
	 *             present in the population partition</li>
	 * 
	 *             <li>{@link PersonError.UNKNOWN_PERSON_ID} if the partition
	 *             sampler has an excluded person that does not exist</li>
	 * 
	 *             <li>{@link StochasticsError.UNKNOWN_RANDOM_NUMBER_GENERATOR_ID}
	 *             if the partition sampler has a random number generator id
	 *             that is unknown</li>
	 *
	 */
	public Optional<PersonId> samplePartition(Object key, PartitionSampler partitionSampler) {
		validateKeyExists(key);
		PopulationPartition populationPartition = partitionDataManager.getPopulationPartition(key);
		validatePartitionSampler(populationPartition, partitionSampler);
		return populationPartition.samplePartition(partitionSampler);
	}

	/**
	 * Returns true if and only if the person is contained in the population
	 * partition corresponding to the key.
	 * 
	 * @throws ContractException
	 *
	 *             <li>{@link PersonError.NULL_PERSON_ID} if the person id is
	 *             null</li>
	 * 
	 *             <li>{@link PersonError.UNKNOWN_PERSON_ID} if the person id is
	 *             null</li>
	 * 
	 *             <li>{@link PartitionError.NULL_PARTITION_KEY} if the key is
	 *             null</li>
	 * 
	 *             <li>{@link PartitionError.UNKNOWN_POPULATION_PARTITION_KEY}
	 *             if the key is unknown</li>
	 */

	public boolean contains(final PersonId personId, Object key) {
		validatePersonNotNull(personId);
		validatePersonExists(personId);
		validateKeyExists(key);
		PopulationPartition populationPartition = partitionDataManager.getPopulationPartition(key);
		return populationPartition.contains(personId);
	}

	/**
	 * Returns true if and only if the person is contained in the population
	 * corresponding to the key. A person is counted if every label in the label
	 * set is equal to the corresponding label for the person.
	 * 
	 * @throws ContractException
	 *
	 *             <li>{@link PersonError.NULL_PERSON_ID} if the person id is
	 *             null</li>
	 * 
	 *             <li>{@link PersonError.UNKNOWN_PERSON_ID} if the person id is
	 *             null</li>
	 * 
	 *             <li>{@link PartitionError.NULL_PARTITION_KEY} if the key is
	 *             null</li>
	 * 
	 *             <li>{@link PartitionError.UNKNOWN_POPULATION_PARTITION_KEY}
	 *             if the key is unknown</li>
	 * 
	 *             <li>{@link PartitionError.NULL_LABEL_SET} if the label set is
	 *             null</li>
	 * 
	 *             <li>{@link PartitionError.INCOMPATIBLE_LABEL_SET} if the
	 *             label contains a dimension not present in the partition</li>
	 * 
	 * 
	 * 
	 */

	public boolean contains(PersonId personId, LabelSet labelSet, Object key) {
		validatePersonNotNull(personId);
		validatePersonExists(personId);
		validateKeyExists(key);
		PopulationPartition populationPartition = partitionDataManager.getPopulationPartition(key);
		validateLabelSet(populationPartition, labelSet);

		return populationPartition.contains(personId, labelSet);
	}

	/**
	 * Returns true if and only if a partition is associated with the given key.
	 * Null tolerant.
	 */
	public boolean partitionExists(final Object key) {
		return partitionDataManager.partitionExists(key);
	}

	/**
	 * Returns the ComponentId of the component that added the population
	 * partition.
	 * 
	 * <li>{@link PartitionError.NULL_PARTITION_KEY} if the key is null</li>
	 * 
	 * <li>{@link PartitionError.UNKNOWN_POPULATION_PARTITION_KEY} if the key is
	 * unknown</li>
	 */
	public AgentId getOwningAgentId(final Object key) {
		validateKeyExists(key);
		return partitionDataManager.getOwningAgentId(key);
	}

	private void validateLabelSet(final PopulationPartition populationPartition, final LabelSet labelSet) {
		if (labelSet == null) {
			context.throwContractException(PartitionError.NULL_LABEL_SET);
		}

		if (!populationPartition.validateLabelSetInfo(labelSet)) {
			context.throwContractException(PartitionError.INCOMPATIBLE_LABEL_SET);
		}
	}

	private void validatePartitionSampler(PopulationPartition populationPartition, final PartitionSampler partitionSampler) {
		if (partitionSampler == null) {
			context.throwContractException(PartitionError.NULL_PARTITION_SAMPLER);
		}

		if (partitionSampler.getLabelSet().isPresent()) {
			final LabelSet labelSet = partitionSampler.getLabelSet().get();
			if (!populationPartition.validateLabelSetInfo(labelSet)) {
				context.throwContractException(PartitionError.INCOMPATIBLE_LABEL_SET);
			}
		}

		if (partitionSampler.getExcludedPerson().isPresent()) {
			final PersonId excludedPersonId = partitionSampler.getExcludedPerson().get();
			validatePersonExists(excludedPersonId);
		}

		if (partitionSampler.getRandomNumberGeneratorId().isPresent()) {
			final RandomNumberGeneratorId randomNumberGeneratorId = partitionSampler.getRandomNumberGeneratorId().get();
			validateRandomNumberGeneratorId(randomNumberGeneratorId);
		}

	}

	/*
	 * Precondition : person id is not null
	 */
	private void validatePersonExists(final PersonId personId) {
		if (!personDataView.personExists(personId)) {
			context.throwContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	private void validatePersonNotNull(final PersonId personId) {
		if (personId == null) {
			context.throwContractException(PersonError.NULL_PERSON_ID);
		}
	}

	private void validateKeyExists(final Object key) {
		if (key == null) {
			context.throwContractException(PartitionError.NULL_PARTITION_KEY);
		}
		if (!partitionDataManager.partitionExists(key)) {
			context.throwContractException(PartitionError.UNKNOWN_POPULATION_PARTITION_KEY, key);
		}
	}

	private void validateRandomNumberGeneratorId(final RandomNumberGeneratorId randomNumberGeneratorId) {
		if (randomNumberGeneratorId == null) {
			context.throwContractException(StochasticsError.NULL_RANDOM_NUMBER_GENERATOR_ID);
		}
		final RandomGenerator randomGenerator = stochasticsDataView.getRandomGeneratorFromId(randomNumberGeneratorId);
		if (randomGenerator == null) {
			context.throwContractException(StochasticsError.UNKNOWN_RANDOM_NUMBER_GENERATOR_ID, randomNumberGeneratorId);
		}
	}

}