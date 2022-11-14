package lesson.plugins.model.actors;

import lesson.plugins.model.PersonProperty;
import lesson.plugins.model.Resource;
import nucleus.ActorContext;
import nucleus.EventFilter;
import plugins.people.support.PersonId;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.resources.datamanagers.ResourcesDataManager;
import plugins.resources.events.PersonResourceUpdateEvent;

public class QuestionnaireDistributor {
	private PersonPropertiesDataManager personPropertiesDataManager;

	public void init(ActorContext actorContext) {
		ResourcesDataManager resourcesDataManager = actorContext
				.getDataManager(ResourcesDataManager.class);
		personPropertiesDataManager = actorContext
				.getDataManager(PersonPropertiesDataManager.class);
		

		EventFilter<PersonResourceUpdateEvent> eventFilter = 
				resourcesDataManager
				.getEventFilterForPersonResourceUpdateEvent(Resource.ANTI_VIRAL_MED);
		actorContext.subscribe(eventFilter, this::handleAntiViralDistribution);

		eventFilter = resourcesDataManager
				.getEventFilterForPersonResourceUpdateEvent(Resource.HOSPITAL_BED);
		actorContext.subscribe(eventFilter, this::handleAntiHospitalBedDistribution);

	}

	private void handleAntiViralDistribution(ActorContext actorContext,
			PersonResourceUpdateEvent personResourceUpdateEvent) {
		PersonId personId = personResourceUpdateEvent.getPersonId();
		boolean hasAntiviral = personResourceUpdateEvent.getCurrentResourceLevel() > 0;
		if (!hasAntiviral) {
			boolean hospitalized = personPropertiesDataManager
					.getPersonPropertyValue(personId, PersonProperty.HOSPITALIZED);
			if (!hospitalized) {
				distributeQuestionaire(personId);
			}
		}
	}

	private void handleAntiHospitalBedDistribution(ActorContext actorContext
			, PersonResourceUpdateEvent personResourceUpdateEvent) {
		PersonId personId = personResourceUpdateEvent.getPersonId();
		boolean hasBed = personResourceUpdateEvent.getCurrentResourceLevel() > 0;
		boolean dead = personIsDead(personId);
		if (!hasBed && !dead) {
			distributeQuestionaire(personId);
		}
	}

	private boolean personIsDead(PersonId personId) {
		boolean deadInHome = personPropertiesDataManager
				.getPersonPropertyValue(personId, PersonProperty.DEAD_IN_HOME);
		boolean deadInHospital = personPropertiesDataManager
				.getPersonPropertyValue(personId, PersonProperty.DEAD_IN_HOSPITAL);
		return deadInHome || deadInHospital;
	}

	private void distributeQuestionaire(PersonId personId) {
		personPropertiesDataManager
			.setPersonPropertyValue(personId, PersonProperty.RECEIVED_QUESTIONNAIRE, true);
	}
}
