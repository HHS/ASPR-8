package lesson.plugins.family.events;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import lesson.plugins.family.support.FamilyId;
import lesson.plugins.person.support.PersonId;

public final class FamilyMemberShipAdditionEvent implements Event {

	private final FamilyId familyId;
	private final PersonId personId;

	public FamilyMemberShipAdditionEvent(FamilyId familyId, PersonId personId) {
		super();
		this.familyId = familyId;
		this.personId = personId;
	}

	public FamilyId getFamilyId() {
		return familyId;
	}

	public PersonId getPersonId() {
		return personId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FamilyMemberShipAdditionEvent [familyId=");
		builder.append(familyId);
		builder.append(", personId=");
		builder.append(personId);
		builder.append("]");
		return builder.toString();
	}

}
