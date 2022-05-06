package plugins.globalproperties.events;

import net.jcip.annotations.Immutable;
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

@Immutable
public class GlobalPropertyDefinitionEvent implements Event{

	private final GlobalPropertyId globalPropertyId;
	private final Object initialPropertyValue;

	/**
	 * Creates the event.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GlobalPropertiesError.NULL_GLOBAL_PROPERTY_ID} if
	 *             the property id is null</li>
	 *             <li>{@linkplain GlobalPropertiesError#NULL_GLOBAL_PROPERTY_VALUE}
	 *             if the initial property value is null</li>
	 */
	public GlobalPropertyDefinitionEvent(GlobalPropertyId globalPropertyId, Object initialPropertyValue) {

		if (globalPropertyId == null) {
			throw new ContractException(GlobalPropertiesError.NULL_GLOBAL_PROPERTY_ID);
		}

		if (initialPropertyValue == null) {
			throw new ContractException(GlobalPropertiesError.NULL_GLOBAL_PROPERTY_VALUE);
		}

		this.globalPropertyId = globalPropertyId;
		this.initialPropertyValue = initialPropertyValue;
	}

	/**
	 * Returns the property id of the added property definition 
	 */
	public GlobalPropertyId getGlobalPropertyId() {
		return globalPropertyId;
	}

	/**
	 * Returns the initial property value for the added property definition
	 */
	public Object getInitialPropertyValue() {
		return initialPropertyValue;
	}

}
