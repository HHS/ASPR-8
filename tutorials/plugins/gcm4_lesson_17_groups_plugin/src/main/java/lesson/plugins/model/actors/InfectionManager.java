package lesson.plugins.model.actors;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import lesson.plugins.model.support.DiseaseState;
import lesson.plugins.model.support.GlobalProperty;
import lesson.plugins.model.support.GroupProperty;
import lesson.plugins.model.support.GroupType;
import lesson.plugins.model.support.PersonProperty;
import nucleus.ActorContext;
import plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import plugins.groups.datamanagers.GroupsDataManager;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupSampler;
import plugins.groups.support.GroupTypeId;
import plugins.people.support.PersonId;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.stochastics.StochasticsDataManager;

public class InfectionManager {
	private ActorContext actorContext;
	private PersonPropertiesDataManager personPropertiesDataManager;
	private GroupsDataManager groupsDataManager;
	private RandomGenerator randomGenerator;
	private int minInfectiousPeriod;
	private int maxInfectiousPeriod;
	private double infectionInterval;

	public void init(ActorContext actorContext) {
		this.actorContext = actorContext;

		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		randomGenerator = stochasticsDataManager.getRandomGenerator();
		Random random = new Random(randomGenerator.nextLong());

		groupsDataManager = actorContext.getDataManager(GroupsDataManager.class);
		GlobalPropertiesDataManager globalPropertiesDataManager = actorContext.getDataManager(GlobalPropertiesDataManager.class);
		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		List<PersonId> susceptiblePeople = personPropertiesDataManager.getPeopleWithPropertyValue(PersonProperty.DISEASE_STATE, DiseaseState.SUSCEPTIBLE);
		Collections.shuffle(susceptiblePeople, random);

		minInfectiousPeriod = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.MIN_INFECTIOUS_PERIOD);
		maxInfectiousPeriod = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.MAX_INFECTIOUS_PERIOD);
		double r0 = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.R0);
		infectionInterval = (double) (minInfectiousPeriod + maxInfectiousPeriod) / (2 * r0);

		int initialInfections = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.INITIAL_INFECTIONS);
		initialInfections = FastMath.min(initialInfections, susceptiblePeople.size());

		for (int i = 0; i < initialInfections; i++) {
			PersonId personId = susceptiblePeople.get(i);
			double planTime = randomGenerator.nextDouble() * 0.5 + 0.25;
			actorContext.addPlan((c) -> infectPerson(personId), planTime);
		}

	}

	private void infectPerson(PersonId personId) {
		personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.DISEASE_STATE, DiseaseState.INFECTIOUS);
		int infectiousDays = randomGenerator.nextInt(maxInfectiousPeriod - minInfectiousPeriod) + minInfectiousPeriod;
		int infectionCount = (int) FastMath.round(((double) infectiousDays / infectionInterval));
		double planTime = actorContext.getTime();
		for (int j = 0; j < infectionCount; j++) {
			planTime += infectionInterval;
			actorContext.addPlan((c) -> infectContact(personId), planTime);
		}
		actorContext.addPlan((c) -> endInfectiousness(personId), planTime);
	}

	private void infectContact(PersonId personId) {
		List<GroupId> groupsForPerson = groupsDataManager.getGroupsForPerson(personId);
		GroupId groupId = groupsForPerson.get(randomGenerator.nextInt(groupsForPerson.size()));
		
		//work groups doing telework have a 50% contact mitigation
		GroupTypeId groupTypeId = groupsDataManager.getGroupType(groupId);
		if (groupTypeId.equals(GroupType.WORK)) {
			boolean teleworkGroup = groupsDataManager.getGroupPropertyValue(groupId, GroupProperty.TELEWORK);
			if (teleworkGroup) {
				if (randomGenerator.nextBoolean()) {
					return;
				}
			}
		}
		GroupSampler groupSampler = GroupSampler.builder().setExcludedPersonId(personId).build();
		Optional<PersonId> optional = groupsDataManager.sampleGroup(groupId, groupSampler);
		if (optional.isPresent()) {
			PersonId contactedPerson = optional.get();
			DiseaseState diseaseState = personPropertiesDataManager.getPersonPropertyValue(contactedPerson, PersonProperty.DISEASE_STATE);
			if (diseaseState == DiseaseState.SUSCEPTIBLE) {
				int infectedCount = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.INFECTED_COUNT);
				infectedCount++;
				personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.INFECTED_COUNT, infectedCount);
				infectPerson(contactedPerson);
			}
		}
	}

	private void endInfectiousness(PersonId personId) {
		personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.DISEASE_STATE, DiseaseState.RECOVERED);
	}
}
