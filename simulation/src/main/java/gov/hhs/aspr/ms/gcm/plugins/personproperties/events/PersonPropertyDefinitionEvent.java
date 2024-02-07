package gov.hhs.aspr.ms.gcm.plugins.personproperties.events;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.support.PersonPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.Immutable;

/**
 * An event released by the people data manager whenever a person property
 * definition is added to the simulation.
 */
@Immutable
public record PersonPropertyDefinitionEvent(PersonPropertyId personPropertyId) implements Event {

	/**
	 * Creates the event.
	 *
	 * @throws ContractException {@linkplain PropertyError#NULL_PROPERTY_ID} if the
	 *                           property id is null
	 */
	public PersonPropertyDefinitionEvent {

		if (personPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}
	}

}
