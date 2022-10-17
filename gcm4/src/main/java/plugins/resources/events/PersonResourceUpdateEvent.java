package plugins.resources.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.people.support.PersonId;
import plugins.resources.support.ResourceId;

/**
 * An observation event indicating that a person's resource level has changed.
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public class PersonResourceUpdateEvent implements Event {
	private final PersonId personId;
	private final ResourceId resourceId;
	private final long previousResourceLevel;
	private final long currentResourceLevel;

	/**
	 * Constructs the event
	 */
	public PersonResourceUpdateEvent(final PersonId personId, final ResourceId resourceId, final long previousResourceLevel, final long currentResourceLevel) {
		this.personId = personId;
		this.resourceId = resourceId;
		this.previousResourceLevel = previousResourceLevel;
		this.currentResourceLevel = currentResourceLevel;
	}

	/**
	 * Returns the resource id used to create this event
	 */
	public ResourceId getResourceId() {
		return resourceId;
	}

	/**
	 * Returns the current resource level used to create this event
	 */
	public long getCurrentResourceLevel() {
		return currentResourceLevel;
	}

	/**
	 * Returns the person id used to create this event
	 */
	public PersonId getPersonId() {
		return personId;
	}

	/**
	 * Returns the previous resource level used to create this event
	 */
	public long getPreviousResourceLevel() {
		return previousResourceLevel;
	}


}
