package gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.math3.random.RandomGenerator;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.filters.Filter;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.filters.TrueFilter;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.containers.BasePeopleContainer;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.containers.PeopleContainer;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.datamanagers.StochasticsDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.support.RandomNumberGeneratorId;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * Implementation of PopulationPartition for degenerate partitions having only a
 * filter and a single cell in its partition space, i.e. the partition was
 * specified with no labeling functions.
 */
public class DegeneratePopulationPartitionImpl implements PopulationPartition {

	private final StochasticsDataManager stochasticsDataManager;

	private final PeopleContainer peopleContainer;

	private final PartitionsContext partitionsContext;

	private final Filter filter;

	private final Map<Class<? extends Event>, List<FilterSensitivity<? extends Event>>> eventClassToFilterSensitivityMap = new LinkedHashMap<>();

	/**
	 * Constructs an DegeneratePopulationPartitionImpl
	 *
	 * @throws ContractException {@linkplain PartitionError#NON_DEGENERATE_PARTITION}
	 *                           if the partition contains labelers
	 * @throws RuntimeException
	 *                           <ul>
	 *                           <li>if context is null</li>
	 *                           <li>if partition is null</li>
	 *                           <li>if the partition contains labelers</li>
	 *                           </ul>
	 */
	public DegeneratePopulationPartitionImpl(final PartitionsContext partitionsContext, final Partition partition,
			boolean supportRunContinuity) {
		this.partitionsContext = partitionsContext;
		stochasticsDataManager = partitionsContext.getDataManager(StochasticsDataManager.class);
		filter = partition.getFilter().orElse(new TrueFilter());

		if (!partition.isDegenerate()) {
			throw new ContractException(PartitionError.NON_DEGENERATE_PARTITION);
		}

		for (final FilterSensitivity<? extends Event> filterSensitivity : filter.getFilterSensitivities()) {
			List<FilterSensitivity<? extends Event>> list = eventClassToFilterSensitivityMap
					.get(filterSensitivity.getEventClass());
			if (list == null) {
				list = new ArrayList<>();
				eventClassToFilterSensitivityMap.put(filterSensitivity.getEventClass(), list);
			}
			list.add(filterSensitivity);
		}
		
		final PeopleDataManager peopleDataManager = partitionsContext.getDataManager(PeopleDataManager.class);
		peopleContainer = new BasePeopleContainer(peopleDataManager, supportRunContinuity);

		
		final int personIdLimit = peopleDataManager.getPersonIdLimit();
		for (int i = 0; i < personIdLimit; i++) {
			if (peopleDataManager.personIndexExists(i)) {
				final PersonId personId = peopleDataManager.getBoxedPersonId(i).get();
				if (filter.evaluate(partitionsContext, personId)) {
					/*
					 * Using unsafe add since this is in the constructor, we are sure that the
					 * person is not already contained
					 */
					peopleContainer.unsafeAdd(personId);
				}
			}
		}

	}

	@Override
	public void attemptPersonAddition(final PersonId personId) {
		if (filter.evaluate(partitionsContext, personId)) {
			/*
			 * By contract, this method is only invoked with person ids that are new to the
			 * simulation or new to this population partition and thus cannot already in
			 * members of this population partition.
			 */
			peopleContainer.unsafeAdd(personId);
		}
	}

	@Override
	public void attemptPersonRemoval(final PersonId personId) {
		peopleContainer.remove(personId);
	}

	@Override
	public boolean contains(final PersonId personId) {
		return peopleContainer.contains(personId);
	}

	@Override
	public boolean contains(final PersonId personId, final LabelSet labelSet) {
		return peopleContainer.contains(personId);
	}

	/**
	 * Returns the people identifiers of this index
	 */
	@Override
	public List<PersonId> getPeople() {
		return peopleContainer.getPeople();
	}

	@Override
	public List<PersonId> getPeople(final LabelSet labelSet) {
		return peopleContainer.getPeople();
	}

	@Override
	public int getPeopleCount() {
		return peopleContainer.size();
	}

	@Override
	public int getPeopleCount(final LabelSet labelSet) {
		return peopleContainer.size();
	}

	/**
	 * Returns a map whose single key is an empty label set and whose single value
	 * is the number of people in the population partition. Precondition: the label
	 * set should be empty or null.
	 */
	@Override
	public Map<LabelSet, Integer> getPeopleCountMap(LabelSet labelSet) {
		Map<LabelSet, Integer> result = new LinkedHashMap<>();
		result.put(LabelSet.builder().build(), peopleContainer.size());
		return result;
	}

	@Override
	public void handleEvent(final Event event) {
		PersonId personId = null;
		final List<FilterSensitivity<? extends Event>> filterSensitivities = eventClassToFilterSensitivityMap
				.get(event.getClass());
		if (filterSensitivities != null) {
			for (final FilterSensitivity<? extends Event> filterSensitivity : filterSensitivities) {
				final Optional<PersonId> optionalPersonId = filterSensitivity.requiresRefresh(partitionsContext, event);
				if (optionalPersonId.isPresent()) {
					personId = optionalPersonId.get();
					break;
				}
			}
		}

		if (personId != null) {
			if (filter.evaluate(partitionsContext, personId)) {
				peopleContainer.safeAdd(personId);
			} else {
				peopleContainer.remove(personId);
			}
		}

	}

	/**
	 * Forces the index to evaluate a person's membership in this index.
	 */
	@Override
	public Optional<PersonId> samplePartition(final PartitionSampler partitionSampler) {
		RandomGenerator randomGenerator;
		final RandomNumberGeneratorId randomNumberGeneratorId = partitionSampler.getRandomNumberGeneratorId()
				.orElse(null);
		if (randomNumberGeneratorId != null) {
			randomGenerator = stochasticsDataManager.getRandomGeneratorFromId(randomNumberGeneratorId);
		} else {
			randomGenerator = stochasticsDataManager.getRandomGenerator();
		}

		final PersonId excludedPersonId = partitionSampler.getExcludedPerson().orElse(null);

		int candidateCount = peopleContainer.size();
		if (excludedPersonId != null) {
			if (peopleContainer.contains(excludedPersonId)) {
				candidateCount--;
			}
		}
		PersonId result = null;
		if (candidateCount > 0) {
			while (true) {
				result = peopleContainer.getRandomPersonId(randomGenerator);
				if (!result.equals(excludedPersonId)) {
					break;
				}
			}
		}

		return Optional.ofNullable(result);
	}

	@Override
	public boolean validateLabelSetInfo(final LabelSet labelSet) {
		return labelSet.isEmpty();
	}

	@Override
	public <T> Optional<T> getPersonValue(LabelSetFunction<T> labelSetFunction, PersonId personId) {
		return Optional.ofNullable(null);
	}

}
