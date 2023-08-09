package gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.DiseaseState;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.GlobalProperty;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.GroupProperty;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.GroupType;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.PersonProperty;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.SchoolStatus;
import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.plugins.groups.datamanagers.GroupsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupId;
import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupSampler;
import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupTypeId;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsDataManager;

public class InfectionManager {
	private ActorContext actorContext;
	private PersonPropertiesDataManager personPropertiesDataManager;
	private GroupsDataManager groupsDataManager;
	private RandomGenerator randomGenerator;
	private int minInfectiousPeriod;
	private int maxInfectiousPeriod;
	private double infectionInterval;

	/* start code_ref= groups_plugin_infection_manager_init|code_cap=The infection manager initializes by infecting the initially infected people in the first day. */
	public void init(ActorContext actorContext) {
		this.actorContext = actorContext;

		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		randomGenerator = stochasticsDataManager.getRandomGenerator();
		Random random = new Random(randomGenerator.nextLong());

		groupsDataManager = actorContext.getDataManager(GroupsDataManager.class);
		GlobalPropertiesDataManager globalPropertiesDataManager = actorContext
				.getDataManager(GlobalPropertiesDataManager.class);
		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		List<PersonId> susceptiblePeople = personPropertiesDataManager
				.getPeopleWithPropertyValue(PersonProperty.DISEASE_STATE, DiseaseState.SUSCEPTIBLE);
		List<PersonId> susceptibleAdults = new ArrayList<>();
		for (PersonId personId : susceptiblePeople) {
			int age = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.AGE);
			if (age > 18) {
				susceptibleAdults.add(personId);
			}
		}

		Collections.shuffle(susceptibleAdults, random);

		minInfectiousPeriod = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.MIN_INFECTIOUS_PERIOD);
		maxInfectiousPeriod = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.MAX_INFECTIOUS_PERIOD);
		double r0 = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.R0);
		infectionInterval = (double) (minInfectiousPeriod + maxInfectiousPeriod) / (2 * r0);

		int initialInfections = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.INITIAL_INFECTIONS);
		initialInfections = FastMath.min(initialInfections, susceptibleAdults.size());

		for (int i = 0; i < initialInfections; i++) {
			PersonId personId = susceptibleAdults.get(i);
			double planTime = randomGenerator.nextDouble() * 0.5 + 0.25;
			actorContext.addPlan((c) -> infectPerson(personId), planTime);
		}
	}
	/* end */

	/* start code_ref= groups_plugin_infection_manager_infect_person|code_cap= When a person is infected, the number of possible infectious contacts is determined and planned.  After the last infectious contact, the person is scheduled to become recovered.*/
	private void infectPerson(PersonId personId) {
		personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.DISEASE_STATE,
				DiseaseState.INFECTIOUS);
		int infectiousDays = randomGenerator.nextInt(maxInfectiousPeriod - minInfectiousPeriod) + minInfectiousPeriod;
		int infectionCount = (int) FastMath.round(((double) infectiousDays / infectionInterval));
		double planTime = actorContext.getTime();
		for (int j = 0; j < infectionCount; j++) {
			planTime += infectionInterval;
			actorContext.addPlan((c) -> infectContact(personId), planTime);
		}
		actorContext.addPlan((c) -> endInfectiousness(personId), planTime);
	}
	/* end */

	/* start code_ref= groups_plugin_infection_manager_infect_contact|code_cap= The infection manager attempts to infect a susceptible person found in a randomly selected group associated with the currently infected person. */
	private void infectContact(PersonId personId) {
		List<GroupId> groupsForPerson = groupsDataManager.getGroupsForPerson(personId);
		GroupId groupId = groupsForPerson.get(randomGenerator.nextInt(groupsForPerson.size()));

		// work groups doing telework have a 50% contact mitigation
		GroupTypeId groupTypeId = groupsDataManager.getGroupType(groupId);
		if (groupTypeId.equals(GroupType.WORK)) {
			boolean teleworkGroup = groupsDataManager.getGroupPropertyValue(groupId, GroupProperty.TELEWORK);
			if (teleworkGroup) {
				if (randomGenerator.nextBoolean()) {
					return;
				}
			}
		}

		// school groups in COHORT mode have a 50% contact mitigation
		// school groups in CLOSED mode have a 100% contact mitigation
		if (groupTypeId.equals(GroupType.SCHOOL)) {
			SchoolStatus schoolStatus = groupsDataManager.getGroupPropertyValue(groupId, GroupProperty.SCHOOL_STATUS);
			switch (schoolStatus) {
			case COHORT:
				if (randomGenerator.nextBoolean()) {
					return;
				}
				break;
			case CLOSED:
				return;
			default:
				// no mitigation
				break;
			}
		}

		GroupSampler groupSampler = GroupSampler.builder().setExcludedPersonId(personId).build();
		Optional<PersonId> optional = groupsDataManager.sampleGroup(groupId, groupSampler);
		if (optional.isPresent()) {
			PersonId contactedPerson = optional.get();
			DiseaseState diseaseState = personPropertiesDataManager.getPersonPropertyValue(contactedPerson,
					PersonProperty.DISEASE_STATE);
			if (diseaseState == DiseaseState.SUSCEPTIBLE) {
				int infectedCount = personPropertiesDataManager.getPersonPropertyValue(personId,
						PersonProperty.INFECTED_COUNT);
				infectedCount++;
				personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.INFECTED_COUNT,
						infectedCount);
				infectPerson(contactedPerson);
			}
		}
	}
	/* end */

	private void endInfectiousness(PersonId personId) {
		personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.DISEASE_STATE,
				DiseaseState.RECOVERED);
	}
}
