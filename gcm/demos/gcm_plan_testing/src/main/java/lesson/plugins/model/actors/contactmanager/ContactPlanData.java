package lesson.plugins.model.actors.contactmanager;

import nucleus.PlanData;
import plugins.people.support.PersonId;

public final class ContactPlanData implements PlanData{

	private final PersonId personId;
	private final ContactAction contactAction;
	private final double time;

	public ContactPlanData(PersonId personId, ContactAction contactAction, double time) {
		super();
		this.personId = personId;
		this.contactAction = contactAction;
		this.time = time;
	}

	public PersonId getPersonId() {
		return personId;
	}

	public ContactAction getContactAction() {
		return contactAction;
	}

	public double getTime() {
		return time;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ContactPlanData [personId=");
		builder.append(personId);
		builder.append(", contactAction=");
		builder.append(contactAction);
		builder.append(", time=");
		builder.append(time);
		builder.append("]");
		return builder.toString();
	}

}
