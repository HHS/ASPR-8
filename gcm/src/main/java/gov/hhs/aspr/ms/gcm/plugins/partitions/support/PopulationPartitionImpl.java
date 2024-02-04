package gov.hhs.aspr.ms.gcm.plugins.partitions.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.containers.BasePeopleContainer;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.containers.PeopleContainer;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.filters.Filter;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.filters.TrueFilter;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.RandomNumberGeneratorId;
import gov.hhs.aspr.ms.util.combinatorics.TupleGenerator;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * Primary implementor for {@link PopulationPartition}
 */
public final class PopulationPartitionImpl implements PopulationPartition {

	private class FullKeyIterator implements KeyIterator {

		private final Iterator<Key> iterator = keyToPeopleMap.keySet().iterator();

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public Key next() {
			return iterator.next();
		}

		@Override
		public int size() {
			return keyToPeopleMap.keySet().size();
		}

	}

	private static class Key {

		private final Object[] keys;
		private int hashCode;

		private Key(final int size) {
			keys = new Object[size];
		}

		private Key(final Key key) {
			keys = Arrays.copyOf(key.keys, key.keys.length);
		}

		/*
		 * We are using the older Arrays.equals and Arrays.hashCode since it suffices
		 * for our use case and uses half the runtime of the newer deep versions
		 */
		public void calculateHashCode() {
			hashCode = Arrays.hashCode(keys);
		}

