package gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors;

import java.util.List;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.DiseaseState;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.GlobalProperty;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.GroupProperty;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.GroupType;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.PersonProperty;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.SchoolStatus;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.ActorPlan;
import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.datamanagers.GroupsDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support.GroupConstructionInfo;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support.GroupId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.datamanagers.PersonPropertiesDataManager;

public class SchoolManager {
	private ActorContext actorContext;
	private GroupsDataManager groupsDataManager;
	private PersonPropertiesDataManager personPropertiesDataManager;
	private double cohortThreshold;
	private double closureThreshold;
	private final double reviewInterval = 7;

	/* start code_ref= groups_plugin_school_manager_init|code_cap=The school manager initializes by establishing some property constants and planning school status review for seven days after the simulation starts.*/
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
		ActorPlan plan = new ActorPlan(planTime,false,this::reviewSchools);				
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

	/* start code_ref= groups_plugin_school_manager_review_school|code_cap=Each school is reviewed on a weekly basis.  As the fraction of students who are infected increases, the school transitions from OPEN to COHORT to CLOSED.*/
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

	/* start code_ref= groups_plugin_school_manager_close_schools|code_cap=When a school is closed, all the students are removed from the school group so that infection can no longer spread via school-based contact. */
	private void closeSchool(GroupId groupId) {
		groupsDataManager.setGroupPropertyValue(groupId, GroupProperty.SCHOOL_STATUS, SchoolStatus.CLOSED);
		List<PersonId> people = groupsDataManager.getPeopleForGroup(groupId);
		for (PersonId personId : people) {
			groupsDataManager.removePersonFromGroup(personId, groupId);
		}
	}
	/* end */

	/* start code_ref= groups_plugin_school_manager_split_schools|code_cap= When a school moves to COHORT status, a new group is added to the simulation and half of the students move to this new group.*/
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
