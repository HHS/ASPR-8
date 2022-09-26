package lessons.lesson_12.plugins.vaccine;

import lessons.lesson_12.plugins.person.PersonId;
import net.jcip.annotations.Immutable;
import nucleus.Event;

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
