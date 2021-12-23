package plugins.people.datacontainers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import nucleus.DataView;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import util.ContractException;

/**
 * Manager for the set of {@link PersonId} values for the simulation. It serves
 * to ensure that PersonId values used as keys are unique and for helping other
 * managers with optimizing their own data structures.
 * 
 * @author Shawn Hatch
 *
 */

public final class PersonDataView implements DataView {
	private final PersonDataManager personDataManager;

	/**
	 * Constructs the data view from the given data manager
	 * 
	 * @throws ContractException
	 *             <li>if the person data manager is null</li>
	 */
	public PersonDataView(PersonDataManager personDataManager) {
		if (personDataManager == null) {
			throw new ContractException(PersonError.NULL_PERSON_DATA_MANAGER);
		}
		this.personDataManager = personDataManager;
	}

	/**
	 * Returns true if and only if the person exits. Null tolerant.
	 */
	public boolean personExists(final PersonId personId) {
		return personDataManager.personExists(personId);
	}

	/**
	 * Returns a list of PersonId for each person that exists.
	 */
	public List<PersonId> getPeople() {
		return personDataManager.getPeople();
	}

	/**
	 * Returns the last person id that was created by this plugin, i.e. the last
	 * person added to the simulation.
	 */
	public Optional<PersonId> getLastIssuedPersonId() {
		return personDataManager.getLastIssuedPersonId();
	}

	/**
	 * Returns true if and only if there is an existing person associated with
	 * the given index. The PersonId is a wrapper around an int index.
	 */
	public boolean personIndexExists(int personId) {
		return personDataManager.personIndexExists(personId);
	}

	/**
	 * Returns the lowest int id that has yet to be associated with a person.
	 * Lower values will correspond to existing, removed or unused id values.
	 */
	public int getPersonIdLimit() {
		return personDataManager.getPersonIdLimit();
	}

	/**
	 * Returns the PersonId that corresponds to the given int value.
	 * 
	 * @throws ContractException
	 *             <li>if there is no PersonId associated with the value</li>
	 */
	public PersonId getBoxedPersonId(int personId) {
		return personDataManager.getBoxedPersonId(personId);
	}

	/**
	 * Returns the map that transforms person ids in the initial data into the
	 * contiguous person ids of the simulation. Only valid for the people
	 * present at the beginning of the simulation.
	 */
	public Map<PersonId, PersonId> getScenarioToSimPeopleMap() {
		return personDataManager.getScenarioToSimPeopleMap();
	}

	/**
	 * Returns the map that transforms person ids in the simulation into the
	 * contiguous initial data person ids. Only valid for the people present at
	 * the beginning of the simulation.
	 */
	public Map<PersonId, PersonId> getSimToScenarioPeopleMap() {
		return personDataManager.getSimToScenarioPeopleMap();
	}

	/**
	 * Returns the number of existing people
	 */
	public int getPopulationCount() {
		return personDataManager.getPopulationCount();
	}

	/**
	 * Returns the projected population count that reflects the effect of the
	 * current population count and any capacity expansions. This will always be
	 * at least the active population count and will only exceed that count if
	 * population growth projection events have been resolved.
	 */
	public int getProjectedPopulationCount() {
		return personDataManager.getProjectedPopulationCount();
	}

	/**
	 * Returns the time of the last added or removed person. Returns zero if no
	 * people have been added.
	 */
	public double getPopulationTime() {
		return personDataManager.getPopulationTime();
	}

}
