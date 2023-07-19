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

	/* start code_ref=resources_QuestionnaireDistributor_init */
	public void init(ActorContext actorContext) {
		ResourcesDataManager resourcesDataManager = actorContext.getDataManager(ResourcesDataManager.class);
		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);

		EventFilter<PersonResourceUpdateEvent> eventFilter = resourcesDataManager
				.getEventFilterForPersonResourceUpdateEvent(Resource.ANTI_VIRAL_MED);
		actorContext.subscribe(eventFilter, this::handleAntiViralDistribution);

		eventFilter = resourcesDataManager.getEventFilterForPersonResourceUpdateEvent(Resource.HOSPITAL_BED);
		actorContext.subscribe(eventFilter, this::handleAntiHospitalBedDistribution);

	}
	/* end */

	/*
	 * start code_ref=resources_QuestionnaireDistributor_handleAntiViralDistribution
	 */
	private void handleAntiViralDistribution(ActorContext actorContext,
			PersonResourceUpdateEvent personResourceUpdateEvent) {
		PersonId personId = personResourceUpdateEvent.personId();
		boolean hasAntiviral = personResourceUpdateEvent.currentResourceLevel() > 0;
		if (!hasAntiviral) {
			boolean hospitalized = personPropertiesDataManager.getPersonPropertyValue(personId,
					PersonProperty.HOSPITALIZED);
			if (!hospitalized) {
				distributeQuestionaire(personId);
			}
		}
	}
	/* end */

	/*
	 * start code_ref=resources_QuestionnaireDistributor_handleAntiHospitalBedDistribution
	 */
	private void handleAntiHospitalBedDistribution(ActorContext actorContext,
			PersonResourceUpdateEvent personResourceUpdateEvent) {
		PersonId personId = personResourceUpdateEvent.personId();
		boolean hasBed = personResourceUpdateEvent.currentResourceLevel() > 0;
		boolean dead = personIsDead(personId);
		if (!hasBed && !dead) {
			distributeQuestionaire(personId);
		}
	}

	/* end */
	private boolean personIsDead(PersonId personId) {
		boolean deadInHome = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.DEAD_IN_HOME);
		boolean deadInHospital = personPropertiesDataManager.getPersonPropertyValue(personId,
				PersonProperty.DEAD_IN_HOSPITAL);
		return deadInHome || deadInHospital;
	}

	private void distributeQuestionaire(PersonId personId) {
		personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.RECEIVED_QUESTIONNAIRE, true);
	}
}
