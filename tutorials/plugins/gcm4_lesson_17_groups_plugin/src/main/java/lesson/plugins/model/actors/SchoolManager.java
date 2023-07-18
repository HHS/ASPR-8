package lesson.plugins.model.actors;

import java.util.List;

import lesson.plugins.model.support.DiseaseState;
import lesson.plugins.model.support.GlobalProperty;
import lesson.plugins.model.support.GroupProperty;
import lesson.plugins.model.support.GroupType;
import lesson.plugins.model.support.PersonProperty;
import lesson.plugins.model.support.SchoolStatus;
import nucleus.ActorContext;
import nucleus.Plan;
import plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import plugins.groups.datamanagers.GroupsDataManager;
import plugins.groups.support.GroupConstructionInfo;
import plugins.groups.support.GroupId;
import plugins.people.support.PersonId;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;

public class SchoolManager {
	private ActorContext actorContext;
	private GroupsDataManager groupsDataManager;
	private PersonPropertiesDataManager personPropertiesDataManager;
	private double cohortThreshold;
	private double closureThreshold;
	private final double reviewInterval = 7;

	/* start code_ref= groups_plugin_school_manager_init */
	public void init(ActorContext actorContext) {
		this.actorContext = actorContext;
		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		groupsDataManager = actorContext.getDataManager(GroupsDataManager.class);
		GlobalPropertiesDataManager globalPropertiesDataManager = actorContext
				.getDataManager(GlobalPropertiesDataManager.class);
		cohortThreshold = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.SCHOOL_COHORT_INFECTION_THRESHOLD);
		closureThreshold = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.SCHOOL_CLOSURE_INFECTION_THRESHOLD);
		planNextReview();
	}

	private void planNextReview() {
		double planTime = actorContext.getTime() + reviewInterval;
		Plan<ActorContext> plan = Plan.builder(ActorContext.class)//
				.setCallbackConsumer(this::reviewSchools)//
				.setActive(false)//
				.setTime(planTime)//
				.build();
		actorContext.addPlan(plan);
	}

	private void reviewSchools(ActorContext actorContext) {
		List<GroupId> schoolGroupIds = groupsDataManager.getGroupsForGroupType(GroupType.SCHOOL);
		for (GroupId groupId : schoolGroupIds) {
			reviewSchool(groupId);
		}
		planNextReview();
	}

	/* end */

	/* start code_ref= groups_plugin_school_manager_review_school */
	private void reviewSchool(GroupId groupId) {

		int infectiousCount = 0;
		List<PersonId> peopleForGroup = groupsDataManager.getPeopleForGroup(groupId);
		for (PersonId personId : peopleForGroup) {
			DiseaseState diseaseState = personPropertiesDataManager.getPersonPropertyValue(personId,
					PersonProperty.DISEASE_STATE);
			if (diseaseState == DiseaseState.INFECTIOUS) {
				infectiousCount++;
			}
		}

		double infectiousFraction = infectiousCount;
		if (!peopleForGroup.isEmpty()) {
			infectiousFraction /= peopleForGroup.size();
		}

		SchoolStatus schoolStatus = groupsDataManager.getGroupPropertyValue(groupId, GroupProperty.SCHOOL_STATUS);

		switch (schoolStatus) {
		case OPEN:
			if (infectiousFraction >= cohortThreshold) {
				splitSchoolIntoCohorts(groupId);
			}
			break;
		case COHORT:
			if (infectiousFraction >= closureThreshold) {
				closeSchool(groupId);
			}
			break;
		case CLOSED:
			// do nothing
			break;
		default:
			throw new RuntimeException("unhandled case " + schoolStatus);
		}
	}

	/* end */
	
	/* start code_ref= groups_plugin_school_manager_close_schools */
	private void closeSchool(GroupId groupId) {
		groupsDataManager.setGroupPropertyValue(groupId, GroupProperty.SCHOOL_STATUS, SchoolStatus.CLOSED);
		List<PersonId> people = groupsDataManager.getPeopleForGroup(groupId);
		for (PersonId personId : people) {
			groupsDataManager.removePersonFromGroup(personId, groupId);
		}
	}
	/* end */

	/* start code_ref= groups_plugin_school_manager_split_schools */
	private void splitSchoolIntoCohorts(GroupId groupId) {
		GroupConstructionInfo groupConstructionInfo = GroupConstructionInfo.builder().setGroupTypeId(GroupType.SCHOOL)
				.build();
		GroupId newGroupId = groupsDataManager.addGroup(groupConstructionInfo);

		List<PersonId> peopleForGroup = groupsDataManager.getPeopleForGroup(groupId);
		for (int i = 0; i < peopleForGroup.size(); i++) {
			if (i % 2 == 0) {
				PersonId personId = peopleForGroup.get(i);
				groupsDataManager.removePersonFromGroup(personId, groupId);
				groupsDataManager.addPersonToGroup(personId, newGroupId);
			}
		}

		groupsDataManager.setGroupPropertyValue(newGroupId, GroupProperty.SCHOOL_STATUS, SchoolStatus.COHORT);
		groupsDataManager.setGroupPropertyValue(groupId, GroupProperty.SCHOOL_STATUS, SchoolStatus.COHORT);

	}
	/* end */
}
