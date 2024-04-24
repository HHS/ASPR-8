package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support.GroupError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support.GroupPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support.GroupTypeId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.Immutable;

/**
 * An event released by the groups data manager whenever a group property
 * definition is added to the simulation.
 */
@Immutable
public record GroupPropertyDefinitionEvent(GroupTypeId groupTypeId, GroupPropertyId groupPropertyId) implements Event {

	/**
	 * Creates the event.
	 *
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if
	 *                           the group type id is null</li>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the group property id is null</li>
	 *                           </ul>
	 */
	public GroupPropertyDefinitionEvent {

		if (groupTypeId == null) {
			throw new ContractException(GroupError.NULL_GROUP_TYPE_ID);
		}

		if (groupPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}

	}

}
