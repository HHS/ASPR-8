package lesson.plugins.model.actors;

import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import lesson.plugins.model.support.DiseaseState;
import lesson.plugins.model.support.GlobalProperty;
import lesson.plugins.model.support.GroupProperty;
import lesson.plugins.model.support.GroupType;
import lesson.plugins.model.support.PersonProperty;
import nucleus.ActorContext;
import nucleus.Plan;
import plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import plugins.groups.datamanagers.GroupsDataManager;
import plugins.groups.support.GroupId;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.stochastics.datamanagers.StochasticsDataManager;

public class TeleworkManager {

	private final double reviewInterval = 7;
	private ActorContext actorContext;
	
	public void init(ActorContext actorContext) {
		this.actorContext = actorContext;
		scheduleNextReview();
	}
	
	private void scheduleNextReview() {
		double planTime = actorContext.getTime() + reviewInterval;
		Plan<ActorContext> plan = Plan.builder(ActorContext.class)//
				.setCallbackConsumer(this::reviewTeleworkStatus)//
				.setActive(false)//
				.setTime(planTime)//
				.build();
		
		actorContext.addPlan(plan);
	}

	private void reviewTeleworkStatus(ActorContext actorContext) {
		StochasticsDataManager stochasticsDataManager = 
				actorContext.getDataManager(StochasticsDataManager.class);
		RandomGenerator randomGenerator = stochasticsDataManager
				.getRandomGenerator();
		PeopleDataManager peopleDataManager = actorContext
				.getDataManager(PeopleDataManager.class);
		PersonPropertiesDataManager personPropertiesDataManager = actorContext
				.getDataManager(PersonPropertiesDataManager.class);
		GroupsDataManager groupsDataManager = actorContext
				.getDataManager(GroupsDataManager.class);
		GlobalPropertiesDataManager globalPropertiesDataManager = actorContext
				.getDataManager(GlobalPropertiesDataManager.class);
		double threshold = globalPropertiesDataManager
				.getGlobalPropertyValue(
						GlobalProperty.TELEWORK_INFECTION_THRESHOLD);
		double teleworkProbability = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.TELEWORK_PROBABILTY);

		int infectiousCount = personPropertiesDataManager
				.getPersonCountForPropertyValue(PersonProperty.DISEASE_STATE
						, DiseaseState.INFECTIOUS);
		int populationCount = peopleDataManager.getPopulationCount();

		double infectiousFraction = infectiousCount;
		infectiousFraction /= populationCount;

		if (infectiousFraction >= threshold) {
			List<GroupId> workGroupIds = groupsDataManager
					.getGroupsForGroupType(GroupType.WORK);
			for (GroupId groupId : workGroupIds) {
				if (randomGenerator.nextDouble() < teleworkProbability) {
					groupsDataManager
					.setGroupPropertyValue(groupId, 
							GroupProperty.TELEWORK, true);
				}
			}
		} else {
			scheduleNextReview();
		}
	}
	
}
