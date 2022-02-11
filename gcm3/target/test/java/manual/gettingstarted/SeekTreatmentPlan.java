package manual.gettingstarted;

import plugins.gcm.agents.Plan;
import plugins.people.support.PersonId;

public class SeekTreatmentPlan implements Plan {
	private final PersonId personId;

	public SeekTreatmentPlan(final PersonId personId) {
		this.personId = personId;
	}

	public PersonId getPersonId() {
		return personId;
	}
}