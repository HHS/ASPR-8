
package gov.hhs.aspr.ms.gcm.lessons.plugins.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import gov.hhs.aspr.ms.gcm.lessons.plugins.family.FamilyDataManager;
import gov.hhs.aspr.ms.gcm.lessons.plugins.family.FamilyId;
import gov.hhs.aspr.ms.gcm.lessons.plugins.people.PersonDataManager;
import gov.hhs.aspr.ms.gcm.lessons.plugins.people.PersonId;
import gov.hhs.aspr.ms.gcm.lessons.plugins.vaccine.VaccinationDataManager;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.ActorContext;

public final class ModelActor {

	public void init(ActorContext actorContext) {

		// get the data managers that will be needed to add people and families
		PersonDataManager personDataManager = actorContext.getDataManager(PersonDataManager.class);
		FamilyDataManager familyDataManager = actorContext.getDataManager(FamilyDataManager.class);

		// get a source of randomness
		Random random = new Random(123L);

		// add people in families
		for (int i = 0; i < 4; i++) {
			FamilyId familyId = familyDataManager.addFamily();
			int familySize = random.nextInt(5);
			for (int j = 0; j < familySize; j++) {
				PersonId personId = personDataManager.addPerson();
				familyDataManager.addFamilyMember(personId, familyId);
			}
		}

		// add extra people not in families
		for (int i = 0; i < 3; i++) {
			personDataManager.addPerson();
		}

		// plan out randomized vaccinations, one person per day
		List<PersonId> people = new ArrayList<>(personDataManager.getPeople());

		// randomize the people
		Collections.shuffle(people);

		// schedule the vaccinations
		double planTime = 1;
		for (PersonId personId : people) {
			actorContext.addPlan((context) -> vaccinatePerson(context, personId), planTime++);
		}

		// plan the removal of people from the simulation starting at day
		// randomize the people
		Collections.shuffle(people);

		planTime = 3;
		// schedule some person removals
		for (PersonId personId : people) {
			actorContext.addPlan((c) -> personDataManager.removePerson(personId), planTime++);
		}
	}

	private void vaccinatePerson(ActorContext actorContext, PersonId personId) {
		// The person may have already been removed from the simulation, so we
		// check that before trying to vaccinate them.
		PersonDataManager personDataManager = actorContext.getDataManager(PersonDataManager.class);
		if (personDataManager.personExists(personId)) {
			VaccinationDataManager vaccinationDataManager = actorContext.getDataManager(VaccinationDataManager.class);
			vaccinationDataManager.vaccinatePerson(personId);
			System.out.println("Person " + personId + " was vaccinated at time = " + actorContext.getTime());
		} else {
			System.out.println("Failed to vaccinate Person " + personId + " at time = " + actorContext.getTime());
		}
	}
}
