package gov.hhs.aspr.ms.gcm.plugins.partitions.datamanagers;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.nucleus.DataManager;
import gov.hhs.aspr.ms.gcm.nucleus.DataManagerContext;
import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.DegeneratePopulationPartitionImpl;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.FilterSensitivity;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.LabelSet;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.LabelSetFunction;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.Labeler;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.LabelerSensitivity;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.Partition;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PartitionError;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PartitionSampler;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PartitionsContext;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PopulationPartition;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PopulationPartitionImpl;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.filters.Filter;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.filters.TrueFilter;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.plugins.people.events.PersonAdditionEvent;
import gov.hhs.aspr.ms.gcm.plugins.people.events.PersonRemovalEvent;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.StochasticsError;
import util.errors.ContractException;

/**
 * Mutable data manager for managing population partitions that are maintained
 * as data changes are made to people.
 * <p>
 * Subscribes to the following events for all partitions:
 * </p>
 * <ul>
 * <li>{@linkplain PersonAdditionEvent} <blockquote>Adds the person to all
 * relevant population partitions after event validation and execution phases
 * are complete. </blockquote></li>
 * <li>{@linkplain PersonRemovalEvent} <blockquote>Removes the person from all
 * population partitions by scheduling the removal for the current time. This
 * allows references and partition memberships to remain long enough for
 * resolvers, agents and reports to have final reference to the person while
 * still associated with any relevant partitions. </blockquote></li>
 * </ul>
 * <p>
 * Subscribes to other events as needed to support the population partition
 * maintenance
 * </p>
 */

public final class PartitionsDataManager extends DataManager {

	private final boolean supportRunContinuity;

	public PartitionsDataManager(PartitionsPluginData partitionsPluginData) {
		this.supportRunContinuity = partitionsPluginData.supportsRunContinuity();
	}

	private final Map<Object, Set<Class<? extends Event>>> keyToEventClassesMap = new LinkedHashMap<>();

	private final Set<Class<? extends Event>> reservedEventClasses = new LinkedHashSet<>();

	private final Map<Class<? extends Event>, Set<Object>> eventClassToKeyMap = new LinkedHashMap<>();

	private DataManagerContext dataManagerContext;

	private PartitionsContext partitionsContext;

	private PeopleDataManager peopleDataManager;

	private final Map<Object, PopulationPartition> keyToPopulationPartitionMap = new LinkedHashMap<>();
	private final Set<Object> safePartitionKeys = Collections.unmodifiableSet(keyToPopulationPartitionMap.keySet());

	/*
	 * Returns the set of keys for population partitions
	 */
	private Set<Object> getKeys() {
		return safePartitionKeys;
	}

	/**
	 * Returns the list of person identifiers in the population partition for the
	 * given key.
	 *
	 * @throws util.errors.ContractException
	 *                           <li>{@link PartitionError.NULL_PARTITION_KEY} if
	 *                           the key is null</li>
	 *                           <li>{@link PartitionError.UNKNOWN_POPULATION_PARTITION_KEY}
	 *                           if the key is unknown</li>
	 *                           </ul>
	 */
	public List<PersonId> getPeople(final Object key) {
		validateKeyExists(key);
		return getPopulationPartition(key).getPeople();
	}

	/**
	 * Returns the list of person identifiers in the population partition for the
	 * given key. A person is included if every label in the label set is equal to
	 * the corresponding label for the person.
	 *
	 * @throws util.errors.ContractException
	 *                           <li>{@link PartitionError.NULL_PARTITION_KEY} if
	 *                           the key is null</li>
	 *                           <li>{@link PartitionError.UNKNOWN_POPULATION_PARTITION_KEY}
	 *                           if the key is unknown</li>
	 *                           <li>{@link PartitionError.NULL_LABEL_SET} if the
	 *                           label set is null</li>
	 *                           <li>{@link PartitionError.INCOMPATIBLE_LABEL_SET}
	 *                           if the label set contains dimensions not contained
	 *                           in the population partition</li>
	 *                           </ul>
	 */
	public List<PersonId> getPeople(final Object key, LabelSet labelSet) {
		validateKeyExists(key);
		PopulationPartition populationPartition = getPopulationPartition(key);
		validateLabelSet(populationPartition, labelSet);
		return populationPartition.getPeople(labelSet);
	}

