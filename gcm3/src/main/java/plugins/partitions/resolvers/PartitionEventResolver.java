package plugins.partitions.resolvers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nucleus.AgentId;
import nucleus.Event;
import nucleus.ResolverContext;
import plugins.partitions.PartitionsPlugin;
import plugins.partitions.datacontainers.PartitionDataManager;
import plugins.partitions.datacontainers.PartitionDataView;
import plugins.partitions.events.PartitionAdditionEvent;
import plugins.partitions.events.PartitionRemovalEvent;
import plugins.partitions.support.DegeneratePopulationPartitionImpl;
import plugins.partitions.support.Filter;
import plugins.partitions.support.FilterSensitivity;
import plugins.partitions.support.Labeler;
import plugins.partitions.support.LabelerSensitivity;
import plugins.partitions.support.Partition;
import plugins.partitions.support.PartitionError;
import plugins.partitions.support.PopulationPartition;
import plugins.partitions.support.PopulationPartitionImpl;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.events.observation.BulkPersonCreationObservationEvent;
import plugins.people.events.observation.PersonCreationObservationEvent;
import plugins.people.events.observation.PersonImminentRemovalObservationEvent;
import plugins.people.support.BulkPersonContructionData;
import plugins.people.support.PersonId;
import util.ContractException;

/**
 *
 * Provides event resolution for the {@linkplain PartitionsPlugin}.
 * <P>
 * Creates, publishes and maintains the {@linkplain PartitionDataView}.
 * </P>
 *
 * <P>
 * Resolves the following events:
 * <ul>
 *
 *
 * <li>{@linkplain PartitionAdditionEvent} <blockquote>Adds the population
 * partition to the partition data view and maintains its content as person
 * details evolve. <BR>
 * <BR>
 *
 * Throws {@link ContractException}
 * <ul>
 * <li>{@linkplain PartitionError#NULL_PARTITION} if the partition is null</li>
 * <li>{@linkplain PartitionError#NULL_PARTITION_KEY} if the key is null</li>
 * <li>{@linkplain PartitionError#DUPLICATE_PARTITION} if a partition is
 * currently associated with the key</li>
 * </ul>
 * </blockquote></li>
 *
 *
 * <li>{@linkplain PartitionRemovalEvent} <blockquote>Removes the partition from
 * the partition data view.<BR>
 * <BR>
 * Throws {@link ContractException}
 * <ul>
 * 
 * <li>{@linkplain PartitionError#NULL_PARTITION_KEY} if the key is null</li>
 * <li>{@linkplain PartitionError#UNKNOWN_POPULATION_PARTITION_KEY} if the key
 * is unknown</li>
 * <li>{@linkplain PartitionError#PARTITION_DELETION_BY_NON_OWNER} if the
 * partition is not owned by the requesting agent</li>
 * </ul>
 * </blockquote></li>
 *
 *
 * <li>{@linkplain PersonCreationObservationEvent} <blockquote>Adds the person
 * to all relevant population partitions after event validation and execution
 * phases are complete. </blockquote></li>
 *
 *
 * <li>{@linkplain BulkPersonCreationObservationEvent} <blockquote>Adds the
 * people to the relevant population partitions after event validation and
 * execution phases are complete. </blockquote></li>
 *
 *
 * <li>{@linkplain PersonImminentRemovalObservationEvent} <blockquote>Removes
 * the person from all population partitions by scheduling the removal for the
 * current time. This allows references and partition memberships to remain long
 * enough for resolvers, agents and reports to have final reference to the
 * person while still associated with any relevant partitions.
 * </blockquote></li>
 *
 * </ul>
 * </p>
 *
 * @author Shawn Hatch
 *
 */

public final class PartitionEventResolver {

	private PartitionDataManager partitionDataManager;

	private PersonDataView personDataView;

	private final Map<Object, Set<Class<? extends Event>>> keyToEventClassesMap = new LinkedHashMap<>();

	private final Set<Class<? extends Event>> reservedEventClasses = new LinkedHashSet<>();

	private final Map<Class<? extends Event>, Set<Object>> eventClassToKeyMap = new LinkedHashMap<>();

