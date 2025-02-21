package gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors;

import java.util.Optional;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.AgeGroup;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.DiseaseState;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.GlobalProperty;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.PersonProperty;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.datamanagers.PartitionsDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.Equality;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.LabelSet;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.Labeler;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.Partition;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.PartitionSampler;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.PartitionsContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.filters.Filter;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.support.FunctionalPersonPropertyLabeler;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.support.PersonPropertyFilter;

public class PartitionVaccinator {

	private Object currentlyEligibleKey = new Object();
	private Object potentiallyEligibleKey = new Object();
	private PartitionsDataManager partitionsDataManager;
	private PersonPropertiesDataManager personPropertiesDataManager;
	private GlobalPropertiesDataManager globalPropertiesDataManager;
	private double vaccinatorDelay;
	private double personInterVaccinationDelay;
	private ActorContext actorContext;

	/*
	 * start code_ref=partitions_plugin_partition_init|code_cap= The
	 * partition-vaccinator manages the eligible population via partitions.
	 */
	public void init(ActorContext actorContext) {
		this.actorContext = actorContext;
		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		partitionsDataManager = actorContext.getDataManager(PartitionsDataManager.class);
		globalPropertiesDataManager = actorContext.getDataManager(GlobalPropertiesDataManager.class);
		establishWorkingVariables();
		createPartitions();
		planNextVaccination();
	}
	/* end */

	/*
	 * start code_ref=partitions_plugin_partition_create_partitions|code_cap= The
	 * partition-vaccinator creates two partitions to help with person selection and
	 * termination of vaccinations.
	 */
	private void createPartitions() {

		PersonPropertyFilter ageFilter = new PersonPropertyFilter(PersonProperty.AGE, Equality.GREATER_THAN_EQUAL, 18);

		PersonPropertyFilter diseaseFilter = new PersonPropertyFilter(PersonProperty.DISEASE_STATE, Equality.EQUAL,
				DiseaseState.SUSCEPTIBLE);

		PersonPropertyFilter vaccineFilter = new PersonPropertyFilter(PersonProperty.VACCINATION_COUNT,
				Equality.LESS_THAN, 3);

		PersonPropertyFilter waitFilter = new PersonPropertyFilter(PersonProperty.WAITING_FOR_NEXT_DOSE, Equality.EQUAL,
				false);

		Filter filter = ageFilter.and(diseaseFilter).and(vaccineFilter).and(waitFilter);

		Labeler ageLabeler = new FunctionalPersonPropertyLabeler(PersonProperty.AGE,
				(value) -> AgeGroup.getAgeGroup((Integer) value));

		Labeler vaccineCountLabeler = new FunctionalPersonPropertyLabeler(PersonProperty.VACCINATION_COUNT,
				(value) -> value);

		Partition partition = Partition.builder()//
				.setFilter(filter)//
				.addLabeler(ageLabeler)//
				.addLabeler(vaccineCountLabeler)//
				.build();

		partitionsDataManager.addPartition(partition, currentlyEligibleKey);

		filter = ageFilter.and(diseaseFilter).and(vaccineFilter);
		partition = Partition.builder()//
				.setFilter(filter)//
				.build();

		partitionsDataManager.addPartition(partition, potentiallyEligibleKey);
	}
	/* end */

	private void establishWorkingVariables() {
		int vaccinationsPerDay = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.VACCINATIONS_PER_DAY);

		personInterVaccinationDelay = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.INTER_VACCINATION_DELAY_TIME);
		vaccinatorDelay = 1.0 / vaccinationsPerDay;
	}

	private void planWaitTermination(PersonId personId) {
		actorContext.addPlan((c) -> this.endWaitTime(personId), personInterVaccinationDelay + actorContext.getTime());
	}

	private void endWaitTime(PersonId personId) {
		personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.WAITING_FOR_NEXT_DOSE, false);
	}

	private double getWeight(PartitionsContext partitionsContext, LabelSet labelSet) {

		AgeGroup ageGroup = (AgeGroup) labelSet.getLabel(PersonProperty.AGE).get();

		int vaccineCount = (Integer) labelSet.getLabel(PersonProperty.VACCINATION_COUNT).get();

		double result = 1;

		switch (ageGroup) {
		case ADULT_18_44:
			result += 0;
			break;
		case ADULT_45_64:
			result += 3;
			break;
		case CHILD:
			result += 0;
			break;
		case SENIOR:
			result += 10;
			break;
		default:
			break;
		}

		switch (vaccineCount) {
		case 0:
			result += 4;
			break;
		case 1:
			result += 3;
			break;
		case 2:
			result += 2;
			break;
		default:
			result += 0;
			break;
		}

		return result;
	}

	/*
	 * start code_ref=partitions_plugin_partition_vaccinate|code_cap= The
	 * partition-vaccinator schedules and executes vaccinations using partitions.
	 */
	private void planNextVaccination() {
		if (partitionsDataManager.getPersonCount(potentiallyEligibleKey) == 0) {
			return;
		}
		actorContext.addPlan(this::vaccinatePerson, vaccinatorDelay + actorContext.getTime());
	}

	private void vaccinatePerson(ActorContext actorContext) {

		PartitionSampler partitionSampler = PartitionSampler.builder()//
				.setLabelSetWeightingFunction(this::getWeight)//
				.build();

		Optional<PersonId> optionalPersonId = partitionsDataManager.samplePartition(currentlyEligibleKey,
				partitionSampler);
		if (optionalPersonId.isPresent()) {
			PersonId personId = optionalPersonId.get();
			int vaccinationCount = personPropertiesDataManager.getPersonPropertyValue(personId,
					PersonProperty.VACCINATION_COUNT);
			vaccinationCount++;
			personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.VACCINATION_COUNT,
					vaccinationCount);
			if (vaccinationCount < 3) {
				personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.WAITING_FOR_NEXT_DOSE,
						true);
				planWaitTermination(personId);
			}

		}
		planNextVaccination();
	}
	/* end */

}
