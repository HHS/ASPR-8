package lesson.plugins.model.actors.contactmanager;

import nucleus.ActorContext;
import nucleus.ActorId;
import nucleus.PlanData;
import plugins.people.support.PersonId;

public final class ContactPlanData implements PlanData{

	private final PersonId personId;
	private final ContactAction contactAction;
	private final double time;

	

	
//	public Consumer<ActorContext> getConsumer(ActorContext actorContext){
//		
//		ContactManager contactManager = actorContext.getActor(actorId);
//		
//		switch (contactAction) {
//		
//		case END_INFECTIOUSNESS:
//			return (c)->contactManager.endInfectiousness(personId);			
//		case INFECT_CONTACT:
//			return (c)->contactManager.infectContact(personId);		
//			
//		case INFECT_PERSON:
//			return (c)->{
//				PersonPropertiesDataManager personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
//				
//			};		
//			
//		default:
//			throw new RuntimeException("");		
//		}
//		
//		
//	}

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
