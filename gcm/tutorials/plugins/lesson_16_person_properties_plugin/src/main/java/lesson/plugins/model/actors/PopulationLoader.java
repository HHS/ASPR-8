package lesson.plugins.model.actors;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonConstructionData;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonConstructionData.Builder;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.support.PersonPropertyDefinitionInitialization;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.support.PersonPropertyValueInitialization;
import gov.hhs.aspr.ms.gcm.plugins.regions.datamanagers.RegionsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionId;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyDefinition;
import lesson.plugins.model.GlobalProperty;
import lesson.plugins.model.PersonProperty;

public class PopulationLoader {
	private RandomGenerator randomGenerator;
	private PeopleDataManager peopleDataManager;
	private PersonPropertiesDataManager personPropertiesDataManager;
	private GlobalPropertiesDataManager globalPropertiesDataManager;

	/* start code_ref= person_properties_population_loader_add_immunity_property */
	private void addImmunityProperty() {
		PersonPropertyDefinitionInitialization.Builder builder = PersonPropertyDefinitionInitialization.builder();
		builder.setPersonPropertyId(PersonProperty.IS_IMMUNE);
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).build();
		builder.setPropertyDefinition(propertyDefinition);
		double immunityProbability = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.IMMUNITY_PROBABILITY);

		for (PersonId personId : peopleDataManager.getPeople()) {
			boolean isImmune = randomGenerator.nextDouble() < immunityProbability;
			builder.addPropertyValue(personId, isImmune);
		}
		PersonPropertyDefinitionInitialization personPropertyDefinitionInitialization = builder.build();
		personPropertiesDataManager.definePersonProperty(personPropertyDefinitionInitialization);
	}
	/* end */

	/* start code_ref= person_properties_population_loader_init */
	public void init(ActorContext actorContext) {
		peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);
		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		globalPropertiesDataManager = actorContext.getDataManager(GlobalPropertiesDataManager.class);
		RegionsDataManager regionsDataManager = actorContext.getDataManager(RegionsDataManager.class);
		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		randomGenerator = stochasticsDataManager.getRandomGenerator();
		List<RegionId> regionIds = new ArrayList<>(regionsDataManager.getRegionIds());

		int populationSize = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.POPULATION_SIZE);
		double refusalProbability = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.VACCINE_REFUSAL_PROBABILITY);

		Builder personConstructionDataBuilder = PersonConstructionData.builder();
		for (int i = 0; i < populationSize; i++) {
			RegionId regionId = regionIds.get(randomGenerator.nextInt(regionIds.size()));
			personConstructionDataBuilder.add(regionId);

			boolean refusesVaccine = randomGenerator.nextDouble() < refusalProbability;
			PersonPropertyValueInitialization personPropertyInitialization = new PersonPropertyValueInitialization(
					PersonProperty.REFUSES_VACCINE, refusesVaccine);
			personConstructionDataBuilder.add(personPropertyInitialization);
			PersonConstructionData personConstructionData = personConstructionDataBuilder.build();
			peopleDataManager.addPerson(personConstructionData);
		}

		double simulationDuration = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.SIMULATION_DURATION);
		actorContext.addPlan((c) -> c.halt(), simulationDuration);

		double immunityStartTime = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.IMMUNITY_START_TIME);
		actorContext.addPlan((c) -> addImmunityProperty(), immunityStartTime);
	}
	/* end */
}
