package manual.gettingstarted;

import plugins.compartments.support.CompartmentId;
import plugins.gcm.agents.AbstractComponent;
import plugins.gcm.agents.Environment;
import plugins.people.support.PersonId;
import plugins.regions.support.RegionId;

public class TreatmentCompartment extends AbstractComponent {

	@Override
	public void init(Environment environment) {
		// tell the simulation to alert us when a person arrives in this
		// compartment
		environment.observeCompartmentPersonArrival(true, Compartment.TREATMENT);
	}

	@Override
	protected void observePersonCompartmentChange(final Environment environment, final CompartmentId previousCompartmentId, final CompartmentId currentCompartmentId, final PersonId personId) {

		if (currentCompartmentId != Compartment.TREATMENT) {
			return;
		}

		/*
		 * Every person who shows up for treatment should be sick, but lets
		 * check to make sure nothing went wrong.
		 */
		Boolean personIsSick = environment.getPersonPropertyValue(personId, PersonProperty.SICK);

		if (!personIsSick) {
			throw new RuntimeException("Unexpected non-sick person " + personId);
		}

		/*
		 * Determine how long the person has been sick
		 */
		double timeWhenSick = environment.getPersonPropertyTime(personId, PersonProperty.SICK);
		double currentTime = environment.getTime();
		double sicknessDuration = currentTime - timeWhenSick;

		/*
		 * Determine the dosage needed -- one dose per day sick
		 */
		long requiredDosage = (long) sicknessDuration + 1;

		/*
		 * Determine how many doses of MEDICATION are still on hand in the
		 * person's region
		 */
		RegionId regionId = environment.getPersonRegion(personId);
		long availableDosage = environment.getRegionResourceLevel(regionId, Resource.MEDICATION);

		/*
		 * Determine the dosage that will be given to the person -- don't exceed
		 * the amount available
		 */
		long distributedDosage = Math.min(requiredDosage, availableDosage);

		if (distributedDosage >= requiredDosage) {
			/*
			 * Give the person the treatment -- this will automatically move the
			 * amount from the region's supply to the person. Then send the
			 * person to the RECOVERED compartment
			 */
			environment.transferResourceToPerson(Resource.MEDICATION, personId, distributedDosage);
			environment.setPersonPropertyValue(personId, PersonProperty.SICK, false);
			environment.setPersonCompartment(personId, Compartment.RECOVERED);
		} else {
			/*
			 * We have run out of MEDICATION and the person is sent to the
			 * TERMINAL compartment
			 */
			environment.setPersonCompartment(personId, Compartment.TERMINAL);
		}

	}

}
