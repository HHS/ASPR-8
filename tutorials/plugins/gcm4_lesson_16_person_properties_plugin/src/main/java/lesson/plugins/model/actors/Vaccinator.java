package lesson.plugins.model.actors;

import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import lesson.plugins.model.PersonProperty;
import lesson.plugins.vaccine.VaccinationDataManager;
import nucleus.ActorContext;
import nucleus.EventFilter;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonId;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.personproperties.events.PersonPropertyUpdateEvent;
import plugins.personproperties.support.PersonPropertyDefinitionInitialization;
import plugins.stochastics.StochasticsDataManager;
import plugins.util.properties.PropertyDefinition;

public final class Vaccinator {

	private PeopleDataManager peopleDataManager;
	private VaccinationDataManager vaccinationDataManager;
	private RandomGenerator randomGenerator;
	private PersonPropertiesDataManager personPropertiesDataManager;
	private ActorContext actorContext;

	private void vaccinatePerson(PersonId personId) {
		int vaccineAttempts = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.VACCINE_ATTEMPTS);
		personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.VACCINE_ATTEMPTS, vaccineAttempts + 1);

		boolean isImmune = false;
		if (personPropertiesDataManager.personPropertyIdExists(PersonProperty.IS_IMMUNE)) {
			isImmune = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.IS_IMMUNE);
		}

		Boolean refusesVaccine = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.REFUSES_VACCINE);
		if (!isImmune) {
			if (refusesVaccine) {
				actorContext.addKeyedPlan((c) -> vaccinatePerson(personId), actorContext.getTime() + randomGenerator.nextDouble() * 60, personId);
			} else {
				vaccinationDataManager.vaccinatePerson(personId);
			}
		}
	}

	private void addImmunityProperty() {
		PersonPropertyDefinitionInitialization.Builder builder = PersonPropertyDefinitionInitialization.builder();
		builder.setPersonPropertyId(PersonProperty.IS_IMMUNE);
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).build();
		builder.setPropertyDefinition(propertyDefinition);
		for (PersonId personId : peopleDataManager.getPeople()) {
			boolean isImmune = randomGenerator.nextDouble() < 0.33;
			builder.addPropertyValue(personId, isImmune);
		}
		PersonPropertyDefinitionInitialization personPropertyDefinitionInitialization = builder.build();
		personPropertiesDataManager.definePersonProperty(personPropertyDefinitionInitialization);
	}

	private void handleEducatedPerson(ActorContext actorContext, PersonPropertyUpdateEvent personPropertyUpdateEvent) {
		/*
		 * We know that the person property is PersonProperty.REFUSES_VACCINE since we used an event filter when subscribing
		 */
		Boolean refusesVaccine = personPropertyUpdateEvent.getCurrentPropertyValue();
		if (!refusesVaccine) {
			PersonId personId = personPropertyUpdateEvent.getPersonId();
			actorContext.removePlan(personId);
			vaccinatePerson(personId);
		}
	}

	public void init(ActorContext actorContext) {
		this.actorContext = actorContext;
		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		randomGenerator = stochasticsDataManager.getRandomGenerator();
		peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);
		vaccinationDataManager = actorContext.getDataManager(VaccinationDataManager.class);
		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		List<PersonId> unvaccinatedPeople = vaccinationDataManager.getUnvaccinatedPeople();

		for (PersonId personId : unvaccinatedPeople) {
			actorContext.addKeyedPlan((c) -> vaccinatePerson(personId), actorContext.getTime() + randomGenerator.nextDouble() * 60, personId);
		}

		actorContext.addPlan((c) -> addImmunityProperty(), actorContext.getTime() + 120);

		EventFilter<PersonPropertyUpdateEvent> eventFilter = personPropertiesDataManager//
			.getEventFilterForPersonPropertyUpdateEvent(PersonProperty.REFUSES_VACCINE);
		
		actorContext.subscribe(eventFilter, this::handleEducatedPerson);

	}
}
