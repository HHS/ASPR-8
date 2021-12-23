package manual.demo.components;

import org.apache.commons.math3.random.RandomGenerator;

import manual.demo.identifiers.Compartment;
import manual.demo.identifiers.CompartmentProperty;
import manual.demo.identifiers.PersonProperty;
import manual.demo.identifiers.Resource;
import manual.demo.plans.MovePlan;
import plugins.compartments.support.CompartmentId;
import plugins.gcm.agents.AbstractComponent;
import plugins.gcm.agents.Environment;
import plugins.gcm.agents.Plan;
import plugins.people.support.PersonId;

public class InfectedCompartment extends AbstractComponent {

	@Override
	public void init(Environment environment) {
		double weightThreshold = environment.getRandomGenerator().nextDouble() * 20 + 60;
		environment.setCompartmentPropertyValue(Compartment.INFECTED, CompartmentProperty.WEIGHT_THRESHOLD, weightThreshold);
		environment.observeCompartmentPersonArrival(true, Compartment.INFECTED);
	}

	@Override
	protected void observePersonCompartmentChange(final Environment environment, final CompartmentId previousCompartmentId, final CompartmentId currentCompartmentId, final PersonId personId) {
		if (currentCompartmentId != Compartment.INFECTED) {
			return;
		}

		Float weight = environment.getPersonPropertyValue(personId, PersonProperty.WEIGHT);
		RandomGenerator randomGenerator = environment.getRandomGenerator();

		double weightThreshold = environment.getCompartmentPropertyValue(Compartment.INFECTED, CompartmentProperty.WEIGHT_THRESHOLD);
		if (weight > weightThreshold) {
			double moveTime = randomGenerator.nextDouble() * 5 + environment.getTime();
			environment.addPlan(new MovePlan(personId, Compartment.DEAD), moveTime);
		}
	}

	@Override
	public void executePlan(final Environment environment, final Plan plan) {
		MovePlan movePlan = (MovePlan) plan;
		PersonId personId = movePlan.getPersonId();
		long personResourceLevel = environment.getPersonResourceLevel(personId, Resource.RESOURCE_1);
		if (personResourceLevel > 0) {
			environment.removeResourceFromPerson(Resource.RESOURCE_1, personId, personResourceLevel);
		}
		environment.setPersonCompartment(movePlan.getPersonId(), movePlan.getCompartment());
	}

}
