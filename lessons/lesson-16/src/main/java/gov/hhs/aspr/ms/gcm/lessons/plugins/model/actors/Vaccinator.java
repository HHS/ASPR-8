package gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.GlobalProperty;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.PersonProperty;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.ActorPlan;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.ConsumerActorPlan;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.EventFilter;
import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.events.PersonPropertyUpdateEvent;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.datamanagers.StochasticsDataManager;

public final class Vaccinator {

	private PeopleDataManager peopleDataManager;
	private RandomGenerator randomGenerator;
	private PersonPropertiesDataManager personPropertiesDataManager;
	private GlobalPropertiesDataManager globalPropertiesDataManager;
	private double vaccineAttemptInterval;
	private ActorContext actorContext;
	private Map<PersonId,ActorPlan> actorPlans = new LinkedHashMap<>();

	/* start code_ref= person_properties_vaccinator_vaccinate_person|code_cap= With each vaccination attempt, the vaccinator updates the VACCINE_ATTEMPTS person property for the person.  People who refuse vaccination are scheduled for another vaccination attempt. */
	private void vaccinatePerson(PersonId personId) {
		int vaccineAttempts = personPropertiesDataManager.getPersonPropertyValue(personId,
				PersonProperty.VACCINE_ATTEMPTS);
		personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.VACCINE_ATTEMPTS,
				vaccineAttempts + 1);

		boolean isImmune = false;
		if (personPropertiesDataManager.personPropertyIdExists(PersonProperty.IS_IMMUNE)) {
			isImmune = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.IS_IMMUNE);
		}

		Boolean refusesVaccine = personPropertiesDataManager.getPersonPropertyValue(personId,
				PersonProperty.REFUSES_VACCINE);
		if (!isImmune) {
			if (refusesVaccine) {
				double planTime = actorContext.getTime() + randomGenerator.nextDouble() * vaccineAttemptInterval;

				ActorPlan plan = new ConsumerActorPlan(planTime, (c) -> vaccinatePerson(personId));				
				actorContext.addPlan(plan);
				//record the plan for possible cancellation
				actorPlans.put(personId, plan);
			} else {
				personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.VACCINATED, true);
			}
		}
	}
	/* end */

	/* start code_ref= person_properties_vaccinator_handle_vaccine_acceptance|code_cap=When a person stops refusing vaccination, the vaccinator immediately attempts the vaccination of that person. */
	private void handleVaccineAcceptance(ActorContext actorContext,
			PersonPropertyUpdateEvent personPropertyUpdateEvent) {
		/*
		 * We know that the person property is PersonProperty.REFUSES_VACCINE since we
		 * used an event filter when subscribing
		 */
		Boolean refusesVaccine = personPropertyUpdateEvent.getCurrentPropertyValue();
		if (!refusesVaccine) {
			PersonId personId = personPropertyUpdateEvent.personId();
			// drop the current plan
			ActorPlan actorPlan = actorPlans.remove(personId);
			if(actorPlan != null) {
				actorPlan.cancelPlan();				
			}			
			vaccinatePerson(personId);
		}
	}
	/* end */

	/* start code_ref= person_properties_vaccinator_plan_vaccination|code_cap= Each unvaccinated person has a planned vaccination based on the VACCINE_ATTEMPT_INTERVAL global property. */
	private void planVaccination(PersonId personId) {
		double planTime = actorContext.getTime() + randomGenerator.nextDouble() * vaccineAttemptInterval;		
		ActorPlan plan = new ConsumerActorPlan(planTime, (c) -> vaccinatePerson(personId));				
		actorContext.addPlan(plan);
		//record the plan for possible cancellation
		actorPlans.put(personId, plan);
	}

	private void handleNewPerson(PersonId personId) {
		boolean vaccinated = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.VACCINATED);
		if (!vaccinated) {
			planVaccination(personId);
		}
	}
	/* end */

	/* start code_ref= person_properties_vaccinator_init|code_cap=The vaccinator initializes by planning vaccination attempts for each person who is unvaccinated. It also subscribes to changes in the vaccine refusal property for all people so that when a person stops refusing vaccine, they can be vaccinated immediately.*/
	public void init(ActorContext actorContext) {
		this.actorContext = actorContext;
		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		randomGenerator = stochasticsDataManager.getRandomGenerator();
		peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);
		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		globalPropertiesDataManager = actorContext.getDataManager(GlobalPropertiesDataManager.class);

		List<PersonId> unvaccinatedPeople = personPropertiesDataManager
				.getPeopleWithPropertyValue(PersonProperty.VACCINATED, false);
		vaccineAttemptInterval = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.VACCINE_ATTEMPT_INTERVAL);
		for (PersonId personId : unvaccinatedPeople) {
			planVaccination(personId);
		}

		EventFilter<PersonPropertyUpdateEvent> eventFilter = personPropertiesDataManager//
				.getEventFilterForPersonPropertyUpdateEvent(PersonProperty.REFUSES_VACCINE);

		actorContext.subscribe(eventFilter, this::handleVaccineAcceptance);

		actorContext.subscribe(peopleDataManager.getEventFilterForPersonAdditionEvent(), (c, e) -> {
			handleNewPerson(e.personId());
		});

	}
	/* end */
}