	/**
	 * Returns the number of people in the population partition for the given key.
	 * <li>{@link PartitionError.NULL_PARTITION_KEY} if the key is null</li></li>
	 * <li>{@link PartitionError.UNKNOWN_POPULATION_PARTITION_KEY} if the key is
	 * unknown</li>
	 * </ul>
	 */
	public int getPersonCount(final Object key) {
		validateKeyExists(key);
		PopulationPartition populationPartition = getPopulationPartition(key);
		return populationPartition.getPeopleCount();
	}

	/**
	 * Returns the number of people in the population partition for the given key
	 * under the given label set. A person is counted if every label in the label
	 * set is equal to the corresponding label for the person.
	 *
	 * @throws util.errors.ContractException
	 *                           <li>{@link PartitionError.NULL_PARTITION_KEY} if
	 *                           the key is null</li>
	 *                           <li>{@link PartitionError.UNKNOWN_POPULATION_PARTITION_KEY}
	 *                           if the key is unknown</li>
	 *                           <li>{@link PartitionError.NULL_LABEL_SET} if the
	 *                           label set is null</li>
	 *                           <li>{@link PartitionError.INCOMPATIBLE_LABEL_SET}
	 *                           if the label set contains dimensions not contained
	 *                           in the population partition</li>
	 *                           </ul>
	 */
	public int getPersonCount(final Object key, LabelSet labelSet) {
		validateKeyExists(key);
		PopulationPartition populationPartition = getPopulationPartition(key);
		validateLabelSet(populationPartition, labelSet);
		return populationPartition.getPeopleCount(labelSet);
	}

	/**
	 * Returns a mapping from LabelSet to Integer whose keys are all the label sets
	 * in the population partition that match the given label set. The values are
	 * the number of people matching that label set. A person is counted if every
	 * label in the label set is equal to the corresponding label for the person.
	 * All values will be positive.
	 *
	 * @throws util.errors.ContractException
	 *                           <li>{@link PartitionError.NULL_PARTITION_KEY} if
	 *                           the key is null</li>
	 *                           <li>{@link PartitionError.UNKNOWN_POPULATION_PARTITION_KEY}
	 *                           if the key is unknown</li>
	 *                           <li>{@link PartitionError.NULL_LABEL_SET} if the
	 *                           label set is null</li>
	 *                           <li>{@link PartitionError.INCOMPATIBLE_LABEL_SET}
	 *                           if the label set contains dimensions not contained
	 *                           in the population partition</li>
	 *                           </ul>
	 */
	public Map<LabelSet, Integer> getPeopleCountMap(final Object key, LabelSet labelSet) {
		validateKeyExists(key);
		PopulationPartition populationPartition = getPopulationPartition(key);
		validateLabelSet(populationPartition, labelSet);
		return populationPartition.getPeopleCountMap(labelSet);
	}

	/**
	 * Returns a randomly selected person from the given population partition using
	 * the given PartitionSampler.
	 *
	 * @throws util.errors.ContractException
	 *                           <li>{@link PartitionError.NULL_PARTITION_KEY} if
	 *                           the key is null</li>
	 *                           <li>{@link PartitionError.UNKNOWN_POPULATION_PARTITION_KEY}
	 *                           if the key is unknown</li>
	 *                           <li>{@link PartitionError.NULL_PARTITION_SAMPLER}
	 *                           if the partition sampler is null</li>
	 *                           <li>{@link PartitionError.INCOMPATIBLE_LABEL_SET}
	 *                           if the partition sampler has a label set containing
	 *                           dimensions not present in the population
	 *                           partition</li>
	 *                           <li>{@link PersonError.UNKNOWN_PERSON_ID} if the
	 *                           partition sampler has an excluded person that does
	 *                           not exist</li>
	 *                           <li>{@link StochasticsError.UNKNOWN_RANDOM_NUMBER_GENERATOR_ID}
	 *                           if the partition sampler has a random number
	 *                           generator id that is unknown</li>
	 */
	public Optional<PersonId> samplePartition(Object key, PartitionSampler partitionSampler) {
		validateKeyExists(key);
		PopulationPartition populationPartition = getPopulationPartition(key);
		validatePartitionSampler(populationPartition, partitionSampler);
		return populationPartition.samplePartition(partitionSampler);
	}

