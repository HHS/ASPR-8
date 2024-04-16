package gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.AgeGroup;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.GlobalProperty;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.PersonProperty;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.Region;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonConstructionData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.support.PersonPropertyValueInitialization;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.datamanagers.StochasticsDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.support.Well;

/*start code_ref=partitions_plugin_population_loader|code_cap=People are added to the simulation with region and age assignments.*/
public class PopulationLoader {
	public void init(ActorContext actorContext) {
		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		Well randomGenerator = stochasticsDataManager.getRandomGenerator();
		PeopleDataManager peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);
		GlobalPropertiesDataManager globalPropertiesDataManager = actorContext
				.getDataManager(GlobalPropertiesDataManager.class);
		Integer populationSize = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.POPULATION_SIZE);
		for (int i = 0; i < populationSize; i++) {
			Region region = Region.getRandomRegion(randomGenerator);
			int age = AgeGroup.getRandomAge(randomGenerator);
			PersonPropertyValueInitialization personPropertyValueInitialization = new PersonPropertyValueInitialization(PersonProperty.AGE,age);
			PersonConstructionData personConstructionData = PersonConstructionData.builder()//
					.add(region)//
					.add(personPropertyValueInitialization)
					.build();//
			peopleDataManager.addPerson(personConstructionData);
		}
	}
}
/* end */