	private void handleBulkPersonCreationObservationEvent(final ResolverContext resolverContext, final BulkPersonCreationObservationEvent bulkPersonCreationObservationEvent) {
		if (partitionDataManager.isEmpty()) {
			return;
		}
		final PersonId basePersonId = bulkPersonCreationObservationEvent.getPersonId();
		final int lowId = basePersonId.getValue();
		final BulkPersonContructionData bulkPersonContructionData = bulkPersonCreationObservationEvent.getBulkPersonContructionData();
		int highId = bulkPersonContructionData.getPersonContructionDatas().size();
		highId += lowId;
		final List<PersonId> personIds = new ArrayList<>();
		for (int id = lowId; id < highId; id++) {
			final PersonId boxedPersonId = personDataView.getBoxedPersonId(id);
			personIds.add(boxedPersonId);
		}
		final Set<Object> partitionIds = partitionDataManager.getKeys();
		for (final Object key : partitionIds) {
			final PopulationPartition populationPartition = partitionDataManager.getPopulationPartition(key);
			for (final PersonId personId : personIds) {
				populationPartition.attemptPersonAddition(personId);
			}
		}

	}

	private void handleEvent(final ResolverContext resolverContext, final Event event) {
		final Set<Object> keys = eventClassToKeyMap.get(event.getClass());
		if (keys != null) {
			for (final Object key : keys) {
				final PopulationPartition populationPartition = partitionDataManager.getPopulationPartition(key);
				populationPartition.handleEvent(event);
			}
		} else {
			throw new RuntimeException("received unhandled event " + event);
		}

	}

