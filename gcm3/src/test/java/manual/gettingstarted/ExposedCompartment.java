package manual.gettingstarted;

import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import plugins.gcm.agents.AbstractComponent;
import plugins.gcm.agents.Environment;
import plugins.gcm.agents.Plan;
import plugins.people.support.PersonId;

public class ExposedCompartment extends AbstractComponent {

	@Override
	public void init(Environment environment) {
		// Get the random generator that is managed by the simulation
		RandomGenerator randomGenerator = environment.getRandomGenerator();

		/*
		 * Retrieve a list of people that are in this compartment. We assume
		 * that this is the entire population of the model.
		 */
		List<PersonId> people = environment.getPeopleInCompartment(Compartment.EXPOSED);

		/*
		 * Checking our assumption
		 */
		if (environment.getPopulationCount() != people.size()) {
			throw new RuntimeException("The entire population should start in the EXPOSED compartment");
		}

		// Determine immunity status for every person. For each person, there is
		// a 20% chance to be immune.
		for (PersonId personId : people) {
			boolean immune = randomGenerator.nextDouble() < 0.2;
			if (immune) {
				environment.setPersonPropertyValue(personId, PersonProperty.IMMUNE, immune);
			}
		}

		// Retrieve a list of people who are not immune
		List<PersonId> nonImmunePeople = environment.getPeopleWithPropertyValue(PersonProperty.IMMUNE, false);

		/*
		 * 80% of those who are not immune will get sick. Again the default was
		 * false so we only set the true values.
		 * 
		 */
		for (PersonId personId : nonImmunePeople) {
			boolean sick = randomGenerator.nextDouble() < 0.8;
			if (sick) {
				environment.setPersonPropertyValue(personId, PersonProperty.SICK, sick);
			}
		}

		// Retrieve a list of the people who are sick
		List<PersonId> sickPeople = environment.getPeopleWithPropertyValue(PersonProperty.SICK, true);

		/*
		 * Each person who is sick will eventually seek treatment (1-10) days
		 * from now. Make a plan for each person to seek treatment in the
		 * future.
		 */
		for (PersonId personId : sickPeople) {
			// Make a future plan to move the person to TREATMENT compartment
			SeekTreatmentPlan seekTreatmentPlan = new SeekTreatmentPlan(personId);
			// Determine when the person should move
			double treatmentDelayDays = 9 * randomGenerator.nextDouble() + 1;
			double treatmentEntryTime = environment.getTime() + treatmentDelayDays;
			environment.addPlan(seekTreatmentPlan, treatmentEntryTime);
		}
	}

	@Override
	public void executePlan(final Environment environment, final Plan plan) {
		// The plan made earlier has come back from the environment at the
		// scheduled time. We now act on that plan by
		// moving the person to the TREATMENT compartment
		SeekTreatmentPlan seekTreatmentPlan = (SeekTreatmentPlan) plan;
		environment.setPersonCompartment(seekTreatmentPlan.getPersonId(), Compartment.TREATMENT);
	}

}
