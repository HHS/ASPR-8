package plugins.people.datacontainers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import nucleus.SimulationContext;
import nucleus.NucleusError;
import plugins.people.PeoplePlugin;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import util.ContractException;

/**
 * Mutable data manager that backs the {@linkplain PersonDataView}. This data
 * manager is for internal use by the {@link PeoplePlugin} and should not be
 * published. It provides various functions for person id values. Limited
 * validation of inputs are performed and mutation methods have invocation
 * ordering requirements.
 * 
 * @author Shawn Hatch
 *
 */
public final class PersonDataManager {

	/*
	 * We keep the person records in a list rather than a map so that we can
	 * retrieve a person record by index (personId).
	 */
	private List<PersonId> personIds;

	private PersonId lastIssuedPersonId;

	/**
	 * Returns true if and only if there is an existing person associated with
	 * the given index. The PersonId is a wrapper around an int index.
	 */
	public boolean personIndexExists(int personId) {
		boolean result = false;
		if ((personId >= 0) && (personId < personIds.size())) {
			result = personIds.get(personId) != null;
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
	 * Returns the PersonId that corresponds to the given int value.
	 * 
	 * @throws ContractException
	 *             <li>if there is no PersonId associated with the value</li>
	 */
	public PersonId getBoxedPersonId(int personId) {
		PersonId result = null;
		if ((personId >= 0) && (personIds.size() > personId)) {
			result = personIds.get(personId);
		}

		if (result == null) {
			simulationContext.throwContractException(PersonError.UNKNOWN_PERSON_ID, personId);
		}
		return result;
	}

	/**
	 * Returns a new person id that has been added to the simulation. The
	 * returned PersonId is unique and will wrap the int value returned by
	 * getPersonIdLimit() just prior to invoking this method.
	 */
	public PersonId addPersonId() {
		PersonId personId = new PersonId(personIds.size());
		lastIssuedPersonId = personId;
		personIds.add(personId);
		if (globalPopulationRecord.projectedPopulationCount < personIds.size()) {
			globalPopulationRecord.projectedPopulationCount = personIds.size();
		}
		globalPopulationRecord.populationCount++;
		globalPopulationRecord.assignmentTime = simulationContext.getTime();
		return personId;
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
	public void removePerson(PersonId personId) {
		if (personId == null) {
			simulationContext.throwContractException(PersonError.NULL_PERSON_ID);
		}
		if (!personExists(personId)) {
			simulationContext.throwContractException(PersonError.UNKNOWN_PERSON_ID);
		}
		globalPopulationRecord.populationCount--;
		globalPopulationRecord.assignmentTime = simulationContext.getTime();
		personIds.set(personId.getValue(), null);
	}

	private SimulationContext simulationContext;

	/**
	 * Constructs this data manager from the given context and initial capacity
	 * 
	 * 
	 * @throws IllegalArgumentException
	 *             <li>if the initial capacity is negative</li>
	 * @throws ContractException
	 *             <li>{@linkplain NucleusError#NULL_CONTEXT} if the context is
	 *             null</li>
	 */
	public PersonDataManager(SimulationContext simulationContext, int initialCapacity) {
		if (simulationContext == null) {
			throw new ContractException(NucleusError.NULL_CONTEXT);
		}
		this.simulationContext = simulationContext;
		personIds = new ArrayList<>(initialCapacity);
	}

	/**
	 * Expands the capacity of data structures to hold people by the given
	 * count. Used to more efficiently prepare for bulk population additions.
	 */
	public void expandCapacity(int count) {
		if (count > 0) {
			globalPopulationRecord.projectedPopulationCount += count;
			List<PersonId> newPersonIds = new ArrayList<>(personIds.size() + count);
			newPersonIds.addAll(personIds);
			personIds = newPersonIds;
		}
	}

	/**
	 * Returns the last person id that was created by this manager, i.e. the
	 * last person added to the simulation.
	 */
	public Optional<PersonId> getLastIssuedPersonId() {
		return Optional.ofNullable(lastIssuedPersonId);
	}

	/**
	 * Returns the map that transforms person ids in the initial data into the
	 * contiguous person ids of the simulation. Only valid for the people
	 * present at the beginning of the simulation.
	 */
	public Map<PersonId, PersonId> getScenarioToSimPeopleMap() {
		return new LinkedHashMap<>(scenarioToSimPeopleMap);
	}

	/**
	 * Returns the map that transforms person ids in the simulation into the
	 * contiguous initial data person ids. Only valid for the people present at
	 * the beginning of the simulation.
	 */
	public Map<PersonId, PersonId> getSimToScenarioPeopleMap() {
		return new LinkedHashMap<>(simToScenarioPeopleMap);
	}

	/**
	 * Sets the mapping between the person ids in the initial data and the
	 * contiguous person ids of the simulation.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NON_ONE_TO_ONE_MAPPING} if the
	 *             given mapping is not one-to-one</li>
	 */
	public void setScenarioToSimPeopleMap(Map<PersonId, PersonId> scenarioToSimPeopleMap) {
		this.scenarioToSimPeopleMap.clear();
		this.simToScenarioPeopleMap.clear();
		this.scenarioToSimPeopleMap.putAll(scenarioToSimPeopleMap);
		for (PersonId scenarionPersonId : scenarioToSimPeopleMap.keySet()) {
			PersonId simPersonId = scenarioToSimPeopleMap.get(scenarionPersonId);
			this.simToScenarioPeopleMap.put(simPersonId, scenarionPersonId);
		}
		if (this.scenarioToSimPeopleMap.size() != simToScenarioPeopleMap.size()) {
			simulationContext.throwContractException(PersonError.NON_ONE_TO_ONE_MAPPING);
		}
	}

	private Map<PersonId, PersonId> scenarioToSimPeopleMap = new LinkedHashMap<>();
	private Map<PersonId, PersonId> simToScenarioPeopleMap = new LinkedHashMap<>();

	/**
	 * Returns the number of existing people
	 */
	public int getPopulationCount() {
		return globalPopulationRecord.populationCount;
	}

	/**
	 * Returns the projected population count that reflects the effect of the
	 * current population count and any capacity expansions.
	 */
	public int getProjectedPopulationCount() {
		return globalPopulationRecord.projectedPopulationCount;
	}

	/**
	 * Returns the time of the last added or removed person. Returns zero if no
	 * people have been added.
	 */
	public double getPopulationTime() {
		return globalPopulationRecord.assignmentTime;
	}

	private static class PopulationRecord {
		private int projectedPopulationCount;
		private int populationCount;
		private double assignmentTime;
	}

	private final PopulationRecord globalPopulationRecord = new PopulationRecord();
}
