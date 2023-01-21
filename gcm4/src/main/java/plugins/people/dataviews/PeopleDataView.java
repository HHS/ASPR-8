package plugins.people.dataviews;

import java.util.List;
import java.util.Optional;

import nucleus.DataView;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonId;

/**
 * Data view of the PeopleDataManager
 *
 */
public final class PeopleDataView implements DataView {
	
	private final PeopleDataManager peopleDataManager;
	/**
	 * Constructs this view from the corresponding data manager 
	 * 
	 */
	public PeopleDataView(PeopleDataManager peopleDataManager) {
		this.peopleDataManager = peopleDataManager;
	}

	/**
	 * Returns the PersonId that corresponds to the given int value.
	 */
	public Optional<PersonId> getBoxedPersonId(final int personId) {
		return peopleDataManager.getBoxedPersonId(personId);
	}

	/**
	 * Returns a list of PersonId for each person that exists.
	 */
	public List<PersonId> getPeople() {
		return peopleDataManager.getPeople();
	}

	/**
	 * Returns the lowest int id that has yet to be associated with a person.
	 * Lower values will correspond to existing, removed or unused id values.
	 */
	public int getPersonIdLimit() {
		return peopleDataManager.getPersonIdLimit();
	}

	/**
	 * Returns the number of existing people
	 */
	public int getPopulationCount() {
		return peopleDataManager.getPopulationCount();
	}

	/**
	 * Returns the time of the last added or removed person. Returns zero if no
	 * people have been added.
	 */
	public double getPopulationTime() {
		return peopleDataManager.getPopulationTime();
	}

	/**
	 * Returns the projected population count that reflects the effect of the
	 * current population count and any capacity expansions.
	 */
	public int getProjectedPopulationCount() {
		return peopleDataManager.getProjectedPopulationCount();
	}

	/**
	 * Returns true if and only if the person exists in the simulation.
	 */
	public boolean personExists(final PersonId personId) {
		return peopleDataManager.personExists(personId);
	}

	/**
	 * Returns true if and only if there is an existing person associated with
	 * the given index. The PersonId is a wrapper around an int index.
	 */
	public boolean personIndexExists(final int personId) {
		return peopleDataManager.personIndexExists(personId);

	}
}
