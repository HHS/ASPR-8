package gov.hhs.aspr.ms.gcm.plugins.globalproperties.events;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.support.GlobalPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyError;
import util.errors.ContractException;

/**
 * An event released by the global data manager whenever a global property
 * definition is added to the simulation.
 */
public record GlobalPropertyDefinitionEvent(GlobalPropertyId globalPropertyId, Object initialPropertyValue)
		implements Event {

	/**
	 * Creates the event.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the property id is null</li>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_VALUE}
	 *                           if the initial property value is null</li>
	 *                           </ul>
	 */
	public GlobalPropertyDefinitionEvent {

		if (globalPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}

		if (initialPropertyValue == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
		}
	}
}
