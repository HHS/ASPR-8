package plugins.people.datamanagers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import nucleus.DataManager;
import nucleus.DataManagerContext;
import nucleus.NucleusError;
import plugins.people.PeoplePluginData;
import plugins.people.events.BulkPersonAdditionEvent;
import plugins.people.events.BulkPersonImminentAdditionEvent;
import plugins.people.events.PersonAdditionEvent;
import plugins.people.events.PersonImminentAdditionEvent;
import plugins.people.events.PersonImminentRemovalEvent;
import plugins.people.events.PersonRemovalEvent;
import plugins.people.support.BulkPersonConstructionData;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import util.errors.ContractException;

/**
 * Mutable data manager for people.
 *
 * @author Shawn Hatch
 *
 */
public final class PeopleDataManager extends DataManager {

	private final PeoplePluginData peoplePluginData;

	public PeopleDataManager(PeoplePluginData peoplePluginData) {
		this.peoplePluginData = peoplePluginData;

	}

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
	 *             <li>{@linkplain PersonError#NULL_PERSON_CONSTRUCTION_DATA} if
	 *             the person construction data is null</li>
	 *
	 */
	public Optional<PersonId> addBulkPeople(final BulkPersonConstructionData bulkPersonConstructionData) {
		validateBulkPersonConstructionData(bulkPersonConstructionData);

		final List<PersonConstructionData> personConstructionDatas = bulkPersonConstructionData.getPersonConstructionDatas();
		PersonId result = null;
		final int count = personConstructionDatas.size();
		if (count > 0) {
			BulkPersonAdditionEvent.Builder eventBuilder = BulkPersonAdditionEvent.builder();
			for (int i = 0; i < count; i++) {
				final PersonId personId = addPersonId();
				eventBuilder.addPersonId(personId);
				if (result == null) {
					result = personId;
				}
			}
			BulkPersonAdditionEvent bulkPersonAdditionEvent = eventBuilder.build();

			final BulkPersonImminentAdditionEvent bulkPersonImminentAdditionEvent = new BulkPersonImminentAdditionEvent(result, bulkPersonConstructionData);
			dataManagerContext.releaseEvent(bulkPersonImminentAdditionEvent);
			dataManagerContext.releaseEvent(bulkPersonAdditionEvent);
		}

		return Optional.ofNullable(result);
	}

	/**
	 * Returns a new person id that has been added to the simulation. The
	 * returned PersonId is unique and will wrap the int value returned by
	 * getPersonIdLimit() just prior to invoking this method.
	 *
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NULL_PERSON_CONSTRUCTION_DATA} if
	 *             the person construction data is null</li>
	 *
	 */
	public PersonId addPerson(final PersonConstructionData personConstructionData) {
		validatePersonConstructionDataNotNull(personConstructionData);

		final PersonId personId = addPersonId();

		final PersonImminentAdditionEvent personImminentAdditionEvent = new PersonImminentAdditionEvent(personId, personConstructionData);
		dataManagerContext.releaseEvent(personImminentAdditionEvent);
		dataManagerContext.releaseEvent(new PersonAdditionEvent(personId));
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

		for (PersonId personId : peoplePluginData.getPersonIds()) {
			personIds.add(personId);
		}
		globalPopulationRecord.projectedPopulationCount = personIds.size();
		globalPopulationRecord.populationCount = personIds.size();
		globalPopulationRecord.assignmentTime = dataManagerContext.getTime();
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

		dataManagerContext.addPlan((context) -> {
			globalPopulationRecord.populationCount--;
			globalPopulationRecord.assignmentTime = dataManagerContext.getTime();
			personIds.set(personId.getValue(), null);
			context.releaseEvent(new PersonRemovalEvent(personId));
		}, dataManagerContext.getTime());

		dataManagerContext.releaseEvent(new PersonImminentRemovalEvent(personId));

	}

	private void validateBulkPersonConstructionData(final BulkPersonConstructionData bulkPersonConstructionData) {
		if (bulkPersonConstructionData == null) {
			throw new ContractException(PersonError.NULL_BULK_PERSON_CONSTRUCTION_DATA);
		}
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
}
