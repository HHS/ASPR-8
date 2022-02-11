package manual.demo.plans;

import manual.demo.identifiers.Compartment;
import plugins.gcm.agents.Plan;
import plugins.people.support.PersonId;

public class MovePlan implements Plan {
	private final PersonId personId;
	private final Compartment compartment;

	public MovePlan(final PersonId personId, final Compartment compartment) {
		this.personId = personId;
		this.compartment = compartment;
	}

	public PersonId getPersonId() {
		return personId;
	}

	public Compartment getCompartment() {
		return compartment;
	}

}