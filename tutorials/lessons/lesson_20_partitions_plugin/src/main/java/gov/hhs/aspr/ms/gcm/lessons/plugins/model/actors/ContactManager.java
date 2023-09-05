package gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors;

import java.util.List;
import java.util.Optional;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.DiseaseState;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.GlobalProperty;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.PersonProperty;
import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.plugins.partitions.datamanagers.PartitionsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.Partition;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PartitionSampler;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.Well;

public class ContactManager {

	private double minimumInfectiousPeriod;
	private double maximumInfectiousPeriod;
	private double infectiousContactRate;
	private int infectionCount;
	private double transmissionProbabilty;

	private final Object partitionKey = new Object();

	private GlobalPropertiesDataManager globalPropertiesDataManager;
	private PersonPropertiesDataManager personPropertiesDataManager;
	private PartitionsDataManager partitionsDataManager;
	private PeopleDataManager peopleDataManager;

	private ActorContext actorContext;
	private Well randomGenerator;

	private void loadGlobalProperties() {
		minimumInfectiousPeriod = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.MINIMUM_INFECTIOUS_PERIOD);

		maximumInfectiousPeriod = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.MAXIMUM_INFECTIOUS_PERIOD);

		if (minimumInfectiousPeriod > maximumInfectiousPeriod) {
			throw new RuntimeException("Minimum infectious period exceeds maximum infectious period");
		}

		infectiousContactRate = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.INFECTIOUS_CONTACT_RATE);

		if (infectiousContactRate < 0) {
			throw new RuntimeException("infectious contact rate is negative");
		}

		transmissionProbabilty = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.TRANSMISSION_PROBABILTY);
		if (transmissionProbabilty < 0 || transmissionProbabilty > 1) {
			throw new RuntimeException("transmission probability out of bounds[0,1]");
		}
		infectionCount = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.INITIAL_INFECTION_COUNT);
	}

	private void initializeInfections() {
		List<PersonId> people = peopleDataManager.getPeople();

		if (infectionCount > people.size()) {
			throw new RuntimeException("Initial infectious count exceeds population size");
		}

		for (int i = 0; i < infectionCount; i++) {
			PersonId personId = people.get(i);
			infectPerson(personId);
		}
	}

	private void infectPerson(PersonId personId) {
		personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.DISEASE_STATE,
				DiseaseState.INFECTIOUS);

		double infectiousPeriod = randomGenerator.nextDouble() * (maximumInfectiousPeriod - minimumInfectiousPeriod)
				+ minimumInfectiousPeriod;

		double lastContactTime = actorContext.getTime() + infectiousPeriod;

		double infectionTime = actorContext.getTime();
		while (true) {
			double contactDelay = (1.0 / infectiousContactRate);
			contactDelay *= (1 + randomGenerator.nextDouble() / 5 - 0.1);
			infectionTime += contactDelay;

			if (infectionTime < lastContactTime) {
				actorContext.addPlan((c2) -> {
					processInfectiousContact(personId);
				}, infectionTime);
			} else {
				break;
			}
		}
		actorContext.addPlan((c2) -> {
			endInfectiousness(personId);
		}, lastContactTime);

	}

	private void endInfectiousness(PersonId personId) {
		personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.DISEASE_STATE,
				DiseaseState.RECOVERED);
	}

	private void establishPopulationPartition() {
		partitionsDataManager.addPartition(Partition.builder().build(), partitionKey);
	}

	public void init(ActorContext actorContext) {
		this.actorContext = actorContext;
		partitionsDataManager = actorContext.getDataManager(PartitionsDataManager.class);
		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);
		globalPropertiesDataManager = actorContext.getDataManager(GlobalPropertiesDataManager.class);
		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		randomGenerator = stochasticsDataManager.getRandomGenerator();

		loadGlobalProperties();
		establishPopulationPartition();
		initializeInfections();

	}

	private void processInfectiousContact(PersonId personId) {
		
		PartitionSampler partitionSampler = PartitionSampler.builder().setExcludedPerson(personId).build();
		Optional<PersonId> optionalPersonId = partitionsDataManager.samplePartition(partitionKey, partitionSampler);
		if (optionalPersonId.isPresent()) {

			PersonId contactedPersonId = optionalPersonId.get();
			

			DiseaseState diseaseState = personPropertiesDataManager.getPersonPropertyValue(contactedPersonId,
					PersonProperty.DISEASE_STATE);
			if (diseaseState == DiseaseState.SUSCEPTIBLE) {

				int vaccinationCount = personPropertiesDataManager.getPersonPropertyValue(contactedPersonId,
						PersonProperty.VACCINATION_COUNT);
				double mitigatedTransmissionProbability;

				switch (vaccinationCount) {
				case 0:
					mitigatedTransmissionProbability = 1;
					break;
				case 1:
					mitigatedTransmissionProbability = 0.5;
					break;
				case 2:
					mitigatedTransmissionProbability = 0.2;
					break;
				default:
					mitigatedTransmissionProbability = 0;
					break;
				}

				mitigatedTransmissionProbability *= transmissionProbabilty;

				if (randomGenerator.nextDouble() < mitigatedTransmissionProbability) {
					infectPerson(contactedPersonId);
				}
			}
		}

	}
}