	/**
	 * Returns an optional value by applying the given function to the label set
	 * associated with the person. If the person is not contained in the population
	 * partition the method returns an empty optional. Note that the
	 * labelSetFunction must be consistent with the partition definition used to
	 * create this population partition. No precondition tests will be performed.
	 * 
	 * @throws util.errors.ContractException
	 *                           <li>{@link PartitionError#NULL_PARTITION_KEY} if
	 *                           the key is null</li>
	 *                           <li>{@link PartitionError#UNKNOWN_POPULATION_PARTITION_KEY}
	 *                           if the key is unknown</li>
	 *                           <li>{@link PersonError#UNKNOWN_PERSON_ID} if the
	 *                           person does not exist</li>
	 *                           <li>{@link PartitionError.NULL_LABEL_SET_FUNCTION}
	 *                           if the label set function is null</li>
	 */
	public <T> Optional<T> getPersonValue(Object key, LabelSetFunction<T> labelSetFunction, PersonId personId) {
		validateKeyExists(key);
		validatePersonExists(personId);
		validateLabelSetFunction(labelSetFunction);
		PopulationPartition populationPartition = getPopulationPartition(key);
		return populationPartition.getPersonValue(labelSetFunction, personId);
	}

	/**
	 * Returns true if and only if the person is contained in the population
	 * partition corresponding to the key.
	 * 
	 * @throws util.errors.ContractException
	 *                           <li>{@link PersonError.NULL_PERSON_ID} if the
	 *                           person id is null</li>
	 *                           <li>{@link PersonError.UNKNOWN_PERSON_ID} if the
	 *                           person id is null</li>
	 *                           <li>{@link PartitionError.NULL_PARTITION_KEY} if
	 *                           the key is null</li>
	 *                           <li>{@link PartitionError.UNKNOWN_POPULATION_PARTITION_KEY}
	 *                           if the key is unknown</li>
	 *                           </ul>
	 */
	public boolean contains(final PersonId personId, Object key) {
		validatePersonNotNull(personId);
		validatePersonExists(personId);
		validateKeyExists(key);
		PopulationPartition populationPartition = getPopulationPartition(key);
		return populationPartition.contains(personId);
	}

	/**
	 * Returns true if and only if the person is contained in the population
	 * corresponding to the key. A person is counted if every label in the label set
	 * is equal to the corresponding label for the person.
	 * 
	 * @throws util.errors.ContractException
	 *                           <li>{@link PersonError.NULL_PERSON_ID} if the
	 *                           person id is null</li>
	 *                           <li>{@link PersonError.UNKNOWN_PERSON_ID} if the
	 *                           person id is null</li>
	 *                           <li>{@link PartitionError.NULL_PARTITION_KEY} if
	 *                           the key is null</li>
	 *                           <li>{@link PartitionError.UNKNOWN_POPULATION_PARTITION_KEY}
	 *                           if the key is unknown</li>
	 *                           <li>{@link PartitionError.NULL_LABEL_SET} if the
	 *                           label set is null</li>
	 *                           <li>{@link PartitionError.INCOMPATIBLE_LABEL_SET}
	 *                           if the label contains a dimension not present in
	 *                           the partition</li>
	 *                           </ul>
	 */
	public boolean contains(PersonId personId, LabelSet labelSet, Object key) {
		validatePersonNotNull(personId);
		validatePersonExists(personId);
		validateKeyExists(key);
		PopulationPartition populationPartition = getPopulationPartition(key);
		validateLabelSet(populationPartition, labelSet);

		return populationPartition.contains(personId, labelSet);
	}

	/**
	 * Returns true if and only if a partition is associated with the given key.
	 * Null tolerant.
	 */
	public boolean partitionExists(final Object key) {
		return keyToPopulationPartitionMap.containsKey(key);
	}

	private void validateLabelSet(final PopulationPartition populationPartition, final LabelSet labelSet) {
		if (labelSet == null) {
			throw new ContractException(PartitionError.NULL_LABEL_SET);
		}

		if (!populationPartition.validateLabelSetInfo(labelSet)) {
			throw new ContractException(PartitionError.INCOMPATIBLE_LABEL_SET);
		}
	}

	private void validatePartitionSampler(PopulationPartition populationPartition,
			final PartitionSampler partitionSampler) {
		if (partitionSampler == null) {
			throw new ContractException(PartitionError.NULL_PARTITION_SAMPLER);
		}

		if (partitionSampler.getLabelSet().isPresent()) {
			final LabelSet labelSet = partitionSampler.getLabelSet().get();
			if (!populationPartition.validateLabelSetInfo(labelSet)) {
				throw new ContractException(PartitionError.INCOMPATIBLE_LABEL_SET);
			}
		}

		if (partitionSampler.getExcludedPerson().isPresent()) {
			final PersonId excludedPersonId = partitionSampler.getExcludedPerson().get();
			validatePersonExists(excludedPersonId);
		}

	}

