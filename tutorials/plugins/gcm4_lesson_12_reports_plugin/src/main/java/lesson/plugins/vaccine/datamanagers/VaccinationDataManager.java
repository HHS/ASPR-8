package lesson.plugins.vaccine;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lesson.plugins.family.FamilyDataManager;
import lesson.plugins.family.FamilyId;
import lesson.plugins.person.PersonDataManager;
import lesson.plugins.person.PersonId;
import lesson.plugins.person.PersonRemovalEvent;
import nucleus.DataManager;
import nucleus.DataManagerContext;

public final class VaccinationDataManager extends DataManager {

	private Set<PersonId> vaccinatedPeople = new LinkedHashSet<>();

	private PersonDataManager personDataManager;
	private FamilyDataManager familyDataManager;
	private DataManagerContext dataManagerContext;
	@Override
	public void init(DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);
		dataManagerContext.subscribe(PersonRemovalEvent.class, this::handlePersonRemovalEvent);
		personDataManager = dataManagerContext.getDataManager(PersonDataManager.class);
		familyDataManager = dataManagerContext.getDataManager(FamilyDataManager.class);
		this.dataManagerContext = dataManagerContext;
	}

	private void handlePersonRemovalEvent(DataManagerContext dataManagerContext, PersonRemovalEvent personRemovalEvent) {
		PersonId personId = personRemovalEvent.getPersonId();
		vaccinatedPeople.remove(personId);
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
		dataManagerContext.releaseEvent(new VaccinationEvent(personId));

	}

	public List<PersonId> getUnvaccinatedFamilyMembers(PersonId personId) {		
		if (!personDataManager.personExists(personId)) {
			throw new RuntimeException("unknown person " + personId);
		}
		List<PersonId> result = new ArrayList<>();
		Optional<FamilyId> optional = familyDataManager.getFamilyId(personId);
		if(optional.isPresent()) {
			FamilyId familyId = optional.get();
			List<PersonId> familyMembers = familyDataManager.getFamilyMembers(familyId);
			for(PersonId familyMemeberId : familyMembers) {
				if(!isPersonVaccinated(familyMemeberId)) {
					result.add(personId);
				}
			}
		}
		return result;
	}

}
