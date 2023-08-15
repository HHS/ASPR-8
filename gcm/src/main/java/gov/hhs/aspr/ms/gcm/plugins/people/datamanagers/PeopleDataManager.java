package gov.hhs.aspr.ms.gcm.plugins.people.datamanagers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import gov.hhs.aspr.ms.gcm.nucleus.DataManager;
import gov.hhs.aspr.ms.gcm.nucleus.DataManagerContext;
import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.nucleus.EventFilter;
import gov.hhs.aspr.ms.gcm.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.plugins.people.events.PersonAdditionEvent;
import gov.hhs.aspr.ms.gcm.plugins.people.events.PersonImminentAdditionEvent;
import gov.hhs.aspr.ms.gcm.plugins.people.events.PersonImminentRemovalEvent;
import gov.hhs.aspr.ms.gcm.plugins.people.events.PersonRemovalEvent;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonConstructionData;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonRange;
import util.errors.ContractException;

/**
 * Mutable data manager for people.
 *
 *
 */
public final class PeopleDataManager extends DataManager {

	private static class PopulationRecord {
		private int populationCount;
		private double assignmentTime;

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("PopulationRecord [populationCount=");
			builder.append(populationCount);
			builder.append(", assignmentTime=");
			builder.append(assignmentTime);
			builder.append("]");
			return builder.toString();
		}

	}

	private static record PersonAdditionMutationEvent(PersonId personId, PersonConstructionData personConstructionData)
			implements Event {
	}

	private static record PersonRemovalMutationEvent(PersonId personId) implements Event {
	}

	private final PeoplePluginData peoplePluginData;

	/*
	 * We keep the person records in a list rather than a map so that we can
	 * retrieve a person record by index (personId).
	 */
	private List<PersonId> personIds = new ArrayList<>();

	private DataManagerContext dataManagerContext;

	private final PopulationRecord globalPopulationRecord = new PopulationRecord();

	public PeopleDataManager(PeoplePluginData peoplePluginData) {
		this.peoplePluginData = peoplePluginData;
	}

	/**
	 * Returns a new person id that has been added to the simulation. The returned
	 * PersonId is unique and will wrap the int value returned by getPersonIdLimit()
	 * just prior to invoking this method.
	 *
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PersonError#NULL_PERSON_CONSTRUCTION_DATA}
	 *                           if the person construction data is null</li>
	 *
	 */
	public PersonId addPerson(final PersonConstructionData personConstructionData) {
		PersonId personId = new PersonId(personIds.size());
		dataManagerContext.releaseMutationEvent(new PersonAdditionMutationEvent(personId, personConstructionData));
		return personId;
	}

	/**
	 * Returns the PersonId that corresponds to the given int value.
	 */
	public Optional<PersonId> getBoxedPersonId(final int personId) {
		PersonId result = null;
		if (personIds.size() > personId) {
			result = personIds.get(personId);
		}
		return Optional.ofNullable(result);
	}

	/**
	 * Returns an event filter used to subscribe to {@link PersonAdditionEvent}
	 * events. Matches all such events.
	 */
	public EventFilter<PersonAdditionEvent> getEventFilterForPersonAdditionEvent() {
		return EventFilter.builder(PersonAdditionEvent.class)//
				.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link PersonImminentRemovalEvent} events. Matches all such events.
	 */
	public EventFilter<PersonImminentRemovalEvent> getEventFilterForPersonImminentRemovalEvent() {
		return EventFilter.builder(PersonImminentRemovalEvent.class)//
				.build();
	}

	/**
	 * Returns a list of PersonId for each person that exists.
	 */
	public List<PersonId> getPeople() {

		int count = 0;
		for (final PersonId boxedPersonId : personIds) {
			if (boxedPersonId != null) {
				count++;
			}
		}
		final List<PersonId> result = new ArrayList<>(count);

		for (final PersonId boxedPersonId : personIds) {
			if (boxedPersonId != null) {
				result.add(boxedPersonId);
			}
		}

		return result;
	}

	/**
	 * Returns the lowest int id that has yet to be associated with a person. Lower
	 * values will correspond to existing, removed or unused id values.
	 */
	public int getPersonIdLimit() {
		return personIds.size();
	}

	/**
	 * Returns the number of existing people
	 */
	public int getPopulationCount() {
		return globalPopulationRecord.populationCount;
	}

	/**
	 * Returns the time of the last added or removed person. Returns zero if no
	 * people have been added.
	 */
	public double getPopulationTime() {
		return globalPopulationRecord.assignmentTime;
	}

	private void handlePersonAdditionMutationEvent(DataManagerContext dataManagerContext,
			PersonAdditionMutationEvent personAdditionMutationEvent) {
		PersonConstructionData personConstructionData = personAdditionMutationEvent.personConstructionData();
		PersonId personId = personAdditionMutationEvent.personId();
		validatePersonConstructionDataNotNull(personConstructionData);

		if (personId.getValue() != personIds.size()) {
			throw new RuntimeException("unexpected person id during person addition " + personId);
		}

		personIds.add(personId);
		globalPopulationRecord.populationCount++;
		globalPopulationRecord.assignmentTime = dataManagerContext.getTime();

		/*
		 * It is very likely that the PersonImminentAdditionEvent will have subscribers,
		 * so we don't waste time asking if there are any.
		 * 
		 */
		final PersonImminentAdditionEvent personImminentAdditionEvent = new PersonImminentAdditionEvent(personId,
				personConstructionData);
		dataManagerContext.releaseObservationEvent(personImminentAdditionEvent);

		if (dataManagerContext.subscribersExist(PersonAdditionEvent.class)) {
			dataManagerContext.releaseObservationEvent(new PersonAdditionEvent(personId));
		}

	}

	private void handlePersonRemovalMutationEvent(DataManagerContext dataManagerContext,
			PersonRemovalMutationEvent personRemovalMutationEvent) {
		PersonId personId = personRemovalMutationEvent.personId();

		validatePersonExists(personId);

		dataManagerContext.addPlan((context) -> {
			globalPopulationRecord.populationCount--;
			globalPopulationRecord.assignmentTime = dataManagerContext.getTime();
			personIds.set(personId.getValue(), null);

			// it is very likely that there are observers, so we don't ask
			// before creating the event
			context.releaseObservationEvent(new PersonRemovalEvent(personId));

		}, dataManagerContext.getTime());

		if (dataManagerContext.subscribersExist(PersonImminentRemovalEvent.class)) {
			dataManagerContext.releaseObservationEvent(new PersonImminentRemovalEvent(personId));
		}
	}

	/**
	 * Initializes the data manager. This method should only be invoked by the
	 * simulation. All data manager descendant classes that override this method
	 * must invoke the super.
	 *
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain NucleusError#DATA_MANAGER_DUPLICATE_INITIALIZATION}
	 *                           if init() is invoked more than once</li>
	 * 
	 *                           <li>{@linkplain PersonError#PERSON_ASSIGNMENT_TIME_IN_FUTURE}
	 *                           if the plugin data person assignment time exceeds
	 *                           the start time of the simulation</li>
	 *
	 */
	@Override
	public void init(final DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);
		this.dataManagerContext = dataManagerContext;

		dataManagerContext.subscribe(PersonAdditionMutationEvent.class, this::handlePersonAdditionMutationEvent);
		dataManagerContext.subscribe(PersonRemovalMutationEvent.class, this::handlePersonRemovalMutationEvent);

		int personCount = peoplePluginData.getPersonCount();

		personIds = new ArrayList<>(personCount);
		for (int i = 0; i < personCount; i++) {
			personIds.add(null);
		}

		List<PersonId> personIdsFromPluginData = peoplePluginData.getPersonIds();
		globalPopulationRecord.populationCount = personIdsFromPluginData.size();

		for (PersonId personId : personIdsFromPluginData) {
			personIds.set(personId.getValue(), personId);
		}

		if (peoplePluginData.getAssignmentTime() > dataManagerContext.getTime()) {
			throw new ContractException(PersonError.PERSON_ASSIGNMENT_TIME_IN_FUTURE);
		}

		globalPopulationRecord.assignmentTime = peoplePluginData.getAssignmentTime();
		if (dataManagerContext.stateRecordingIsScheduled()) {
			dataManagerContext.subscribeToSimulationClose(this::recordSimulationState);
		}
	}

	/**
	 * Returns true if and only if the person exists in the simulation.
	 */
	public boolean personExists(final PersonId personId) {
		if (personId != null) {
			int personIndex = personId.getValue();
			if (personIndex < personIds.size()) {
				return personIds.get(personId.getValue()) != null;
			}
		}
		return false;
	}

	/**
	 * Returns true if and only if there is an existing person associated with the
	 * given index. The PersonId is a wrapper around an int index.
	 */
	public boolean personIndexExists(final int personId) {
		boolean result = false;
		if (personId >= 0 && personId < personIds.size()) {
			result = personIds.get(personId) != null;
		}
		return result;

	}

	private void recordSimulationState(DataManagerContext dataManagerContext) {
		PeoplePluginData.Builder builder = PeoplePluginData.builder();
		builder.setPersonCount(personIds.size());
		builder.setAssignmentTime(globalPopulationRecord.assignmentTime);

		int a = -1;
		int b = -1;
		PersonId lastPersonId = null;
		for (int i = 0; i < personIds.size(); i++) {
			PersonId personId = personIds.get(i);
			if (personId != null) {
				if (lastPersonId == null) {
					a = i;
					b = i;
				} else {
					b = i;
				}
			} else {
				if (lastPersonId != null) {
					builder.addPersonRange(new PersonRange(a, b));
				}
			}
			lastPersonId = personId;
		}
		if (a >= 0) {
			builder.addPersonRange(new PersonRange(a, b));
		}
		dataManagerContext.releaseOutput(builder.build());
	}

	/**
	 * Removes the person from the simulation.
	 *
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PersonError#NULL_PERSON_ID} if the
	 *                           person id is null
	 *                           <li>
	 *                           <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if
	 *                           the person does not exist
	 *                           <li>
	 *
	 *
	 */
	public void removePerson(final PersonId personId) {
		dataManagerContext.releaseMutationEvent(new PersonRemovalMutationEvent(personId));
	}

	private void validatePersonConstructionDataNotNull(final PersonConstructionData personConstructionData) {
		if (personConstructionData == null) {
			throw new ContractException(PersonError.NULL_PERSON_CONSTRUCTION_DATA);
		}
	}

	private void validatePersonExists(final PersonId personId) {
		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
		if (!personExists(personId)) {
			throw new ContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PeopleDataManager [personIds=");
		builder.append(personIds);
		builder.append(", globalPopulationRecord=");
		builder.append(globalPopulationRecord);
		builder.append("]");
		return builder.toString();
	}

	private static class PersonIndexIterator implements Iterator<Integer> {

		private Integer next;
		private final Iterator<PersonId> iterator;

		public PersonIndexIterator(Iterator<PersonId> iterator) {
			this.iterator = iterator;
			increment();
		}

		private void increment() {
			next = null;
			while (iterator.hasNext()) {
				PersonId personId = iterator.next();
				if (personId != null) {
					next = personId.getValue();
					break;
				}
			}
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public Integer next() {
			if (next == null) {
				throw new NoSuchElementException();
			}
			Integer result = next;			
			increment();
			return result;
		}
	}

	public Iterator<Integer> getPersonIndexIterator() {
		return new PersonIndexIterator(personIds.iterator());
	}

}
