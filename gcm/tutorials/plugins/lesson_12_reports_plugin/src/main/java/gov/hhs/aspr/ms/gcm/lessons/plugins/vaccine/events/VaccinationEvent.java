package gov.hhs.aspr.ms.gcm.lessons.plugins.vaccine.events;

import gov.hhs.aspr.ms.gcm.lessons.plugins.person.support.PersonId;
import gov.hhs.aspr.ms.gcm.nucleus.Event;
import net.jcip.annotations.Immutable;

@Immutable
public final class VaccinationEvent implements Event {
	private final PersonId personId;

	public VaccinationEvent(PersonId personId) {
		super();
		this.personId = personId;
	}

	public PersonId getPersonId() {
		return personId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("VaccinationEvent [personId=");
		builder.append(personId);
		builder.append("]");
		return builder.toString();
	}

}
