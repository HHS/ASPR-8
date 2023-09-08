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
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.PersonProperty;
import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.plugins.groups.datamanagers.GroupsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupId;
import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupSampler;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsDataManager;

public class ContactManager {
	private ActorContext actorContext;
	private PersonPropertiesDataManager personPropertiesDataManager;
	private GroupsDataManager groupsDataManager;
	private PeopleDataManager peopleDataManager;
	private RandomGenerator randomGenerator;
	private int minInfectiousPeriod;
	private int maxInfectiousPeriod;
	private double infectionInterval;
	private double communityContactRate;

	private void endInfectiousness(final PersonId personId) {
		personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.DISEASE_STATE,
				DiseaseState.RECOVERED);
	}

	/* start code_ref=materials_plugin_contact_manager_infect_contact|code_cap=The contact manager attempts to make an infectious contact from an infected person to a randomly selected susceptible person who is not vaccinated from either the general public or from one of the infected person's groups.  */
	private void infectContact(final PersonId personId) {

		if (randomGenerator.nextDouble() < communityContactRate) {
			final List<PersonId> people = peopleDataManager.getPeople();
			people.remove(personId);
			if (people.size() > 0) {
				final PersonId contactedPerson = people.get(randomGenerator.nextInt(people.size()));
				final DiseaseState diseaseState = personPropertiesDataManager.getPersonPropertyValue(contactedPerson,
						PersonProperty.DISEASE_STATE);
				final boolean vaccinated = personPropertiesDataManager.getPersonPropertyValue(contactedPerson,
						PersonProperty.VACCINATED);
				if ((diseaseState == DiseaseState.SUSCEPTIBLE) && !vaccinated) {
					infectPerson(contactedPerson);
				}
			}
		} else {
			final List<GroupId> groupsForPerson = groupsDataManager.getGroupsForPerson(personId);
			final GroupId groupId = groupsForPerson.get(randomGenerator.nextInt(groupsForPerson.size()));
			final GroupSampler groupSampler = GroupSampler.builder().setExcludedPersonId(personId).build();
			final Optional<PersonId> optional = groupsDataManager.sampleGroup(groupId, groupSampler);
			if (optional.isPresent()) {
				final PersonId contactedPerson = optional.get();
				final DiseaseState diseaseState = personPropertiesDataManager.getPersonPropertyValue(contactedPerson,
						PersonProperty.DISEASE_STATE);
				final boolean vaccinated = personPropertiesDataManager.getPersonPropertyValue(contactedPerson,
						PersonProperty.VACCINATED);
				if ((diseaseState == DiseaseState.SUSCEPTIBLE) && !vaccinated) {
					infectPerson(contactedPerson);
				}
			}
		}
	}
	/* end */

	/* start code_ref=materials_plugin_contact_manager_infect_person|code_cap= When a person is infected, the number of possible infectious contacts is determined and planned.  After the last infectious contact, the person is scheduled to become recovered. */
	private void infectPerson(final PersonId personId) {
		personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.DISEASE_STATE,
				DiseaseState.INFECTIOUS);
		final int infectiousDays = randomGenerator.nextInt(maxInfectiousPeriod - minInfectiousPeriod)
				+ minInfectiousPeriod;
		final int infectionCount = (int) FastMath.round((infectiousDays / infectionInterval));

		double planTime = actorContext.getTime();

		for (int j = 0; j < infectionCount; j++) {
			planTime += infectionInterval;
			actorContext.addPlan((c) -> infectContact(personId), planTime);
		}
		actorContext.addPlan((c) -> endInfectiousness(personId), planTime);
	}

	/* end */
	/* start code_ref=materials_plugin_contact_manager_init|code_cap=The contact manager initializes by infecting the initially infected people in the first day.*/
	public void init(final ActorContext actorContext) {
		this.actorContext = actorContext;

		final StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		randomGenerator = stochasticsDataManager.getRandomGenerator();
		final Random random = new Random(randomGenerator.nextLong());

		peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);

		groupsDataManager = actorContext.getDataManager(GroupsDataManager.class);
		final GlobalPropertiesDataManager globalPropertiesDataManager = actorContext
				.getDataManager(GlobalPropertiesDataManager.class);
		communityContactRate = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.COMMUNITY_CONTACT_RATE);

		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		final List<PersonId> susceptiblePeople = personPropertiesDataManager
				.getPeopleWithPropertyValue(PersonProperty.DISEASE_STATE, DiseaseState.SUSCEPTIBLE);
		final List<PersonId> susceptibleAdults = new ArrayList<>();
		for (final PersonId personId : susceptiblePeople) {
			final int age = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.AGE);
			if (age > 18) {
				susceptibleAdults.add(personId);
			}
		}

		Collections.shuffle(susceptibleAdults, random);

		minInfectiousPeriod = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.MIN_INFECTIOUS_PERIOD);
		maxInfectiousPeriod = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.MAX_INFECTIOUS_PERIOD);
		final double r0 = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.R0);
		infectionInterval = (minInfectiousPeriod + maxInfectiousPeriod) / (2 * r0);

		int initialInfections = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.INITIAL_INFECTIONS);
		initialInfections = FastMath.min(initialInfections, susceptibleAdults.size());

		for (int i = 0; i < initialInfections; i++) {
			final PersonId personId = susceptibleAdults.get(i);
			final double planTime = (randomGenerator.nextDouble() * 0.5) + 0.25;
			actorContext.addPlan((c) -> infectPerson(personId), planTime);
		}
	}
	/* end */

}
