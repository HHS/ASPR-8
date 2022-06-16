package plugins.personproperties.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.personproperties.support.PersonPropertyId;
import plugins.util.properties.PropertyError;
import util.errors.ContractException;
/**
 * An event released by the global data manager whenever a global property
 * definition is added to the simulation.
 * 
 * @author Shawn Hatch
 *
 */

@Immutable
public class PersonPropertyDefinitionEvent implements Event{

	private final PersonPropertyId personPropertyId;
	

	/**
	 * Creates the event.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID if
	 *             the property id is null</li>
	 *             
	 */
	public PersonPropertyDefinitionEvent(PersonPropertyId personPropertyId) {

		if (personPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}
		this.personPropertyId = personPropertyId;
	}

	/**
	 * Returns the property id of the added property definition 
	 */
	public PersonPropertyId getPersonPropertyId() {
		return personPropertyId;
	}

}
