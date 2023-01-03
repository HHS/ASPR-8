package plugins.personproperties.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.personproperties.support.PersonPropertyId;
import plugins.util.properties.PropertyError;
import util.errors.ContractException;

/**
 * An event released by the people data manager whenever a person property
 * definition is added to the simulation.
 *
 */

@Immutable
public record PersonPropertyDefinitionEvent(
		PersonPropertyId personPropertyId) implements Event {

	/**
	 * Creates the event.
	 *
	 * @throws ContractException <li>{@linkplain PropertyError#NULL_PROPERTY_ID if
	 *                           the property id is null</li>
	 */
	public PersonPropertyDefinitionEvent {

		if (personPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}
	}

}
