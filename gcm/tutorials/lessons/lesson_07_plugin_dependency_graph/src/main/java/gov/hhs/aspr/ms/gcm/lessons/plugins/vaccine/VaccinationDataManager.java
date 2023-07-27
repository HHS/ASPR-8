package gov.hhs.aspr.ms.gcm.lessons.plugins.vaccine;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.lessons.plugins.family.FamilyDataManager;
import gov.hhs.aspr.ms.gcm.lessons.plugins.family.FamilyId;
import gov.hhs.aspr.ms.gcm.lessons.plugins.people.PersonDataManager;
import gov.hhs.aspr.ms.gcm.lessons.plugins.people.PersonId;
import gov.hhs.aspr.ms.gcm.lessons.plugins.people.PersonRemovalEvent;
import gov.hhs.aspr.ms.gcm.nucleus.DataManager;
import gov.hhs.aspr.ms.gcm.nucleus.DataManagerContext;

public final class VaccinationDataManager extends DataManager {

	private Set<PersonId> vaccinatedPeople = new LinkedHashSet<>();

	private PersonDataManager personDataManager;
	private FamilyDataManager familyDataManager;

	@Override
	public void init(DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);
		dataManagerContext.subscribe(PersonRemovalEvent.class, this::handlePersonRemovalEvent);
		personDataManager = dataManagerContext.getDataManager(PersonDataManager.class);
		familyDataManager = dataManagerContext.getDataManager(FamilyDataManager.class);
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

	/* start code_ref=dag_dependent_query */
	public List<PersonId> getUnvaccinatedFamilyMembers(PersonId personId) {
		if (!personDataManager.personExists(personId)) {
			throw new RuntimeException("unknown person " + personId);
		}
		List<PersonId> result = new ArrayList<>();
		Optional<FamilyId> optional = familyDataManager.getFamilyId(personId);
		if (optional.isPresent()) {
			FamilyId familyId = optional.get();
			List<PersonId> familyMembers = familyDataManager.getFamilyMembers(familyId);
			for (PersonId familyMemeberId : familyMembers) {
				if (!isPersonVaccinated(familyMemeberId)) {
					result.add(personId);
				}
			}
		}
		return result;
	}
	/* end */
}
