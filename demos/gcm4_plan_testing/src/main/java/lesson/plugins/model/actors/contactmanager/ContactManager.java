package lesson.plugins.model.actors.contactmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import lesson.plugins.model.support.DiseaseState;
import lesson.plugins.model.support.GlobalProperty;
import lesson.plugins.model.support.PersonProperty;
import nucleus.ActorContext;
import nucleus.Plan;
import plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import plugins.groups.datamanagers.GroupsDataManager;
import plugins.groups.support.GroupId;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonId;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.stochastics.StochasticsDataManager;
import plugins.stochastics.support.Well;

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

	public static boolean LOG_ACTIVE = true;

	private void log(Object... values) {
		if (LOG_ACTIVE) {
			StringBuilder sb = new StringBuilder();
			sb.append("Contact Manager : ");
			sb.append(actorContext.getTime());
			sb.append(" :");
			for (Object value : values) {
				sb.append(" ");
				sb.append(String.valueOf(value));
			}
			System.out.println(sb);
		}
	}

	public ContactManager(final ContactManagerPluginData contactManagerPluginData) {
		this.contactManagerPluginData = contactManagerPluginData;
	}

	protected void endInfectiousness(final PersonId personId) {
		log("ending infectiousness for person ", personId);
		reportRNG();
		personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.DISEASE_STATE, DiseaseState.RECOVERED);
	}

	protected void infectContact(final PersonId personId) {
		log("resolving infectious contact for person ", personId);
		reportRNG();

		if (randomGenerator.nextDouble() < communityContactRate) {
			final List<PersonId> people = peopleDataManager.getPeople();
			people.remove(personId);
			if (people.size() > 0) {
				final PersonId contactedPerson = people.get(randomGenerator.nextInt(people.size()));
				final DiseaseState diseaseState = personPropertiesDataManager.getPersonPropertyValue(contactedPerson, PersonProperty.DISEASE_STATE);
				final boolean vaccinated = personPropertiesDataManager.getPersonPropertyValue(contactedPerson, PersonProperty.VACCINATED);
				if ((diseaseState == DiseaseState.SUSCEPTIBLE) && !vaccinated) {
					log("person", personId, "infects person", contactedPerson);
					infectPerson(contactedPerson);
				}
			}
		} else {
			final List<GroupId> groupsForPerson = groupsDataManager.getGroupsForPerson(personId);
			Collections.sort(groupsForPerson);
			int index = randomGenerator.nextInt(groupsForPerson.size());
			final GroupId groupId = groupsForPerson.get(index);
			List<PersonId> peopleForGroup = groupsDataManager.getPeopleForGroup(groupId);
			peopleForGroup.remove(personId);

			// final GroupSampler groupSampler =
			// GroupSampler.builder().setExcludedPersonId(personId).build();
			// final Optional<PersonId> optional =
			// groupsDataManager.sampleGroup(groupId, groupSampler);
			// if (optional.isPresent()) {
			if (!peopleForGroup.isEmpty()) {
				Collections.sort(peopleForGroup);
				index = randomGenerator.nextInt(peopleForGroup.size());
				final PersonId contactedPerson = peopleForGroup.get(index);
				final DiseaseState diseaseState = personPropertiesDataManager.getPersonPropertyValue(contactedPerson, PersonProperty.DISEASE_STATE);
				final boolean vaccinated = personPropertiesDataManager.getPersonPropertyValue(contactedPerson, PersonProperty.VACCINATED);
				if ((diseaseState == DiseaseState.SUSCEPTIBLE) && !vaccinated) {
					log("person", personId, "infects person", contactedPerson);
					infectPerson(contactedPerson);
				}
			}
		}
	}

	protected void infectPerson(final PersonId personId) {
		log("infecting person", personId);
		reportRNG();
		personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.DISEASE_STATE, DiseaseState.INFECTIOUS);
		final int infectiousDays = randomGenerator.nextInt(maxInfectiousPeriod - minInfectiousPeriod) + minInfectiousPeriod;
		final int infectionCount = (int) FastMath.round((infectiousDays / infectionInterval));

		double planTime = actorContext.getTime();

		for (int j = 0; j < infectionCount; j++) {
			log("planning infectious contact for person", personId);
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

	private void reportRNG() {
		Well wellRNG = (Well) randomGenerator;
		log("rng index =", wellRNG.getWellState().getIndex());
	}

	private Consumer<ActorContext> getConsumerFromContactPlanData(ContactPlanData contactPlanData) {		
		switch (contactPlanData.getContactAction()) {
		case END_INFECTIOUSNESS:
			return (c) -> endInfectiousness(contactPlanData.getPersonId());

		case INFECT_CONTACT:
			return (c) -> infectContact(contactPlanData.getPersonId());

		case INFECT_PERSON:
			return (c) -> infectPerson(contactPlanData.getPersonId());

		default:
			throw new RuntimeException("unhandled case " + contactPlanData.getContactAction());
		}
	}

	public void init(final ActorContext actorContext) {

		this.actorContext = actorContext;

		final StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		randomGenerator = stochasticsDataManager.getRandomGenerator();

		peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);
		if (actorContext.stateRecordingIsScheduled()) {
			actorContext.subscribeToSimulationClose(this::recordSimulationState);
		}
		groupsDataManager = actorContext.getDataManager(GroupsDataManager.class);

		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		actorContext.setPlanDataConverter(ContactPlanData.class, this::getConsumerFromContactPlanData);

		if (actorContext.getTime() == 0) {
			final Random random = new Random(randomGenerator.nextLong());
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

			Collections.sort(susceptibleAdults);
			Collections.shuffle(susceptibleAdults, random);

			minInfectiousPeriod = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.MIN_INFECTIOUS_PERIOD);
			maxInfectiousPeriod = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.MAX_INFECTIOUS_PERIOD);
			final double r0 = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.R0);
			infectionInterval = (minInfectiousPeriod + maxInfectiousPeriod) / (2 * r0);

			int initialInfections = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.INITIAL_INFECTIONS);
			initialInfections = FastMath.min(initialInfections, susceptibleAdults.size());

			for (int i = 0; i < initialInfections; i++) {
				final PersonId personId = susceptibleAdults.get(i);
				log("selecting person", personId, "for initial infection");
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
		}

	}

	private void recordSimulationState(final ActorContext actorContext) {
		actorContext.releaseOutput(//
				ContactManagerPluginData.builder()//
										.setInfectionInterval(infectionInterval)//
										.setMinInfectiousPeriod(minInfectiousPeriod)//
										.setMaxInfectiousPeriod(maxInfectiousPeriod)//
										.setCommunityContactRate(communityContactRate)//
										.build());//

	}

}
