package plugins.people;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import nucleus.DataManager;
import nucleus.DataManagerContext;
import nucleus.NucleusError;
import nucleus.util.ContractException;
import plugins.people.events.BulkPersonCreationObservationEvent;
import plugins.people.events.PersonCreationObservationEvent;
import plugins.people.events.PersonImminentRemovalObservationEvent;
import plugins.people.support.BulkPersonConstructionData;
import plugins.people.support.PersonContructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;

/**
 * Mutable data manager for people.
 *
 * @author Shawn Hatch
 *
 */
public final class PersonDataManager extends DataManager {

	private static class PopulationRecord {
		private int projectedPopulationCount;
		private int populationCount;
		private double assignmentTime;
	}

	/*
	 * We keep the person records in a list rather than a map so that we can
	 * retrieve a person record by index (personId).
	 */
	private List<PersonId> personIds = new ArrayList<>();

	private DataManagerContext dataManagerContext;

	private final PopulationRecord globalPopulationRecord = new PopulationRecord();

	

	/**
	 * Returns a new person id that has been added to the simulation. The
	 * returned PersonId is unique and will wrap the int value returned by
	 * getPersonIdLimit() just prior to invoking this method.
	 *
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NULL_PERSON_CONTRUCTION_DATA} if
	 *             the person construction data is null</li>
	 *
	 */
	public Optional<PersonId> addBulkPeople(final BulkPersonConstructionData bulkPersonConstructionData) {
		validateBulkPersonContructionData(bulkPersonConstructionData);

		final List<PersonContructionData> personContructionDatas = bulkPersonConstructionData.getPersonContructionDatas();
		PersonId result = null;
		final int count = personContructionDatas.size();
		for (int i = 0; i < count; i++) {
			final PersonId personId = addPersonId();
			if (result == null) {
				result = personId;
			}
		}

		if (result != null) {
			final BulkPersonCreationObservationEvent bulkPersonCreationObservationEvent = new BulkPersonCreationObservationEvent(result, bulkPersonConstructionData);
			dataManagerContext.releaseEvent(bulkPersonCreationObservationEvent);
		}

		return Optional.ofNullable(result);
	}

	/**
	 * Returns a new person id that has been added to the simulation. The
	 * returned PersonId is unique and will wrap the int value returned by
	 * getPersonIdLimit() just prior to invoking this method.
	 *
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NULL_PERSON_CONTRUCTION_DATA} if
	 *             the person construction data is null</li>
	 *
	 */
	public PersonId addPerson(final PersonContructionData personContructionData) {
		validatePersonContructionDataNotNull(personContructionData);

		final PersonId personId = addPersonId();

		final PersonCreationObservationEvent personCreationObservationEvent = new PersonCreationObservationEvent(personId, personContructionData);
		dataManagerContext.releaseEvent(personCreationObservationEvent);

		return personId;
	}

	private PersonId addPersonId() {
		final PersonId personId = new PersonId(personIds.size());

		personIds.add(personId);
		if (globalPopulationRecord.projectedPopulationCount < personIds.size()) {
			globalPopulationRecord.projectedPopulationCount = personIds.size();
		}
		globalPopulationRecord.populationCount++;
		globalPopulationRecord.assignmentTime = dataManagerContext.getTime();
		return personId;
	}

	/**
	 * Expands the capacity of data structures to hold people by the given
	 * count. Used to more efficiently prepare for bulk population additions.
	 *
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NEGATIVE_GROWTH_PROJECTION} if
	 *             the count is negative</li>
	 */
	public void expandCapacity(final int count) {
		if (count < 0) {
			throw new ContractException(PersonError.NEGATIVE_GROWTH_PROJECTION);
		}
		if (count > 0) {
			globalPopulationRecord.projectedPopulationCount += count;
			final List<PersonId> newPersonIds = new ArrayList<>(personIds.size() + count);
			newPersonIds.addAll(personIds);
			personIds = newPersonIds;
		}
	}

	/**
	 * Returns the PersonId that corresponds to the given int value.	  
	 */
	public Optional<PersonId> getBoxedPersonId(final int personId) {
		PersonId result = null;
		if ((personId >= 0) && (personIds.size() > personId)) {
			result = personIds.get(personId);
		}		
		return Optional.ofNullable(result);
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
	 * Returns the lowest int id that has yet to be associated with a person.
	 * Lower values will correspond to existing, removed or unused id values.
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

	/**
	 * Returns the projected population count that reflects the effect of the
	 * current population count and any capacity expansions.
	 */
	public int getProjectedPopulationCount() {
		return globalPopulationRecord.projectedPopulationCount;
	}

	/**
	 * Initializes the data manager. This method should only be invoked by the
	 * simulation. All data manager descendant classes that override this method
	 * must invoke the super.
	 *
	 * @throws ContractException
	 *             <li>{@linkplain NucleusError#DATA_MANAGER_DUPLICATE_INITIALIZATION}
	 *             if init() is invoked more than once</li>
	 *
	 */
	@Override
	public void init(final DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);
		this.dataManagerContext = dataManagerContext;

		dataManagerContext.addEventLabeler(BulkPersonCreationObservationEvent.getEventLabeler());

		dataManagerContext.addEventLabeler(PersonCreationObservationEvent.getEventLabeler());

		dataManagerContext.addEventLabeler(PersonImminentRemovalObservationEvent.getEventLabeler());

	}

	/**
	 * Returns true if and only if the person exists in the simulation.
	 */
	public boolean personExists(final PersonId personId) {
		if ((personId != null) && (personId.getValue() >= 0) && (personId.getValue() < personIds.size())) {
			return personIds.get(personId.getValue()) != null;
		}
		return false;
	}

	/**
	 * Returns true if and only if there is an existing person associated with
	 * the given index. The PersonId is a wrapper around an int index.
	 */
	public boolean personIndexExists(final int personId) {
		boolean result = false;
		if ((personId >= 0) && (personId < personIds.size())) {
			result = personIds.get(personId) != null;
		}
		return result;

	}

	/**
	 * Removes the person from the simulation.
	 *
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null
	 *             <li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             does not exist
	 *             <li>
	 *
	 *
	 */
	public void removePerson(final PersonId personId) {
		validatePersonExists(personId);

		dataManagerContext.releaseEvent(new PersonImminentRemovalObservationEvent(personId));

		dataManagerContext.addPlan((context) -> {
			globalPopulationRecord.populationCount--;
			globalPopulationRecord.assignmentTime = dataManagerContext.getTime();
			personIds.set(personId.getValue(), null);
		}, dataManagerContext.getTime());

	}

	private void validateBulkPersonContructionData(final BulkPersonConstructionData bulkPersonConstructionData) {
		if (bulkPersonConstructionData == null) {
			throw new ContractException(PersonError.NULL_BULK_PERSON_CONTRUCTION_DATA);
		}
	}

	private void validatePersonContructionDataNotNull(final PersonContructionData personContructionData) {
		if (personContructionData == null) {
			throw new ContractException(PersonError.NULL_PERSON_CONTRUCTION_DATA);
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
}
