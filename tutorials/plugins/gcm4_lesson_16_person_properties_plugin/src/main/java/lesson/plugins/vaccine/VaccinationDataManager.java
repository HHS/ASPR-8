package lesson.plugins.vaccine;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import nucleus.DataManager;
import nucleus.DataManagerContext;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.events.PersonRemovalEvent;
import plugins.people.support.PersonId;

public final class VaccinationDataManager extends DataManager {

	private Set<PersonId> vaccinatedPeople = new LinkedHashSet<>();

	private PeopleDataManager peopleDataManager;

	private DataManagerContext dataManagerContext;

	@Override
	public void init(DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);
		dataManagerContext.subscribe(PersonRemovalEvent.class, this::handlePersonRemovalEvent);
		peopleDataManager = dataManagerContext.getDataManager(PeopleDataManager.class);
		this.dataManagerContext = dataManagerContext;
	}

	private void handlePersonRemovalEvent(DataManagerContext dataManagerContext, PersonRemovalEvent personRemovalEvent) {
		PersonId personId = personRemovalEvent.getPersonId();
		vaccinatedPeople.remove(personId);
	}

	public Set<PersonId> getVaccinatedPeople() {
		return new LinkedHashSet<>(vaccinatedPeople);
	}

	public List<PersonId> getUnvaccinatedPeople() {
		List<PersonId> people = peopleDataManager.getPeople();
		people.removeAll(vaccinatedPeople);
		return people;
	}

	public boolean isPersonVaccinated(PersonId personId) {
		if (!peopleDataManager.personExists(personId)) {
			throw new RuntimeException("unknown person " + personId);
		}
		return vaccinatedPeople.contains(personId);
	}

	public void vaccinatePerson(PersonId personId) {
		if (!peopleDataManager.personExists(personId)) {
			throw new RuntimeException("unknown person " + personId);
		}

		boolean added = vaccinatedPeople.add(personId);
		if (added) {
			dataManagerContext.releaseEvent(new VaccinationEvent(personId));
		}
	}
	
}
