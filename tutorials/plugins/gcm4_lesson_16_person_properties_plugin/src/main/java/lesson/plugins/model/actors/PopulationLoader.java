package lesson.plugins.model.actors;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import lesson.plugins.model.GlobalProperty;
import lesson.plugins.model.PersonProperty;
import nucleus.ActorContext;
import plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonConstructionData.Builder;
import plugins.people.support.PersonId;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.personproperties.support.PersonPropertyDefinitionInitialization;
import plugins.personproperties.support.PersonPropertyInitialization;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionId;
import plugins.stochastics.StochasticsDataManager;
import plugins.util.properties.PropertyDefinition;

public class PopulationLoader {
	private RandomGenerator randomGenerator;
	private PeopleDataManager peopleDataManager;
	private PersonPropertiesDataManager personPropertiesDataManager;

	private void addImmunityProperty() {
		PersonPropertyDefinitionInitialization.Builder builder = PersonPropertyDefinitionInitialization.builder();
		builder.setPersonPropertyId(PersonProperty.IS_IMMUNE);
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).build();
		builder.setPropertyDefinition(propertyDefinition);
		for (PersonId personId : peopleDataManager.getPeople()) {
			boolean isImmune = randomGenerator.nextDouble() < 0.33;
			builder.addPropertyValue(personId, isImmune);
		}
		PersonPropertyDefinitionInitialization personPropertyDefinitionInitialization = builder.build();
		personPropertiesDataManager.definePersonProperty(personPropertyDefinitionInitialization);
	}

	public void init(ActorContext actorContext) {
		peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);
		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		GlobalPropertiesDataManager globalPropertiesDataManager = actorContext.getDataManager(GlobalPropertiesDataManager.class);
		RegionsDataManager regionsDataManager = actorContext.getDataManager(RegionsDataManager.class);
		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		randomGenerator = stochasticsDataManager.getRandomGenerator();
		List<RegionId> regionIds = new ArrayList<>(regionsDataManager.getRegionIds());

		int populationSize = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.POPULATION_SIZE);
		double refusalProbability = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.VACCINE_REFUSAL_PROBABILITY);

		Builder personConstructionDataBuilder = PersonConstructionData.builder();
		for (int i = 0; i < populationSize; i++) {
			RegionId regionId = regionIds.get(randomGenerator.nextInt(regionIds.size()));
			personConstructionDataBuilder.add(regionId);

			boolean refusesVaccine = randomGenerator.nextDouble() < refusalProbability;
			PersonPropertyInitialization personPropertyInitialization = new PersonPropertyInitialization(PersonProperty.REFUSES_VACCINE, refusesVaccine);
			personConstructionDataBuilder.add(personPropertyInitialization);
			PersonConstructionData personConstructionData = personConstructionDataBuilder.build();
			peopleDataManager.addPerson(personConstructionData);
		}

		double simulationDuration = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.SIMULATION_DURATION);
		actorContext.addPlan((c) -> c.halt(), simulationDuration);

		double immunityStartTime = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.IMMUNITY_START_TIME);
		actorContext.addPlan((c) -> addImmunityProperty(), immunityStartTime);
	}
}
