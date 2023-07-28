package lesson.plugins.model.actors.vaccinator;

import gov.hhs.aspr.ms.gcm.nucleus.PlanData;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;

public class VaccinationPlanData implements PlanData {

	private final PersonId personId;
	private final double time;

	public VaccinationPlanData(PersonId personId, double time) {
		super();
		this.personId = personId;
		this.time = time;
	}

	public PersonId getPersonId() {
		return personId;
	}

	public double getTime() {
		return time;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("VaccinationPlanData [personId=");
		builder.append(personId);
		builder.append(", time=");
		builder.append(time);
		builder.append("]");
		return builder.toString();
	}

}
