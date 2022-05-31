package plugins.globalproperties.events;

import nucleus.Event;
import plugins.globalproperties.support.GlobalPropertiesError;
import plugins.globalproperties.support.GlobalPropertyId;
import util.errors.ContractException;
/**
 * An event released by the global data manager whenever a global property
 * definition is added to the simulation.
 * 
 * @author Shawn Hatch
 *
 */


public record GlobalPropertyDefinitionEvent(GlobalPropertyId globalPropertyId,Object initialPropertyValue) implements Event{

	/**
	 * Creates the event.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GlobalPropertiesError.NULL_GLOBAL_PROPERTY_ID} if
	 *             the property id is null</li>
	 *             <li>{@linkplain GlobalPropertiesError#NULL_GLOBAL_PROPERTY_VALUE}
	 *             if the initial property value is null</li>
	 */
	public GlobalPropertyDefinitionEvent {

		if (globalPropertyId == null) {
			throw new ContractException(GlobalPropertiesError.NULL_GLOBAL_PROPERTY_ID);
		}

		if (initialPropertyValue == null) {
			throw new ContractException(GlobalPropertiesError.NULL_GLOBAL_PROPERTY_VALUE);
		}
	}
}
