
package gov.hhs.aspr.ms.gcm.lessons.plugins.model;

import org.apache.commons.math3.random.RandomGenerator;

import gov.hhs.aspr.ms.gcm.lessons.plugins.family.datamanagers.FamilyDataManager;
import gov.hhs.aspr.ms.gcm.lessons.plugins.family.support.FamilyId;
import gov.hhs.aspr.ms.gcm.lessons.plugins.person.datamanagers.PersonDataManager;
import gov.hhs.aspr.ms.gcm.lessons.plugins.person.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.datamanagers.StochasticsDataManager;

public final class PopulationLoader {

	public void init(ActorContext actorContext) {

		// get the data managers that will be needed to add people and families
		PersonDataManager personDataManager = actorContext.getDataManager(PersonDataManager.class);
		FamilyDataManager familyDataManager = actorContext.getDataManager(FamilyDataManager.class);
		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

		int familyCount = familyDataManager.getInitialFamilyCount();
		int maxFamilySize = familyDataManager.getMaxFamilySize();

		// add people in families
		for (int i = 0; i < familyCount; i++) {
			FamilyId familyId = familyDataManager.addFamily();
			int familySize;
			if (maxFamilySize < 3) {
				familySize = maxFamilySize;
			} else {
				familySize = randomGenerator.nextInt(maxFamilySize - 2) + 2;
			}

			for (int j = 0; j < familySize; j++) {
				PersonId personId = personDataManager.addPerson();
				familyDataManager.addFamilyMember(personId, familyId);
			}
		}

		// add some more individuals
		int individualCount = familyCount / 10;
		for (int i = 0; i < individualCount; i++) {
			personDataManager.addPerson();
		}
	}

}