		@Override
		public boolean equals(final Object obj) {

			// We are guaranteed that obj is a non-null Key
			final Key other = (Key) obj;
			if (hashCode == other.hashCode) {
				return Arrays.equals(keys, other.keys);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		public boolean isEmptyKey() {
			for (final Object key : keys) {
				if (key != null) {
					return false;
				}
			}
			return true;
		}

		public boolean isPartialKey() {
			for (final Object key : keys) {
				if (key == null) {
					return true;
				}
			}
			return false;
		}

		@Override
		public String toString() {
			return "Key [keys=" + Arrays.toString(keys) + "]";
		}

	}

	private static interface KeyIterator extends Iterator<Key> {
		public int size();
	}

	private static class LabelCounter {
		int count;
	}

	private static class LabelManager {
		private final Labeler labeler;
		private final Map<Object, LabelCounter> labels = new LinkedHashMap<>();
		private Iterator<Object> iterator;
		private int lastIndexForIteration = -1;
		private Object lastIterationResult;

		public LabelManager(final Labeler labeler) {
			this.labeler = labeler;
		}

		public void addLabel(final Object label) {
			LabelCounter labelCounter = labels.get(label);
			if (labelCounter == null) {
				labelCounter = new LabelCounter();
				labels.put(label, labelCounter);
				if (iterator != null) {
					iterator = null;
					lastIndexForIteration = -1;
				}
			}
			labelCounter.count++;
		}

		/*
		 * The client must invoke this method with the ordered inputs
		 * 0,1,2...getLabelCount()-1 and may repeat that order continuously.
		 *
		 */
		public Object getLabel(final int index) {
			if (lastIndexForIteration == index) {
				return lastIterationResult;
			}

			if ((iterator == null) || !iterator.hasNext()) {
				iterator = labels.keySet().iterator();
			}

			lastIndexForIteration = index;
			lastIterationResult = iterator.next();

			return lastIterationResult;
		}

		public int getLabelCount() {
			return labels.size();
		}

		public void removeLabel(final Object label) {
			final LabelCounter labelCounter = labels.get(label);
			labelCounter.count--;
			if (labelCounter.count == 0) {
				labels.remove(label);
				if (iterator != null) {
					iterator = null;
					lastIndexForIteration = -1;
				}
			}

		}

	}

	private class PartialKeyIterator implements KeyIterator {
		private final TupleGenerator tupleGenerator;
		private int index;
		private final Key baseKey;
		private Key nextKey;
		int[] tuple;
		int dimensionCount;
		int[] keyIndexes;

		public PartialKeyIterator(final Key partialKey) {

			dimensionCount = 0;
			for (int i = 0; i < keySize; i++) {
				if (partialKey.keys[i] == null) {
					dimensionCount++;
				}
			}
			tuple = new int[dimensionCount];
			keyIndexes = new int[dimensionCount];
			int index = 0;
			for (int i = 0; i < keySize; i++) {
				if (partialKey.keys[i] == null) {
					keyIndexes[index++] = i;
				}
			}

			final TupleGenerator.Builder builder = TupleGenerator.builder();
			for (int i = 0; i < dimensionCount; i++) {
				final int labelIndex = keyIndexes[i];
				final LabelManager labelManager = labelManagers[labelIndex];
				builder.addDimension(labelManager.getLabelCount());
			}
			tupleGenerator = builder.build();

			// baseKey = new Key(partialKey);
			baseKey = partialKey;
			calculateNextKey();
		}

		private void calculateNextKey() {
			if (index >= tupleGenerator.size()) {
				nextKey = null;
			} else {
				while (index < tupleGenerator.size()) {
					tupleGenerator.fillTuple(index++, tuple);
					for (int j = 0; j < dimensionCount; j++) {
						final LabelManager labelManager = labelManagers[keyIndexes[j]];
						// unsafe mutation calculate next key
						baseKey.keys[keyIndexes[j]] = labelManager.getLabel(tuple[j]);
					}
					baseKey.calculateHashCode();
					nextKey = keyMap.get(baseKey);
					if (nextKey != null) {
						break;
					}
				}
			}
		}

		@Override
		public boolean hasNext() {
			return nextKey != null;
		}

		@Override
		public Key next() {
			if (nextKey == null) {
				throw new NoSuchElementException();
			}
			final Key result = nextKey;
			calculateNextKey();
			return result;
		}

		@Override
		public int size() {
			return tupleGenerator.size();
		}

	}

	private final int keySize;

	private final Map<Key, PeopleContainer> keyToPeopleMap = new LinkedHashMap<>();

	private List<Key> personToKeyMap;

	private BitSet personMembership;

	private final boolean retainPersonKeys;

	private final Map<Key, Key> keyMap = new LinkedHashMap<>();

	private final Map<Key, LabelSet> labelSetInfoMap = new LinkedHashMap<>();

	private final LabelManager[] labelManagers;

	private final Map<Object, Integer> labelerIds = new LinkedHashMap<>();

	private final Filter filter;

	// Returns true if and only if the person is contained in this filtered
	// population partition after the evaluation of the person against the
	// filter.
	// This will force the addition or removal of the person from the
	// corresponding
	// partition cell.

	private int personCount;

	private final StochasticsDataManager stochasticsDataManager;

	private final boolean supportRunContinuity;
	private final PartitionsContext partitionsContext;
	private final Map<Class<? extends Event>, List<FilterSensitivity<? extends Event>>> eventClassToFilterSensitivityMap = new LinkedHashMap<>();
	private final Map<Class<? extends Event>, List<LabelerSensitivity<? extends Event>>> eventClassToLabelerSensitivityMap = new LinkedHashMap<>();

	private final Map<LabelerSensitivity<? extends Event>, Labeler> labelerSensitivityToLabelerMap = new LinkedHashMap<>();

	// Guard for both weights array and weightedKeys array
	private boolean weightsAreLocked;

	private double[] weights;

	private Key[] weightedKeys;

	private final PeopleDataManager peopleDataManager;

	/**
	 * Constructs a PopulationPartitionImpl
	 * 
	 * @throws RuntimeException
	 *                          <ul>
	 *                          <li>if context is null</li>
	 *                          <li>if partition is null</li>
	 *                          <li>if the partition contains labelers</li>
	 *                          </ul>
	 */
	public PopulationPartitionImpl(final PartitionsContext partitionsContext, final Partition partition,
			boolean supportRunContinuity) {
		this.supportRunContinuity = supportRunContinuity;
		this.partitionsContext = partitionsContext;

		retainPersonKeys = partition.retainPersonKeys();
		peopleDataManager = partitionsContext.getDataManager(PeopleDataManager.class);

		if (retainPersonKeys) {
			personToKeyMap = new ArrayList<>(peopleDataManager.getPersonIdLimit());
		} else {
			personMembership = new BitSet(peopleDataManager.getPersonIdLimit());
		}

		filter = partition.getFilter().orElse(new TrueFilter());
		for (final FilterSensitivity<? extends Event> filterSensitivity : filter.getFilterSensitivities()) {
			List<FilterSensitivity<? extends Event>> list = eventClassToFilterSensitivityMap
					.get(filterSensitivity.getEventClass());
			if (list == null) {
				list = new ArrayList<>();
				eventClassToFilterSensitivityMap.put(filterSensitivity.getEventClass(), list);
			}
			list.add(filterSensitivity);
		}
		final List<Labeler> labelers = new ArrayList<>(partition.getLabelers());
		for (final Labeler labeler : labelers) {
			final Set<LabelerSensitivity<?>> labelerSensitivities = labeler.getLabelerSensitivities();
			for (final LabelerSensitivity<?> labelerSensitivity : labelerSensitivities) {
				final Class<? extends Event> eventClass = labelerSensitivity.getEventClass();
				List<LabelerSensitivity<? extends Event>> list = eventClassToLabelerSensitivityMap.get(eventClass);
				if (list == null) {
					list = new ArrayList<>();
					eventClassToLabelerSensitivityMap.put(eventClass, list);
				}
				list.add(labelerSensitivity);
				labelerSensitivityToLabelerMap.put(labelerSensitivity, labeler);
			}
		}
		int maxLabelerSensitivityCount = 0;
		for (List<LabelerSensitivity<? extends Event>> list : eventClassToLabelerSensitivityMap.values()) {
			maxLabelerSensitivityCount = FastMath.max(maxLabelerSensitivityCount, list.size());
		}
		labelerSensitivities = new LabelerSensitivity<?>[maxLabelerSensitivityCount];

		stochasticsDataManager = partitionsContext.getDataManager(StochasticsDataManager.class);

		keySize = labelers.size();
		tempKeyForLabelSets = new Key(keySize);
		tempKeyForPeople = new Key(keySize);

		labelManagers = new LabelManager[keySize];
		for (int i = 0; i < keySize; i++) {
			final Labeler labeler = labelers.get(i);
			labelerIds.put(labeler.getId(), i);
			labelManagers[i] = new LabelManager(labeler);
		}

		final int personIdLimit = peopleDataManager.getPersonIdLimit();
		for (int i = 0; i < personIdLimit; i++) {
			if (peopleDataManager.personIndexExists(i)) {
				final PersonId personId = peopleDataManager.getBoxedPersonId(i).get();
				if (filter.evaluate(partitionsContext, personId)) {
					/*
					 * By contract, we know that the person id should not already be a member of
					 * this container
					 */
					addPerson(personId);
				}
			}
		}

	}

	/*
	 * Preconditions: The personId is not null, is not currently a member and passes
	 * the filter
	 */
	private void addPerson(final PersonId personId) {

		if (retainPersonKeys) {
			while (personId.getValue() >= personToKeyMap.size()) {
				personToKeyMap.add(null);
			}
		}

		final Key key = tempKeyForPeople;
		final int n = labelManagers.length;
		for (int i = 0; i < n; i++) {
			final LabelManager labelManager = labelManagers[i];
			final Object label = labelManager.labeler.getCurrentLabel(partitionsContext, personId);
			// unsafe mutation add person
			key.keys[i] = label;
		}
		key.calculateHashCode();
		// key is no longer mutated

		Key cleanedKey = keyMap.get(key);
		if (cleanedKey == null) {
			// key construction -- for add person
			cleanedKey = new Key(key);
			cleanedKey.calculateHashCode();
			keyMap.put(cleanedKey, cleanedKey);
			final BasePeopleContainer basePeopleContainer = new BasePeopleContainer(partitionsContext,
					supportRunContinuity);
			keyToPeopleMap.put(cleanedKey, basePeopleContainer);
			final LabelSet labelSet = getLabelSet(cleanedKey);
			labelSetInfoMap.put(cleanedKey, labelSet);
		}

		for (int i = 0; i < keySize; i++) {
			final LabelManager labelManager = labelManagers[i];
			labelManager.addLabel(cleanedKey.keys[i]);
		}

		if (retainPersonKeys) {
			personToKeyMap.set(personId.getValue(), cleanedKey);
		} else {
			personMembership.set(personId.getValue());
		}

		keyToPeopleMap.get(cleanedKey).unsafeAdd(personId);
		personCount++;
	}

	/*
	 * Allocates the weights array to the given size or 50% larger than the current
	 * size, whichever is largest. Size must be non-negative
	 */
	private void allocateWeights(final int size) {
		if (weights == null) {
			weights = new double[size];
			weightedKeys = new Key[size];
		}
		if (weights.length < size) {
			final int newSize = Math.max(size, weights.length + (weights.length / 2));
			weights = new double[newSize];
			weightedKeys = new Key[newSize];
		}
	}

	private void aquireWeightsLock() {
		if (weightsAreLocked) {
			throw new ContractException(NucleusError.ACCESS_VIOLATION,
					"cannot access weighted sampling during the execution of a previous weighted sampling");
		}
		weightsAreLocked = true;
	}

	private boolean eventHandlingLocked;

	private void aquireEventHandlingLock() {
		if (eventHandlingLocked) {
			throw new ContractException(NucleusError.ACCESS_VIOLATION,
					"cannot access event handling during the execution of an event");
		}
		eventHandlingLocked = true;
	}

	/**
	 * Precondition : the person id is not null.
	 */
	@Override
	public void attemptPersonAddition(final PersonId personId) {
		if (filter.evaluate(partitionsContext, personId)) {
			/*
			 * By contract, we know that the person id should not already be a member of
			 * this container
			 */
			addPerson(personId);
		}
	}

	/**
	 * Precondition: Person must exist
	 */
	@Override
	public void attemptPersonRemoval(final PersonId personId) {

		final Key key = getKeyForPerson(personId);

		if (key == null) {
			return;
		}

		final PeopleContainer peopleContainer = keyToPeopleMap.get(key);
		final boolean removed = peopleContainer.remove(personId);
		if (removed) {
			if (peopleContainer.size() == 0) {
				keyToPeopleMap.remove(key);
				keyMap.remove(key);
				labelSetInfoMap.remove(key);
			}
			for (int i = 0; i < keySize; i++) {
				final LabelManager labelManager = labelManagers[i];
				labelManager.removeLabel(key.keys[i]);
			}
			if (retainPersonKeys) {
				personToKeyMap.set(personId.getValue(), null);
			} else {
				personMembership.set(personId.getValue(), false);
			}
			personCount--;
		}
	}

	@Override
	public boolean contains(final PersonId personId) {
		int id = personId.getValue();
		if (retainPersonKeys) {
			if (personToKeyMap.size() <= id) {
				return false;
			}
			return personToKeyMap.get(id) != null;
		} else {
			return personMembership.get(id);
		}
	}

	@Override
	public boolean contains(final PersonId personId, final LabelSet labelSet) {

		final Key key = getKeyForPerson(personId);
		if (key == null) {
			return false;
		}

		for (final Object dimension : labelSet.getDimensions()) {
			final Integer index = labelerIds.get(dimension);
			if (index == null) {
				return false;
			}
			final Object label = labelSet.getLabel(dimension).orElse(null);
			if (!key.keys[index].equals(label)) {
				return false;
			}
		}
		return true;
	}

	/*
	 * Returns the index in the weights array that is the first to meet or exceed
	 * the target value. Assumes a strictly increasing set of values for indices 0
	 * through keyCount. Decreasing values are strictly prohibited. Consecutive
	 * equal values may return an ambiguous result. The target value must not exceed
	 * weights[peopleCount].
	 *
	 */
	private int findTargetIndex(final double targetValue, final int keyCount) {
		int low = 0;
		int high = keyCount - 1;

		while (low <= high) {
			final int mid = (low + high) >>> 1;
			final double midVal = weights[mid];
			if (midVal < targetValue) {
				low = mid + 1;
			} else if (midVal > targetValue) {
				high = mid - 1;
			} else {
				return mid;
			}
		}
		return low;
	}

	private Key tempKeyForLabelSets;
	private Key tempKeyForPeople;

	private double getDefaultWeight(final PartitionsContext partitionsContext, final LabelSet labelSet) {
		return 1;
	}

	private Key getKeyForLabelSet(final LabelSet labelSet) {

		final Key key = tempKeyForLabelSets;

		for (int i = 0; i < keySize; i++) {
			final Object labelerId = labelManagers[i].labeler.getId();
			final Object label = labelSet.getLabel(labelerId).orElse(null);
			// unsafe mutation get key
			key.keys[i] = label;
		}
		key.calculateHashCode();
		// key is no longer mutated

		return key;
	}

	/*
	 * Returns the key for the personId, if the person is already a member of this
	 * partition. Otherwise, return null.
	 */
	private Key getKeyForPerson(final PersonId personId) {

		if (personId == null) {
			return null;
		}

		if (retainPersonKeys) {

			if (personToKeyMap.size() <= personId.getValue()) {
				return null;
			}
			return personToKeyMap.get(personId.getValue());

		} else {
			if (!personMembership.get(personId.getValue())) {
				return null;
			}

			Key key = tempKeyForPeople;
			final int n = labelManagers.length;
			for (int i = 0; i < n; i++) {
				final LabelManager labelManager = labelManagers[i];
				final Object label = labelManager.labeler.getCurrentLabel(partitionsContext, personId);
				// unsafe mutation add person
				key.keys[i] = label;
			}
			key.calculateHashCode();
			// key is no longer mutated

			// Since the person is known to be a member, we can just return the
			// cleaned key
			key = keyMap.get(key);
			return key;
		}

	}

	private KeyIterator getKeyIterator(final LabelSet labelSet) {
		final Key key = getKeyForLabelSet(labelSet);
		if (key.isEmptyKey()) {
			return new FullKeyIterator();
		}
		return new PartialKeyIterator(key);
	}

	private LabelSet getLabelSet(final Key key) {
		final LabelSet.Builder builder = LabelSet.builder();
		for (int i = 0; i < keySize; i++) {
			final Object labelerId = labelManagers[i].labeler.getId();
			final Object label = key.keys[i];
			builder.setLabel(labelerId, label);
		}
		return builder.build();
	}

	@Override
	public List<PersonId> getPeople() {
		List<PersonId> result = new ArrayList<>(personCount);
		if (retainPersonKeys) {
			int n = personToKeyMap.size();
			for (int i = 0; i < n; i++) {
				if (personToKeyMap.get(i) != null) {
					result.add(peopleDataManager.getBoxedPersonId(i).get());
				}
			}
		} else {
			int n = peopleDataManager.getPersonIdLimit();
			for (int i = 0; i < n; i++) {
				if (personMembership.get(i)) {
					result.add(peopleDataManager.getBoxedPersonId(i).get());
				}
			}
		}
		return result;
	}

	/**
	 * Precondition: the population partition query must match the population
	 * partition definition
	 */
	@Override
	public List<PersonId> getPeople(final LabelSet labelSet) {
		if (isEmpty()) {
			return new ArrayList<>();
		}

		final Key key = getKeyForLabelSet(labelSet);

		if (key.isPartialKey()) {
			final PartialKeyIterator partialKeyIterator = new PartialKeyIterator(key);

			final List<PersonId> result = new ArrayList<>();
			while (partialKeyIterator.hasNext()) {
				final Key fullKey = partialKeyIterator.next();
				final PeopleContainer peopleContainer = keyToPeopleMap.get(fullKey);
				result.addAll(peopleContainer.getPeople());
			}
			return result;
		} else {
			final PeopleContainer peopleContainer = keyToPeopleMap.get(key);

			if (peopleContainer == null) {
				return new ArrayList<>();
			}
			return peopleContainer.getPeople();
		}

	}

	@Override
	public int getPeopleCount() {
		return personCount;
	}

	@Override
	public Map<LabelSet, Integer> getPeopleCountMap(LabelSet labelSet) {
		Map<LabelSet, Integer> result = new LinkedHashMap<>();
		if (!isEmpty()) {

			final Key key = getKeyForLabelSet(labelSet);

			if (key.isPartialKey()) {
				final PartialKeyIterator partialKeyIterator = new PartialKeyIterator(key);

				while (partialKeyIterator.hasNext()) {
					final Key fullKey = partialKeyIterator.next();
					final PeopleContainer peopleContainer = keyToPeopleMap.get(fullKey);
					if (peopleContainer != null) {
						result.put(getLabelSet(fullKey), peopleContainer.size());
					}
				}
			} else {
				final PeopleContainer peopleContainer = keyToPeopleMap.get(key);
				if (peopleContainer != null) {
					result.put(getLabelSet(key), peopleContainer.size());
				}
			}
		}
		return result;
	}

	@Override
	public int getPeopleCount(final LabelSet labelSet) {
		if (isEmpty()) {
			return 0;
		}
		final Key key = getKeyForLabelSet(labelSet);

		if (key.isPartialKey()) {
			final PartialKeyIterator partialKeyIterator = new PartialKeyIterator(key);
			int result = 0;
			while (partialKeyIterator.hasNext()) {
				final Key fullKey = partialKeyIterator.next();
				final PeopleContainer peopleContainer = keyToPeopleMap.get(fullKey);
				if (peopleContainer != null) {
					result += peopleContainer.size();
				}
			}
			return result;
		} else {
			final PeopleContainer peopleContainer = keyToPeopleMap.get(key);
			if (peopleContainer == null) {
				return 0;
			}
			return peopleContainer.size();
		}
	}

	private LabelerSensitivity<? extends Event>[] labelerSensitivities;

	@Override
	public void handleEvent(final Event event) {

		PersonId personId = null;

		// can the filter state have possibly changed?
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
		final boolean filterSensitivityFound = personId != null;

		// determine the sensitivity of the labelers
		final List<LabelerSensitivity<? extends Event>> eventlabelerSensitivities = eventClassToLabelerSensitivityMap
				.get(event.getClass());

		int labelerSensitivityCount = 0;
		if (eventlabelerSensitivities != null) {

			for (final LabelerSensitivity<? extends Event> labelerSensitivity : eventlabelerSensitivities) {
				final Optional<PersonId> optional = labelerSensitivity.getPersonId(event);
				if (optional.isPresent()) {
					labelerSensitivities[labelerSensitivityCount++] = labelerSensitivity;
					if (personId == null) {
						personId = optional.get();
					}
				}
			}
		}

		/*
		 * If neither the filter nor the labelers are sensitive to the event, then the
		 * person id will be null and we know that the person's membership in this
		 * partition and possible cell assignment will not have changed.
		 */
		if (personId == null) {
			return;
		}

		aquireEventHandlingLock();

		/*
		 * At this point we must have found the person. We must determine the key
		 * associated with their current place in this population partition.
		 */
		Key currentKeyForPerson = null;

		if (retainPersonKeys) {
			if (personId.getValue() < personToKeyMap.size()) {
				currentKeyForPerson = personToKeyMap.get(personId.getValue());
			}
		} else {
			if (personMembership.get(personId.getValue())) {
				currentKeyForPerson = tempKeyForPeople;
				final int n = labelManagers.length;
				for (int i = 0; i < n; i++) {
					final LabelManager labelManager = labelManagers[i];
					final Object label = labelManager.labeler.getCurrentLabel(partitionsContext, personId);
					// unsafe mutation add person
					currentKeyForPerson.keys[i] = label;
				}
				for (int i = 0; i < labelerSensitivityCount; i++) {
					LabelerSensitivity<? extends Event> labelerSensitivity = labelerSensitivities[i];
					final Labeler labeler = labelerSensitivityToLabelerMap.get(labelerSensitivity);
					final Object pastLabel = labeler.getPastLabel(partitionsContext, event);
					final Object labelerId = labeler.getId();
					final int dimensionIndex = labelerIds.get(labelerId);
					// unsafe mutation
					currentKeyForPerson.keys[dimensionIndex] = pastLabel;
				}
				currentKeyForPerson.calculateHashCode();
				currentKeyForPerson = keyMap.get(currentKeyForPerson);

				PeopleContainer peopleContainer = keyToPeopleMap.get(currentKeyForPerson);
				if (!peopleContainer.contains(personId)) {

					Key actualKey = null;
					for (Key key : keyToPeopleMap.keySet()) {
						peopleContainer = keyToPeopleMap.get(key);
						if (peopleContainer.contains(personId)) {
							actualKey = key;
							break;
						}
					}

					StringBuilder sb = new StringBuilder();
					sb.append("[");
					boolean firstElement = true;
					for (int i = 0; i < keySize; i++) {
						Object calculatedValue = currentKeyForPerson.keys[i];
						Object actualValue = actualKey.keys[i];
						if (!calculatedValue.equals(actualValue)) {
							if (firstElement) {
								firstElement = false;
							} else {
								sb.append(",");
							}
							Object labelerId = labelManagers[i].labeler.getId();
							sb.append("(");
							sb.append("labelerId=");
							sb.append(labelerId);
							sb.append(" calculated value ='");
							sb.append(calculatedValue);
							sb.append("' actual value ='");
							sb.append(actualValue);
							sb.append("')");
						}
					}
					sb.append("]");
					throw new ContractException(PartitionError.PAST_LABEL_FAILURE, sb.toString());
				}
			}
		}

		/*
		 * If either the person is new to this partition or the the person needs to be
		 * re-evaluated by the filter, then we will have the filter evaluate the person.
		 */
		final boolean personIsCurrentlyInPartition = currentKeyForPerson != null;

		// determine whether the person should be in the partition
		boolean personShouldBeInPartition;
		if (filterSensitivityFound) {
			personShouldBeInPartition = filter.evaluate(partitionsContext, personId);
		} else {
			personShouldBeInPartition = personIsCurrentlyInPartition;
		}

		if (personShouldBeInPartition) {
			if (personIsCurrentlyInPartition) {
				/*
				 * does their key need to be updated? if so, calculate the new key and move the
				 * person, updating labeler managers as well
				 */
				// personShouldBeInPartition == true
				// personIsCurrentlyInPartition == true
				if (labelerSensitivityCount > 0) {
					// key construction -- (copy) handle event
					final Key newKey = new Key(currentKeyForPerson);
					for (int i = 0; i < labelerSensitivityCount; i++) {
						LabelerSensitivity<? extends Event> labelerSensitivity = labelerSensitivities[i];
						// for (final LabelerSensitivity<? extends Event>
						// labelerSensitivity : labelerSensitivities) {
						final Labeler labeler = labelerSensitivityToLabelerMap.get(labelerSensitivity);
						final Object newLabel = labeler.getCurrentLabel(partitionsContext, personId);
						final Object labelerId = labeler.getId();
						final int dimensionIndex = labelerIds.get(labelerId);
						final LabelManager labelManager = labelManagers[dimensionIndex];
						labelManager.removeLabel(newKey.keys[dimensionIndex]);
						labelManager.addLabel(newLabel);
						// unsafe mutation
						newKey.keys[dimensionIndex] = newLabel;
					}
					newKey.calculateHashCode();
					move(currentKeyForPerson, newKey, personId);
				} else {
					/*
					 * Nothing to do
					 */
				}
			} else {
				// personShouldBeInPartition == true
				// personIsCurrentlyInPartition == false

				/*
				 * Generate a full key for the person and add them into the partition cell and
				 * then exit much like an add person.
				 * 
				 * We know that the person is not currently a member of this population
				 * partition and thus it is safe to add them.
				 * 
				 */
				addPerson(personId);
			}
		} else {
			if (personIsCurrentlyInPartition) {
				// personShouldBeInPartition == false
				// personIsCurrentlyInPartition == true

				/*
				 * remove the person
				 */
				if (retainPersonKeys) {
					personToKeyMap.set(personId.getValue(), null);
				} else {
					personMembership.set(personId.getValue(), false);
				}

				final PeopleContainer peopleContainer = keyToPeopleMap.get(currentKeyForPerson);
				peopleContainer.remove(personId);

				if (peopleContainer.size() == 0) {
					keyToPeopleMap.remove(currentKeyForPerson);
					keyMap.remove(currentKeyForPerson);
					labelSetInfoMap.remove(currentKeyForPerson);
				}
				for (int i = 0; i < keySize; i++) {
					final LabelManager labelManager = labelManagers[i];
					labelManager.removeLabel(currentKeyForPerson.keys[i]);
				}

				personCount--;

			} else {
				// personShouldBeInPartition == false
				// personIsCurrentlyInPartition == false

				/* there is nothing to do */
			}
		}
		releaseEventHandlingLock();
	}

	private boolean isEmpty() {
		return personCount == 0;
	}

	private void move(final Key currentKey, final Key newKey, final PersonId personId) {

		if (currentKey.equals(newKey)) {
			return;
		}

		Key cleanedNewKey = keyMap.get(newKey);
		if (cleanedNewKey == null) {
			cleanedNewKey = newKey;
			keyMap.put(cleanedNewKey, cleanedNewKey);
			keyToPeopleMap.put(cleanedNewKey, new BasePeopleContainer(partitionsContext, supportRunContinuity));
			final LabelSet labelSet = getLabelSet(cleanedNewKey);
			labelSetInfoMap.put(cleanedNewKey, labelSet);
		}

		final PeopleContainer peopleContainer = keyToPeopleMap.get(currentKey);
		peopleContainer.remove(personId);
		if (peopleContainer.size() == 0) {
			keyToPeopleMap.remove(currentKey);
			keyMap.remove(currentKey);
			labelSetInfoMap.remove(currentKey);
		}
		/*
		 * We use unsafe add since we know that the person id is not already a member of
		 * the people container associated with the new key as it is not equal to the
		 * old key
		 */
		keyToPeopleMap.get(cleanedNewKey).unsafeAdd(personId);

		if (retainPersonKeys) {
			personToKeyMap.set(personId.getValue(), cleanedNewKey);
		}
	}

	private void releaseWeightsLock() {
		if (!weightsAreLocked) {

			throw new RuntimeException("cannot release sample locking when lock not present");
		}
		weightsAreLocked = false;
	}

	private void releaseEventHandlingLock() {
		if (!eventHandlingLocked) {
			throw new ContractException(NucleusError.ACCESS_VIOLATION,
					"cannot release event handling lock when the lock is not present");
		}
		eventHandlingLocked = false;
	}

	/**
	 * Returns a randomly chosen person identifier from the partition consistent
	 * with the partition sampler info. Note that the sampler must be consistent
	 * with the partition definition used to create this population partition. No
	 * precondition tests will be performed.
	 */
	@Override
	public Optional<PersonId> samplePartition(final PartitionSampler partitionSampler) {
		if (isEmpty()) {
			return Optional.empty();
		}

		RandomGenerator randomGenerator;
		final RandomNumberGeneratorId randomNumberGeneratorId = partitionSampler.getRandomNumberGeneratorId()
				.orElse(null);
		if (randomNumberGeneratorId == null) {
			randomGenerator = stochasticsDataManager.getRandomGenerator();
		} else {
			randomGenerator = stochasticsDataManager.getRandomGeneratorFromId(randomNumberGeneratorId);
		}

		final PersonId excludedPersonId = partitionSampler.getExcludedPerson().orElse(null);

		final LabelSet labelSet = partitionSampler.getLabelSet().orElse(LabelSet.builder().build());

		final LabelSetWeightingFunction labelSetWeightingFunction = partitionSampler.getLabelSetWeightingFunction()
				.orElse(this::getDefaultWeight);

		Key selectedKey = null;

		final Key keyForExcludedPersonId = getKeyForPerson(excludedPersonId);
		KeyIterator keyIterator;

		aquireWeightsLock();

		try {
			keyIterator = getKeyIterator(labelSet);
			allocateWeights(keyIterator.size());
			/*
			 * Initialize the sum of the weights to zero and set the index in the weights
			 * and weightedKeys to zero.
			 */
			double sum = 0;
			int weightsLength = 0;
			while (keyIterator.hasNext()) {
				final Key fullKey = keyIterator.next();
				final LabelSet fullLableSet = labelSetInfoMap.get(fullKey);
				final PeopleContainer peopleContainer = keyToPeopleMap.get(fullKey);
				double weight = labelSetWeightingFunction.getWeight(partitionsContext, fullLableSet);
				if (fullKey != keyForExcludedPersonId) {
					weight *= peopleContainer.size();
				} else {
					weight *= (peopleContainer.size() - 1);
				}

				if (!Double.isFinite(weight) || (weight < 0)) {
					throw new ContractException(PartitionError.MALFORMED_PARTITION_SAMPLE_WEIGHTING_FUNCTION);

				}
				/*
				 * Keys having a zero weight are rejected for selection
				 */
				if (weight > 0) {
					sum += weight;
					weights[weightsLength] = sum;
					weightedKeys[weightsLength] = fullKey;
					weightsLength++;
				}

			}

			/*
			 * If at least one identifierKey was accepted for selection, then we attempt a
			 * random selection.
			 */
			if (weightsLength > 0) {
				/*
				 * Although the individual weights may have been finite, if the sum of those
				 * weights is not finite no legitimate selection can be made
				 */
				if (!Double.isFinite(sum)) {
					throw new ContractException(PartitionError.MALFORMED_PARTITION_SAMPLE_WEIGHTING_FUNCTION);
				}

				final double targetValue = randomGenerator.nextDouble() * sum;
				final int targetIndex = findTargetIndex(targetValue, weightsLength);
				selectedKey = weightedKeys[targetIndex];
			}
		} finally {
			releaseWeightsLock();
		}

		if (selectedKey == null) {
			return Optional.empty();
		}

		/*
		 * We know that the selected key will correspond to a non-empty people container
		 * that includes at least one person who is not the excluded person. This is due
		 * to 1) People containers that become empty are removed and 2) the key
		 * selection algorithm above will adjust the weight of a key to zero if the
		 * corresponding container contains only the excluded person. Zero weighted keys
		 * are ignored and thus the selected key cannot be associated with the excluded
		 * person alone. Therefore, the person selection process will eventually
		 * terminate.
		 */
		final PeopleContainer peopleContainer = keyToPeopleMap.get(selectedKey);
		PersonId selectedPerson = null;
		if (excludedPersonId == null) {
			selectedPerson = peopleContainer.getRandomPersonId(randomGenerator);
		} else {
			while (true) {
				selectedPerson = peopleContainer.getRandomPersonId(randomGenerator);
				if (!selectedPerson.equals(excludedPersonId)) {
					break;
				}
			}
		}
		return Optional.ofNullable(selectedPerson);
	}

	@Override
	public boolean validateLabelSetInfo(final LabelSet labelSet) {
		for (final Object dimension : labelSet.getDimensions()) {
			if (!labelerIds.containsKey(dimension)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public <T> Optional<T> getPersonValue(LabelSetFunction<T> labelSetFunction, PersonId personId) {
		if (isEmpty()) {
			return Optional.empty();
		}
		Key keyForPerson = getKeyForPerson(personId);
		if (keyForPerson == null) {
			return Optional.empty();
		}
		LabelSet labelSet = labelSetInfoMap.get(keyForPerson);
		T value = labelSetFunction.getValue(partitionsContext, labelSet);
		return Optional.of(value);
	}

}