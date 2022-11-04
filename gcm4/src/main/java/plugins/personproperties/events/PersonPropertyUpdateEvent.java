package plugins.personproperties.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.people.support.PersonId;
import plugins.personproperties.support.PersonPropertyId;

/**
 * An observation event indicating that a person's property assignment has
 * changed.
 *
 * @author Shawn Hatch
 *
 */

@Immutable
public class PersonPropertyUpdateEvent implements Event {
	

	private final PersonId personId;
	private final PersonPropertyId personPropertyId;
	private final Object previousPropertyValue;
	private final Object currentPropertyValue;

	/**
	 * Creates this event from valid, non-null inputs
	 * 
	 */
	public PersonPropertyUpdateEvent(final PersonId personId, final PersonPropertyId personPropertyId, final Object previousPropertyValue, final Object currentPropertyValue) {
		super();
		this.personId = personId;
		this.personPropertyId = personPropertyId;
		this.previousPropertyValue = previousPropertyValue;
		this.currentPropertyValue = currentPropertyValue;
	}

	/**
	 * Returns the current property value used to construct this event
	 */
	@SuppressWarnings("unchecked")
	public <T> T getCurrentPropertyValue() {
		return (T)currentPropertyValue;
	}

	/**
	 * Returns the person property id used to construct this event
	 */
	public PersonPropertyId getPersonPropertyId() {
		return personPropertyId;
	}

	/**
	 * Returns the person id used to construct this event
	 */
	public PersonId getPersonId() {
		return personId;
	}

	/**
	 * Returns the previous property value used to construct this event
	 */
	public Object getPreviousPropertyValue() {
		return previousPropertyValue;
	}

	/**
	 * Returns this event in the form:
	 * 
	 * "PersonPropertyUpdateEvent [personId=" + personId + ", personPropertyId="
	 * + personPropertyId + ", previousPropertyValue=" + previousPropertyValue +
	 * ", currentPropertyValue=" + currentPropertyValue + "]";
	 */
	@Override
	public String toString() {
		return "PersonPropertyUpdateEvent [personId=" + personId + ", personPropertyId=" + personPropertyId + ", previousPropertyValue=" + previousPropertyValue + ", currentPropertyValue="
				+ currentPropertyValue + "]";
	}


}
