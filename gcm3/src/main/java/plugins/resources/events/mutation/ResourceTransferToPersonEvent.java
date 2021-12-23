package plugins.resources.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.people.support.PersonId;
import plugins.resources.support.ResourceId;

/**
 * Event to transfer an amount of resource from a person to their region.
 *
 */
@Immutable
public final class ResourceTransferToPersonEvent implements Event {

	private final ResourceId resourceId;

	private final PersonId personId;

	private final long amount;

	/**
	 * Constructs the event 
	 */
	public ResourceTransferToPersonEvent(ResourceId resourceId, PersonId personId, long amount) {		
		this.resourceId = resourceId;
		this.personId = personId;
		this.amount = amount;
	}
	
	/**
	 * Returns the resource id used to create this event
	 */
	public ResourceId getResourceId() {
		return resourceId;
	}

	/**
	 * Returns the person id used to create this event
	 */
	public PersonId getPersonId() {
		return personId;
	}
	/**
	 * Returns the resource amount used to create this event
	 */
	public long getAmount() {
		return amount;
	}

}
