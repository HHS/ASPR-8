package lesson.plugins.vaccine;

import java.util.LinkedHashSet;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.nucleus.DataManager;
import gov.hhs.aspr.ms.gcm.nucleus.DataManagerContext;
import lesson.plugins.people.PersonDataManager;
import lesson.plugins.people.PersonId;
import lesson.plugins.people.PersonRemovalEvent;

/* start code_ref=plugin_dependencies_vaccine_data_manager*/
public final class VaccinationDataManager extends DataManager {

	private Set<PersonId> vaccinatedPeople = new LinkedHashSet<>();

	private PersonDataManager personDataManager;

	@Override
	public void init(DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);
		dataManagerContext.subscribe(PersonRemovalEvent.class, this::handlePersonRemovalEvent);
		personDataManager = dataManagerContext.getDataManager(PersonDataManager.class);
	}

	private void handlePersonRemovalEvent(DataManagerContext dataManagerContext,
			PersonRemovalEvent personRemovalEvent) {
		PersonId personId = personRemovalEvent.getPersonId();
		vaccinatedPeople.remove(personId);
		System.out.println("Vaccination Data Manager is removing person " + personId + " at time = "
				+ dataManagerContext.getTime());
	}

	public Set<PersonId> getVaccinatedPeople() {
		return new LinkedHashSet<>(vaccinatedPeople);
	}

	public Set<PersonId> getUnvaccinatedPeople() {
		Set<PersonId> people = personDataManager.getPeople();
		people.removeAll(vaccinatedPeople);
		return people;
	}

	public boolean isPersonVaccinated(PersonId personId) {
		if (!personDataManager.personExists(personId)) {
			throw new RuntimeException("unknown person " + personId);
		}
		return vaccinatedPeople.contains(personId);
	}

	public void vaccinatePerson(PersonId personId) {
		if (!personDataManager.personExists(personId)) {
			throw new RuntimeException("unknown person " + personId);
		}
		vaccinatedPeople.add(personId);
	}

}
/* end */
