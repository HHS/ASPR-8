package lesson.plugins.model.actors.contactmanager;

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
import nucleus.Plan;
import nucleus.PlanData;
import nucleus.PrioritizedPlanData;
import plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import plugins.groups.datamanagers.GroupsDataManager;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupSampler;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonId;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.stochastics.StochasticsDataManager;

public class ContactManager {
	private final ContactManagerPluginData contactManagerPluginData;
	private ActorContext actorContext;
	private PersonPropertiesDataManager personPropertiesDataManager;
	private GroupsDataManager groupsDataManager;
	private PeopleDataManager peopleDataManager;
	private RandomGenerator randomGenerator;
	private int minInfectiousPeriod;
	private int maxInfectiousPeriod;
	private double infectionInterval;
	private double communityContactRate;

	public ContactManager(final ContactManagerPluginData contactManagerPluginData) {
		this.contactManagerPluginData = contactManagerPluginData;
	}

	protected void endInfectiousness(final PersonId personId) {
		personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.DISEASE_STATE, DiseaseState.RECOVERED);
	}

	protected void infectContact(final PersonId personId) {

		if (randomGenerator.nextDouble() < communityContactRate) {
			final List<PersonId> people = peopleDataManager.getPeople();
			people.remove(personId);
			if (people.size() > 0) {
				final PersonId contactedPerson = people.get(randomGenerator.nextInt(people.size()));
				final DiseaseState diseaseState = personPropertiesDataManager.getPersonPropertyValue(contactedPerson, PersonProperty.DISEASE_STATE);
				final boolean vaccinated = personPropertiesDataManager.getPersonPropertyValue(contactedPerson, PersonProperty.VACCINATED);
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
				final DiseaseState diseaseState = personPropertiesDataManager.getPersonPropertyValue(contactedPerson, PersonProperty.DISEASE_STATE);
				final boolean vaccinated = personPropertiesDataManager.getPersonPropertyValue(contactedPerson, PersonProperty.VACCINATED);
				if ((diseaseState == DiseaseState.SUSCEPTIBLE) && !vaccinated) {
					infectPerson(contactedPerson);
				}
			}
		}
	}

	protected void infectPerson(final PersonId personId) {
		personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.DISEASE_STATE, DiseaseState.INFECTIOUS);
		final int infectiousDays = randomGenerator.nextInt(maxInfectiousPeriod - minInfectiousPeriod) + minInfectiousPeriod;
		final int infectionCount = (int) FastMath.round((infectiousDays / infectionInterval));

		double planTime = actorContext.getTime();

		for (int j = 0; j < infectionCount; j++) {
			planTime += infectionInterval;

			final ContactPlanData contactPlanData = new ContactPlanData(personId, ContactAction.INFECT_CONTACT, planTime);
			final Plan<ActorContext> contactPlan = Plan	.builder(ActorContext.class)//
														.setCallbackConsumer((c) -> infectContact(personId))//
														.setTime(planTime)//
														.setPlanData(contactPlanData)//
														.build();//

			actorContext.addPlan(contactPlan);
		}

		final ContactPlanData contactPlanData = new ContactPlanData(personId, ContactAction.END_INFECTIOUSNESS, planTime);
		final Plan<ActorContext> endInfectiousnessPlan = Plan	.builder(ActorContext.class)//
																.setCallbackConsumer((c) -> endInfectiousness(personId))//
																.setTime(planTime)//
																.setPlanData(contactPlanData)//
																.build();//
		actorContext.addPlan(endInfectiousnessPlan);
	}

	public void init(final ActorContext actorContext) {
		this.actorContext = actorContext;

		// private int minInfectiousPeriod;
		// private int maxInfectiousPeriod;
		// private double infectionInterval;
		// private double communityContactRate;

		final StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		randomGenerator = stochasticsDataManager.getRandomGenerator();
		final Random random = new Random(randomGenerator.nextLong());

		peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);
		if (actorContext.stateRecordingIsScheduled()) {
			actorContext.subscribeToSimulationClose(this::recordSimulationState);
		}
		groupsDataManager = actorContext.getDataManager(GroupsDataManager.class);

		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);

		if (actorContext.getTime() == 0) {
			final GlobalPropertiesDataManager globalPropertiesDataManager = actorContext.getDataManager(GlobalPropertiesDataManager.class);
			communityContactRate = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.COMMUNITY_CONTACT_RATE);

			final List<PersonId> susceptiblePeople = personPropertiesDataManager.getPeopleWithPropertyValue(PersonProperty.DISEASE_STATE, DiseaseState.SUSCEPTIBLE);
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
				final double planTime = actorContext.getTime() + (randomGenerator.nextDouble() * 0.5) + 0.25;

				final ContactPlanData contactPlanData = new ContactPlanData(personId, ContactAction.INFECT_PERSON, planTime);
				final Plan<ActorContext> contactPlan = Plan	.builder(ActorContext.class)//
															.setCallbackConsumer((c) -> infectPerson(personId))//
															.setTime(planTime)//
															.setPlanData(contactPlanData)//
															.build();//

				actorContext.addPlan(contactPlan);
			}
		} else {
			infectionInterval = contactManagerPluginData.getInfectionInterval();
			minInfectiousPeriod = contactManagerPluginData.getMinInfectiousPeriod();
			maxInfectiousPeriod = contactManagerPluginData.getMaxInfectiousPeriod();
			communityContactRate = contactManagerPluginData.getCommunityContactRate();

			List<PrioritizedPlanData> prioritizedPlanDatas = contactManagerPluginData.getPrioritizedPlanDatas(ContactPlanData.class);
			for (PrioritizedPlanData prioritizedPlanData : prioritizedPlanDatas) {
				ContactPlanData contactPlanData = prioritizedPlanData.getPlanData();
				switch (contactPlanData.getContactAction()) {
				case END_INFECTIOUSNESS:

					final Plan<ActorContext> endInfectiousnessPlan = Plan	.builder(ActorContext.class)//
																			.setCallbackConsumer((c) -> endInfectiousness(contactPlanData.getPersonId()))//
																			.setTime(contactPlanData.getTime())//
																			.setPlanData(contactPlanData)//
																			.setPriority(prioritizedPlanData.getPriority())//
																			.build();//
					actorContext.addPlan(endInfectiousnessPlan);					
					break;
				case INFECT_CONTACT:
					
					final Plan<ActorContext> infectContactPlan = Plan	.builder(ActorContext.class)//
																.setCallbackConsumer((c) -> infectContact(contactPlanData.getPersonId()))//
																.setTime(contactPlanData.getTime())//
																.setPlanData(contactPlanData)//
																.setPriority(prioritizedPlanData.getPriority())//
																.build();//
					actorContext.addPlan(infectContactPlan);
					break;
				case INFECT_PERSON:
					
					final Plan<ActorContext> infectPersonPlan = Plan	.builder(ActorContext.class)//
																.setCallbackConsumer((c) -> infectPerson(contactPlanData.getPersonId()))//
																.setTime(contactPlanData.getTime())//
																.setPlanData(contactPlanData)//
																.setPriority(prioritizedPlanData.getPriority())//
																.build();//

					actorContext.addPlan(infectPersonPlan);
					break;
				default:
					throw new RuntimeException("unhandled case " + contactPlanData.getContactAction());

				}
			}
		}
	}

	private void recordSimulationState(final ActorContext actorContext) {

		final ContactManagerPluginData.Builder builder = ContactManagerPluginData.builder();
		final List<PrioritizedPlanData> prioritizedPlanDatas = actorContext.getTerminalActorPlanDatas(PlanData.class);
		for (final PrioritizedPlanData prioritizedPlanData : prioritizedPlanDatas) {
			builder.addPrioritizedPlanData(prioritizedPlanData);
		}

		builder.setInfectionInterval(infectionInterval);
		builder.setMinInfectiousPeriod(minInfectiousPeriod);
		builder.setMaxInfectiousPeriod(maxInfectiousPeriod);
		builder.setCommunityContactRate(communityContactRate);

		final ContactManagerPluginData contactManagerPluginData2 = builder.build();

		actorContext.releaseOutput(contactManagerPluginData2);

	}

}
