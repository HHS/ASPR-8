package plugins.regions.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.regions.support.RegionPropertyId;
import plugins.util.properties.PropertyError;
import util.errors.ContractException;

/**
 * Event indicating the addition of a region property
 *
 * @author Shawn Hatch
 */
@Immutable
public record RegionPropertyDefinitionEvent(
		RegionPropertyId regionPropertyId) implements Event {

	/**
	 * Constructs the event
	 *
	 * @throws ContractException <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
	 *                           region property id is null</li>
	 */
	public RegionPropertyDefinitionEvent {
		if (regionPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}

	}

}
