package lesson.plugins.vaccine.datamanagers;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.nucleus.DataManager;
import gov.hhs.aspr.ms.gcm.nucleus.DataManagerContext;
import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.plugins.people.events.PersonImminentAdditionEvent;
import gov.hhs.aspr.ms.gcm.plugins.people.events.PersonRemovalEvent;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import lesson.plugins.vaccine.support.VaccineError;
import lesson.plugins.vaccine.support.VaccineInitialization;
import util.errors.ContractException;
import util.wrappers.MutableInteger;

public final class VaccinationDataManager extends DataManager {
	/* start code_ref= people_plugin_vaccine_counts */
	private Map<PersonId, MutableInteger> vaccinationCounts = new LinkedHashMap<>();
	/* end */
	private PeopleDataManager personDataManager;

	private DataManagerContext dataManagerContext;

	@Override
	/* start code_ref= people_plugin_vaccination_data_manager */
	public void init(DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);
		dataManagerContext.subscribe(PersonRemovalEvent.class, this::handlePersonRemovalEvent);
		dataManagerContext.subscribe(PersonImminentAdditionEvent.class, this::handlePersonImminentAdditionEvent);
		personDataManager = dataManagerContext.getDataManager(PeopleDataManager.class);
		this.dataManagerContext = dataManagerContext;
		for (PersonId personId : personDataManager.getPeople()) {
			vaccinationCounts.put(personId, new MutableInteger());
		}
		dataManagerContext.subscribe(VaccinationMutationEvent.class, this::handleVaccinationMutationEvent);

	}
	/* end */

	/* start code_ref= people_plugin_vaccination_handling_person_removal */

	private void handlePersonRemovalEvent(DataManagerContext dataManagerContext,
			PersonRemovalEvent personRemovalEvent) {
		PersonId personId = personRemovalEvent.personId();
		vaccinationCounts.remove(personId);
	}

	private void handlePersonImminentAdditionEvent(DataManagerContext dataManagerContext,
			PersonImminentAdditionEvent personImminentAdditionEvent) {
		PersonId personId = personImminentAdditionEvent.personId();
		validateNewPersonId(personId);
		MutableInteger mutableInteger = new MutableInteger();
		vaccinationCounts.put(personId, mutableInteger);
		Optional<VaccineInitialization> optional = personImminentAdditionEvent//
				.personConstructionData()//
				.getValue(VaccineInitialization.class);
		if (optional.isPresent()) {
			VaccineInitialization vaccineInitialization = optional.get();
			int vaccineCount = vaccineInitialization.getVaccineCount();
			validateInitialVaccineCount(vaccineCount);
			mutableInteger.setValue(vaccineCount);
		}
	}
	/* end */

	private void validateInitialVaccineCount(int initialVaccineCount) {
		if (initialVaccineCount < 0) {
			throw new ContractException(VaccineError.NEGATIVE_VACCINE_COUNT);
		}
	}

	private void validateNewPersonId(PersonId personId) {
		if (personId == null) {
			throw new ContractException(PersonError.NEGATIVE_PERSON_ID);
		}
		if (vaccinationCounts.containsKey(personId)) {
			throw new RuntimeException("Person is already tracked " + personId);
		}
	}

	/**
	 * Returns the set of people who have had at least one vaccine
	 */
	public Set<PersonId> getVaccinatedPeople() {
		Set<PersonId> result = new LinkedHashSet<>();
		for (PersonId personId : vaccinationCounts.keySet()) {
			MutableInteger mutableInteger = vaccinationCounts.get(personId);
			if (mutableInteger.getValue() > 0) {
				result.add(personId);
			}
		}
		return result;
	}

	/**
	 * Returns the set of people who have not been vaccinated
	 */
	public Set<PersonId> getUnvaccinatedPeople() {
		Set<PersonId> result = new LinkedHashSet<>();
		for (PersonId personId : vaccinationCounts.keySet()) {
			MutableInteger mutableInteger = vaccinationCounts.get(personId);
			if (mutableInteger.getValue() == 0) {
				result.add(personId);
			}
		}
		return result;
	}

	/**
	 * Returns true if and only if the person is vaccinated
	 * 
	 * @throws ContractException
	 *                           <li>{@linkplain PersonError#NULL_PERSON_ID} if the
	 *                           person id is null</li>
	 *                           <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if
	 *                           the person id is unknown</li>
	 */
	public boolean isPersonVaccinated(PersonId personId) {
		validatePersonId(personId);
		return vaccinationCounts.get(personId).getValue() > 0;
	}

	private static record VaccinationMutationEvent(PersonId personId) implements Event {
	}

	/**
	 * Increases the vaccine count for a person
	 * 
	 * @throws ContractException
	 *                           <li>{@linkplain PersonError#NULL_PERSON_ID} if the
	 *                           person id is null</li>
	 *                           <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if
	 *                           the person id is unknown</li>
	 * 
	 */
	public void vaccinatePerson(PersonId personId) {
		dataManagerContext.releaseMutationEvent(new VaccinationMutationEvent(personId));
	}

	private void handleVaccinationMutationEvent(DataManagerContext dataManagerContext,
			VaccinationMutationEvent vaccinationMutationEvent) {
		PersonId personId = vaccinationMutationEvent.personId();
		validatePersonId(personId);
		vaccinationCounts.get(personId).increment();
	}

	/**
	 * Returns the number of vaccines a person has recieved
	 * 
	 * @throws ContractException
	 *                           <li>{@linkplain PersonError#NULL_PERSON_ID} if the
	 *                           person id is null</li>
	 *                           <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if
	 *                           the person id is unknown</li>
	 * 
	 */
	public int getPersonVaccinationCount(PersonId personId) {
		validatePersonId(personId);
		return vaccinationCounts.get(personId).getValue();
	}

	private void validatePersonId(PersonId personId) {
		if (personId == null) {
			throw new ContractException(PersonError.NEGATIVE_PERSON_ID);
		}
		if (!personDataManager.personExists(personId)) {
			throw new ContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

}