	private void handlePartitionAdditionEventExecution(final ResolverContext resolverContext, final PartitionAdditionEvent partitionAdditionEvent) {

		final Partition partition = partitionAdditionEvent.getPartition();
		final Object key = partitionAdditionEvent.getKey();

		final AgentId agentId = resolverContext.getCurrentAgentId();

		// determine the event classes that will trigger refreshes on the
		// partition
		final Set<Class<? extends Event>> eventClasses = new LinkedHashSet<>();

		final Filter filter = partition.getFilter().orElse(Filter.allPeople());
		filter.validate(resolverContext.getSafeContext());
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
				resolverContext.throwContractException(PartitionError.RESERVED_PARTITION_TRIGGER, reservedEventClass);
			}
		}

		keyToEventClassesMap.put(key, eventClasses);

		/*
		 * Integrate this into the subscription management maps and subscribe
		 * for the event class if needed
		 */
		for (final Class<? extends Event> eventClass : eventClasses) {
			Set<Object> keys = eventClassToKeyMap.get(eventClass);
			if (keys == null) {
				keys = new LinkedHashSet<>();
				eventClassToKeyMap.put(eventClass, keys);
				resolverContext.subscribeToEventPostPhase(eventClass, this::handleEvent);
			}
			keys.add(key);
		}

		// pass the partition to the partition manager

		PopulationPartition populationPartition;
		if (partition.isDegenerate()) {
			populationPartition = new DegeneratePopulationPartitionImpl(resolverContext.getSafeContext(), partition);
		} else {
			populationPartition = new PopulationPartitionImpl(resolverContext.getSafeContext(), partition);
		}

		partitionDataManager.addPartition(key, agentId, populationPartition);
	}

	private void handlePartitionAdditionEventValidation(final ResolverContext resolverContext, final PartitionAdditionEvent partitionAdditionEvent) {

		final Partition partition = partitionAdditionEvent.getPartition();
		final Object key = partitionAdditionEvent.getKey();

		validatePopulationPartitionNotNull(resolverContext, partition);
		validatePopulationPartitionKeyNotNull(resolverContext, key);
		validatePopulationPartitionDoesNotExist(resolverContext, key);
	}

	private void handlePartitionRemovalEventExecution(final ResolverContext resolverContext, final PartitionRemovalEvent partitionRemovalEvent) {
		final Object key = partitionRemovalEvent.getKey();

		partitionDataManager.removePartition(key);

		final Set<Class<? extends Event>> eventClasses = keyToEventClassesMap.get(key);
		for (final Class<? extends Event> eventClass : eventClasses) {
			final Set<Object> keys = eventClassToKeyMap.get(eventClass);
			keys.remove(key);
			if (keys.isEmpty()) {
				eventClassToKeyMap.remove(eventClass);
				resolverContext.unSubscribeToEvent(eventClass);
			}
		}
	}

	private void handlePartitionRemovalEventValidation(final ResolverContext resolverContext, final PartitionRemovalEvent partitionRemovalEvent) {
		final Object key = partitionRemovalEvent.getKey();
		validatePopulationPartitionKeyNotNull(resolverContext, key);
		validatePopulationPartitionExists(resolverContext, key);
		validatePopulationPartitionIsOwnedByFocalAgent(resolverContext, key);
	}

	private void handlePersonCreationObservationEvent(final ResolverContext resolverContext, final PersonCreationObservationEvent personCreationObservationEvent) {
		final PersonId personId = personCreationObservationEvent.getPersonId();
		for (final Object key : partitionDataManager.getKeys()) {
			final PopulationPartition populationPartition = partitionDataManager.getPopulationPartition(key);
			populationPartition.attemptPersonAddition(personId);
		}
	}

	private void handlePersonImminentRemovalObservationEvent(final ResolverContext resolverContext, final PersonImminentRemovalObservationEvent personImminentRemovalObservationEvent) {
		resolverContext.addPlan((context) -> {
			for (final Object key : partitionDataManager.getKeys()) {
				final PopulationPartition populationPartition = partitionDataManager.getPopulationPartition(key);
				populationPartition.attemptPersonRemoval(personImminentRemovalObservationEvent.getPersonId());
			}
		}, resolverContext.getTime());
	}

	public void init(final ResolverContext resolverContext) {

		personDataView = resolverContext.getDataView(PersonDataView.class).get();
		partitionDataManager = new PartitionDataManager();

		resolverContext.subscribeToEventExecutionPhase(PartitionAdditionEvent.class, this::handlePartitionAdditionEventExecution);
		resolverContext.subscribeToEventValidationPhase(PartitionAdditionEvent.class, this::handlePartitionAdditionEventValidation);

		resolverContext.subscribeToEventExecutionPhase(PartitionRemovalEvent.class, this::handlePartitionRemovalEventExecution);
		resolverContext.subscribeToEventValidationPhase(PartitionRemovalEvent.class, this::handlePartitionRemovalEventValidation);

		resolverContext.subscribeToEventPostPhase(PersonCreationObservationEvent.class, this::handlePersonCreationObservationEvent);

		resolverContext.subscribeToEventPostPhase(BulkPersonCreationObservationEvent.class, this::handleBulkPersonCreationObservationEvent);

		resolverContext.subscribeToEventExecutionPhase(PersonImminentRemovalObservationEvent.class, this::handlePersonImminentRemovalObservationEvent);

		resolverContext.publishDataView(new PartitionDataView(resolverContext, partitionDataManager));

	}

	private void validatePopulationPartitionDoesNotExist(final ResolverContext resolverContext, final Object key) {
		if (partitionDataManager.partitionExists(key)) {
			resolverContext.throwContractException(PartitionError.DUPLICATE_PARTITION, key);
		}
	}

	/*
	 * Precondition : the key is not null
	 */
	private void validatePopulationPartitionExists(final ResolverContext resolverContext, final Object key) {
		if (!partitionDataManager.partitionExists(key)) {
			resolverContext.throwContractException(PartitionError.UNKNOWN_POPULATION_PARTITION_KEY, key);
		}
	}

	/*
	 * Precondition: the key must correspond to an existing partition
	 */
	private void validatePopulationPartitionIsOwnedByFocalAgent(final ResolverContext resolverContext, final Object key) {

		if (!partitionDataManager.getOwningAgentId(key).equals(resolverContext.getCurrentAgentId())) {
			resolverContext.throwContractException(PartitionError.PARTITION_DELETION_BY_NON_OWNER, key);
		}
	}

	private void validatePopulationPartitionKeyNotNull(final ResolverContext resolverContext, final Object key) {
		if (key == null) {
			resolverContext.throwContractException(PartitionError.NULL_PARTITION_KEY);
		}
	}

	private void validatePopulationPartitionNotNull(final ResolverContext resolverContext, final Partition partition) {
		if (partition == null) {
			resolverContext.throwContractException(PartitionError.NULL_PARTITION);
		}
	}

}
