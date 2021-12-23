package plugins.resources.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.people.support.PersonId;
import plugins.resources.support.ResourceId;

/**
 * Event for transferring an amount of resource from a person to their region.
 *
 */
@Immutable
public final class ResourceTransferFromPersonEvent implements Event {

	private final ResourceId resourceId;

	private final PersonId personId;

	private final long amount;


	/**
	 * Constructs the event 
	 */
	public ResourceTransferFromPersonEvent(ResourceId resourceId, PersonId personId, long amount) {
		super();
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