	/*
	 * Precondition : person id is not null
	 */
	private void validatePersonExists(final PersonId personId) {
		if (!peopleDataManager.personExists(personId)) {
			throw new ContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	private void validateLabelSetFunction(LabelSetFunction<?> labelSetFunction) {
		if (labelSetFunction == null) {
			throw new ContractException(PartitionError.NULL_LABEL_SET_FUNCTION);
		}
	}

	private void validatePersonNotNull(final PersonId personId) {
		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
	}

	private void validateKeyExists(final Object key) {
		if (key == null) {
			throw new ContractException(PartitionError.NULL_PARTITION_KEY);
		}

		if (!keyToPopulationPartitionMap.containsKey(key)) {
			throw new ContractException(PartitionError.UNKNOWN_POPULATION_PARTITION_KEY, key);
		}
	}

	@Override
	public void init(DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);
		this.dataManagerContext = dataManagerContext;

		this.partitionsContext = new PartitionsContextImpl(dataManagerContext);

		peopleDataManager = dataManagerContext.getDataManager(PeopleDataManager.class);

		dataManagerContext.subscribe(PersonAdditionEvent.class, this::handlePersonAdditionEvent);

		dataManagerContext.subscribe(PersonRemovalEvent.class, this::handlePersonRemovalEvent);

		reservedEventClasses.add(PersonAdditionEvent.class);
		reservedEventClasses.add(PersonRemovalEvent.class);

		if (dataManagerContext.stateRecordingIsScheduled()) {
			dataManagerContext.subscribeToSimulationClose(this::recordSimulationState);
		}

	}

	private void recordSimulationState(DataManagerContext dataManagerContext) {
		PartitionsPluginData partitionsPluginData = PartitionsPluginData.builder()
				.setRunContinuitySupport(supportRunContinuity).build();
		dataManagerContext.releaseOutput(partitionsPluginData);
	}

	/*
	 * Returns the population partition associated with the given key. Returns null
	 * if the key is not recognized.
	 */
	private PopulationPartition getPopulationPartition(final Object key) {
		return keyToPopulationPartitionMap.get(key);
	}

	private void validatePopulationPartitionDoesNotExist(final Object key) {
		if (keyToPopulationPartitionMap.containsKey(key)) {
			throw new ContractException(PartitionError.DUPLICATE_PARTITION, key);
		}
	}

	/*
	 * Precondition : the key is not null
	 */
	private void validatePopulationPartitionExists(final Object key) {
		if (!keyToPopulationPartitionMap.containsKey(key)) {
			throw new ContractException(PartitionError.UNKNOWN_POPULATION_PARTITION_KEY, key);
		}
	}

	private void validatePopulationPartitionKeyNotNull(final Object key) {
		if (key == null) {
			throw new ContractException(PartitionError.NULL_PARTITION_KEY);
		}
	}

	private void validatePopulationPartitionNotNull(final Partition partition) {
		if (partition == null) {
			throw new ContractException(PartitionError.NULL_PARTITION);
		}
	}

	private void handlePersonAdditionEvent(final DataManagerContext dataManagerContext,
			final PersonAdditionEvent personAdditionEvent) {
		final PersonId personId = personAdditionEvent.personId();
		for (final Object key : getKeys()) {
			final PopulationPartition populationPartition = getPopulationPartition(key);
			populationPartition.attemptPersonAddition(personId);
		}
	}

	private void handlePersonRemovalEvent(final DataManagerContext dataManagerContext,
			final PersonRemovalEvent personRemovalEvent) {

		for (final Object key : getKeys()) {
			final PopulationPartition populationPartition = getPopulationPartition(key);
			populationPartition.attemptPersonRemoval(personRemovalEvent.personId());
		}

	}

	/**
	 * Adds a population partition for the given key and component id. The key must
	 * not duplicate an existing key.
	 *
	 * @throws util.errors.ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PartitionError#NULL_PARTITION} if
	 *                           the partition is null</li>
	 *                           <li>{@linkplain PartitionError#NULL_PARTITION_KEY}
	 *                           if the key is null</li>
	 *                           <li>{@linkplain PartitionError#DUPLICATE_PARTITION}
	 *                           if a partition is currently associated with the
	 *                           key</li>
	 *                           </ul>
	 */
	public void addPartition(final Partition partition, final Object key) {

		validatePopulationPartitionNotNull(partition);
		validatePopulationPartitionKeyNotNull(key);
		validatePopulationPartitionDoesNotExist(key);

		/*
		 * determine the event classes that will trigger refreshes on the partition
		 */
		final Set<Class<? extends Event>> eventClasses = new LinkedHashSet<>();

		final Filter filter = partition.getFilter().orElse(new TrueFilter());
		filter.validate(partitionsContext);
		for (final FilterSensitivity<?> filterSensitivity : filter.getFilterSensitivities()) {
			eventClasses.add(filterSensitivity.getEventClass());
		}

		for (final Labeler labeler : partition.getLabelers()) {
			final Set<LabelerSensitivity<?>> labelerSensitivities = labeler.getLabelerSensitivities();
			for (final LabelerSensitivity<?> labelerSensitivity : labelerSensitivities) {
				eventClasses.add(labelerSensitivity.getEventClass());
			}
		}

		// show that these classes do not include the events that this resolver
		// processes
		for (final Class<? extends Event> reservedEventClass : reservedEventClasses) {
			if (eventClasses.contains(reservedEventClass)) {
				throw new ContractException(PartitionError.RESERVED_PARTITION_TRIGGER, reservedEventClass);
			}
		}

		keyToEventClassesMap.put(key, eventClasses);

		/*
		 * Integrate this into the subscription management maps and subscribe for the
		 * event class if needed
		 */
		for (final Class<? extends Event> eventClass : eventClasses) {
			Set<Object> keys = eventClassToKeyMap.get(eventClass);
			if (keys == null) {
				keys = new LinkedHashSet<>();
				eventClassToKeyMap.put(eventClass, keys);
				dataManagerContext.subscribe(eventClass, this::handleEvent);
			}
			keys.add(key);
		}

		// create the population partition

		PopulationPartition populationPartition;
		if (partition.isDegenerate()) {
			populationPartition = new DegeneratePopulationPartitionImpl(partitionsContext, partition,
					supportRunContinuity);
		} else {
			populationPartition = new PopulationPartitionImpl(partitionsContext, partition, supportRunContinuity);
		}
		keyToPopulationPartitionMap.put(key, populationPartition);

	}

	private void handleEvent(final DataManagerContext dataManagerContext, final Event event) {
		final Set<Object> keys = eventClassToKeyMap.get(event.getClass());
		if (keys != null) {
			for (final Object key : keys) {
				final PopulationPartition populationPartition = getPopulationPartition(key);
				populationPartition.handleEvent(event);
			}
		} else {
			throw new RuntimeException("received unhandled event " + event);
		}
	}

	/**
	 * Removes the population index for the given key if that key is present.
	 * Returns true if and only if the partition was removed.
	 * <li>{@linkplain PartitionError#NULL_PARTITION_KEY} if the key is null</li>
	 * <li>{@linkplain PartitionError#UNKNOWN_POPULATION_PARTITION_KEY} if the key
	 * is unknown</li>
	 * </ul>
	 */
	public void removePartition(final Object key) {

		validatePopulationPartitionKeyNotNull(key);
		validatePopulationPartitionExists(key);
		keyToPopulationPartitionMap.remove(key);

		final Set<Class<? extends Event>> eventClasses = keyToEventClassesMap.get(key);
		for (final Class<? extends Event> eventClass : eventClasses) {
			final Set<Object> keys = eventClassToKeyMap.get(eventClass);
			keys.remove(key);
			if (keys.isEmpty()) {
				eventClassToKeyMap.remove(eventClass);
				dataManagerContext.unsubscribe(eventClass);
			}
		}
	}

	private final class PartitionsContextImpl implements PartitionsContext {
		private final DataManagerContext dataManagerContext;

		public PartitionsContextImpl(DataManagerContext dataManagerContext) {
			this.dataManagerContext = dataManagerContext;
		}

		/**
		 * Returns the data manager from the given class reference
		 * 
		 * @throws util.errors.ContractException
		 *                           <ul>
		 *                           <li>{@linkplain NucleusError#NULL_DATA_MANAGER_CLASS}
		 *                           if data manager class is null</li>
		 *                           <li>{@linkplain NucleusError#AMBIGUOUS_DATA_MANAGER_CLASS}
		 *                           if more than one data manager matches the given
		 *                           class</li>
		 *                           </ul>
		 */
		public <T extends DataManager> T getDataManager(Class<T> dataManagerClass) {
			return dataManagerContext.getDataManager(dataManagerClass);
		}

		/**
		 * Returns the current time in the simulation
		 */
		public double getTime() {
			return dataManagerContext.getTime();
		}
	}

}