package lesson.plugins.model.actors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import lesson.plugins.model.support.DiseaseState;
import lesson.plugins.model.support.GlobalProperty;
import lesson.plugins.model.support.PersonProperty;
import nucleus.ActorContext;
import plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import plugins.groups.datamanagers.GroupsDataManager;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupSampler;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonId;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.stochastics.StochasticsDataManager;

public class InfectionManager {
	private ActorContext actorContext;
	private PersonPropertiesDataManager personPropertiesDataManager;
	private GroupsDataManager groupsDataManager;
	private PeopleDataManager peopleDataManager;
	private RandomGenerator randomGenerator;
	private int minInfectiousPeriod;
	private int maxInfectiousPeriod;
	private double infectionInterval;
	private double communityContactRate;

	public void init(ActorContext actorContext) {
		this.actorContext = actorContext;

		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		randomGenerator = stochasticsDataManager.getRandomGenerator();
		Random random = new Random(randomGenerator.nextLong());

		peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);

		groupsDataManager = actorContext.getDataManager(GroupsDataManager.class);
		GlobalPropertiesDataManager globalPropertiesDataManager = actorContext.getDataManager(GlobalPropertiesDataManager.class);
		communityContactRate = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.COMMUNITY_CONTACT_RATE);
		 
		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		List<PersonId> susceptiblePeople = personPropertiesDataManager.getPeopleWithPropertyValue(PersonProperty.DISEASE_STATE, DiseaseState.SUSCEPTIBLE);
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

	private void infectPerson(PersonId personId) {
		personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.DISEASE_STATE, DiseaseState.INFECTIOUS);
		int infectiousDays = randomGenerator.nextInt(maxInfectiousPeriod - minInfectiousPeriod) + minInfectiousPeriod;
		int infectionCount = (int) FastMath.round(((double) infectiousDays / infectionInterval));
		personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.CONTACT_COUNT, infectionCount);
		double planTime = actorContext.getTime();
		for (int j = 0; j < infectionCount; j++) {
			planTime += infectionInterval;
			actorContext.addPlan((c) -> infectContact(personId), planTime);
		}
		actorContext.addPlan((c) -> endInfectiousness(personId), planTime);
	}

	private void infectContact(PersonId personId) {
		
		if (randomGenerator.nextDouble() < communityContactRate) {
			List<PersonId> people = peopleDataManager.getPeople();
			people.remove(personId);
			if (people.size() > 0) {
				PersonId contactedPerson = people.get(randomGenerator.nextInt(people.size()));
				DiseaseState diseaseState = personPropertiesDataManager.getPersonPropertyValue(contactedPerson, PersonProperty.DISEASE_STATE);
				if (diseaseState == DiseaseState.SUSCEPTIBLE) {
					infectPerson(contactedPerson);
				}
			}
		} else {
			List<GroupId> groupsForPerson = groupsDataManager.getGroupsForPerson(personId);
			GroupId groupId = groupsForPerson.get(randomGenerator.nextInt(groupsForPerson.size()));
			GroupSampler groupSampler = GroupSampler.builder().setExcludedPersonId(personId).build();
			Optional<PersonId> optional = groupsDataManager.sampleGroup(groupId, groupSampler);
			if (optional.isPresent()) {
				PersonId contactedPerson = optional.get();
				DiseaseState diseaseState = personPropertiesDataManager.getPersonPropertyValue(contactedPerson, PersonProperty.DISEASE_STATE);
				if (diseaseState == DiseaseState.SUSCEPTIBLE) {
					infectPerson(contactedPerson);
				}
			}
		}
	}

	private void endInfectiousness(PersonId personId) {
		personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.DISEASE_STATE, DiseaseState.RECOVERED);
	}
}
