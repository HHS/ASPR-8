package gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors;

import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.DiseaseState;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.GlobalProperty;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.GroupProperty;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.GroupType;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.PersonProperty;
import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.nucleus.ActorPlan;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.plugins.groups.datamanagers.GroupsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupId;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsDataManager;

public class TeleworkManager {

	private final double reviewInterval = 7;
	private ActorContext actorContext;

	/* start code_ref= groups_plugin_telework_manager_init|code_cap= The telework manager initializes by scheduling a telework status review for seven days from the start of the simulation.*/
	public void init(ActorContext actorContext) {
		this.actorContext = actorContext;
		scheduleNextReview();
	}

	private void scheduleNextReview() {
		double planTime = actorContext.getTime() + reviewInterval;
		ActorPlan plan = new ActorPlan(planTime,false,this::reviewTeleworkStatus);
		actorContext.addPlan(plan);		
	}

	/* end */
	/* start code_ref= groups_plugin_telework_review_status|code_cap= The telework manager schedules a review every seven days until a threshold of infections is reached.  Once the threshold is achieved, work places are randomly selected to use telework until the end of the simulation.*/
	private void reviewTeleworkStatus(ActorContext actorContext) {
		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
		PeopleDataManager peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);
		PersonPropertiesDataManager personPropertiesDataManager = actorContext
				.getDataManager(PersonPropertiesDataManager.class);
		GroupsDataManager groupsDataManager = actorContext.getDataManager(GroupsDataManager.class);
		GlobalPropertiesDataManager globalPropertiesDataManager = actorContext
				.getDataManager(GlobalPropertiesDataManager.class);
		double threshold = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.TELEWORK_INFECTION_THRESHOLD);
		double teleworkProbability = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.TELEWORK_PROBABILTY);

		int infectiousCount = personPropertiesDataManager.getPersonCountForPropertyValue(PersonProperty.DISEASE_STATE,
				DiseaseState.INFECTIOUS);
		int populationCount = peopleDataManager.getPopulationCount();

		double infectiousFraction = infectiousCount;
		infectiousFraction /= populationCount;

		if (infectiousFraction >= threshold) {
			List<GroupId> workGroupIds = groupsDataManager.getGroupsForGroupType(GroupType.WORK);
			for (GroupId groupId : workGroupIds) {
				if (randomGenerator.nextDouble() < teleworkProbability) {
					groupsDataManager.setGroupPropertyValue(groupId, GroupProperty.TELEWORK, true);
				}
			}
		} else {
			scheduleNextReview();
		}
	}
	/* end */
}
