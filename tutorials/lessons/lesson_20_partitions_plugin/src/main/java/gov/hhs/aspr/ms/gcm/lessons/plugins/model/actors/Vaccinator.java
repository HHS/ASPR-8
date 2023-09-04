package gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors;

import java.util.Optional;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.AgeGroup;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.DiseaseState;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.GlobalProperty;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.PersonProperty;
import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.plugins.partitions.datamanagers.PartitionsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.Equality;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.LabelSet;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.Partition;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PartitionSampler;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PartitionsContext;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.support.PersonPropertyFilter;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.support.PersonPropertyLabeler;

public class Vaccinator {

	private Object partitionKey = "key";
	private PartitionsDataManager partitionsDataManager;
	private PersonPropertiesDataManager personPropertiesDataManager;
	private double interVaccinationTime;
	private ActorContext actorContext;

	public void init(ActorContext actorContext) {
		this.actorContext = actorContext;
		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		partitionsDataManager = actorContext.getDataManager(PartitionsDataManager.class);
		GlobalPropertiesDataManager globalPropertiesDataManager = actorContext
				.getDataManager(GlobalPropertiesDataManager.class);
		int vaccinationsPerDay = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.VACCINATIONS_PER_DAY);
		interVaccinationTime = 1.0 / vaccinationsPerDay;

		// only adults
		PersonPropertyFilter ageFilter = new PersonPropertyFilter(PersonProperty.AGE, Equality.GREATER_THAN_EQUAL, 18);

		// only susceptible
		PersonPropertyFilter diseaseFilter = new PersonPropertyFilter(PersonProperty.DISEASE_STATE, Equality.EQUAL,
				DiseaseState.SUSCEPTIBLE);

		// only vaccine count <3
		PersonPropertyFilter vaccineFilter = new PersonPropertyFilter(PersonProperty.VACCINATION_COUNT,
				Equality.LESS_THAN, 3);

		PersonPropertyLabeler ageLabeler = new PersonPropertyLabeler(PersonProperty.AGE) {
			@Override
			protected Object getLabelFromValue(Object value) {
				int age = (Integer) value;
				return AgeGroup.getAgeGroup(age);
			}
		};

		PersonPropertyLabeler vaccineCountLabeler = new PersonPropertyLabeler(PersonProperty.VACCINATION_COUNT) {
			@Override
			protected Object getLabelFromValue(Object value) {
				return value;
			}
		};

		Partition partition = Partition.builder()//
				.setFilter(ageFilter.and(diseaseFilter).and(vaccineFilter))//
				.addLabeler(ageLabeler)//
				.addLabeler(vaccineCountLabeler)//
				.build();

		partitionsDataManager.addPartition(partition, partitionKey);

		planNextVaccination();

	}

	private void planNextVaccination() {
		if (partitionsDataManager.getPersonCount(partitionKey) == 0) {
			return;
		}
		actorContext.addPlan(this::vaccinatePerson, interVaccinationTime + actorContext.getTime());
	}

	private double getWeight(PartitionsContext partitionsContext, LabelSet labelSet) {

		AgeGroup ageGroup = (AgeGroup) labelSet.getLabel(PersonProperty.AGE).get();

		int vaccineCount = (Integer) labelSet.getLabel(PersonProperty.VACCINATION_COUNT).get();

		double result = 1;

		switch (ageGroup) {
		case ADULT_18_30:
			result += 0;
			break;
		case ADULT_30_55:
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

	private void vaccinatePerson(ActorContext actorContext) {

		PartitionSampler partitionSampler = PartitionSampler.builder()//
				.setLabelSetWeightingFunction(this::getWeight)//
				.build();

		Optional<PersonId> optionalPersonId = partitionsDataManager.samplePartition(partitionKey, partitionSampler);
		if (optionalPersonId.isPresent()) {
			PersonId personId = optionalPersonId.get();
			int vaccinationCount = personPropertiesDataManager.getPersonPropertyValue(personId,
					PersonProperty.VACCINATION_COUNT);
			vaccinationCount++;
			personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.VACCINATION_COUNT,
					vaccinationCount);
		}
		planNextVaccination();
	}

}
