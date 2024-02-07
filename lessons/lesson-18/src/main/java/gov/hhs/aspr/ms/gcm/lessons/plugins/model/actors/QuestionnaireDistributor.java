package gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.PersonProperty;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.Resource;
import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.nucleus.EventFilter;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.plugins.resources.datamanagers.ResourcesDataManager;
import gov.hhs.aspr.ms.gcm.plugins.resources.events.PersonResourceUpdateEvent;

public class QuestionnaireDistributor {
	private PersonPropertiesDataManager personPropertiesDataManager;

	/* start code_ref=resources_QuestionnaireDistributor_init|code_cap=The questionnaire distributor initializes by subscribing to anti-viral and hospital bed person resource updates.*/
	public void init(ActorContext actorContext) {
		ResourcesDataManager resourcesDataManager = actorContext.getDataManager(ResourcesDataManager.class);
		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);

		EventFilter<PersonResourceUpdateEvent> eventFilter = resourcesDataManager
				.getEventFilterForPersonResourceUpdateEvent(Resource.ANTI_VIRAL_MED);
		actorContext.subscribe(eventFilter, this::handleAntiViralDistribution);

		eventFilter = resourcesDataManager.getEventFilterForPersonResourceUpdateEvent(Resource.HOSPITAL_BED);
		actorContext.subscribe(eventFilter, this::handleHospitalBedDistribution);

	}
	/* end */

	/*
	 * start code_ref=resources_QuestionnaireDistributor_handleAntiViralDistribution|code_cap=The questionnaire distributor distributes a questionnaire to each person who ends their anti-viral treatment and is not also hospitalized. 
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
	 * start code_ref=resources_QuestionnaireDistributor_handleAntiHospitalBedDistribution|code_cap=The questionnaire distributor distributes a questionnaire to each person that leaves the hospital.
	 */
	private void handleHospitalBedDistribution(ActorContext actorContext,
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
