package plugins.personproperties.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.globalproperties.support.GlobalPropertiesError;
import plugins.personproperties.support.PersonPropertyError;
import plugins.personproperties.support.PersonPropertyId;
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
	 *             <li>{@linkplain GlobalPropertiesError.NULL_PERSON_PROPERTY_ID} if
	 *             the property id is null</li>
	 *             
	 */
	public PersonPropertyDefinitionEvent(PersonPropertyId personPropertyId) {

		if (personPropertyId == null) {
			throw new ContractException(PersonPropertyError.NULL_PERSON_PROPERTY_ID);
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
