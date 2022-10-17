package plugins.groups.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.groups.support.GroupError;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupTypeId;
import plugins.util.properties.PropertyError;
import util.errors.ContractException;
/**
 * An event released by the groups data manager whenever a group property
 * definition is added to the simulation.
 * 
 * @author Shawn Hatch
 *
 */

@Immutable
public class GroupPropertyDefinitionEvent implements Event{

	private final GroupTypeId groupTypeId;
	private final GroupPropertyId groupPropertyId;

	/**
	 * Creates the event.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if
	 *             the group type id is null</li>
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID}
	 *             if the group property id is null</li>
	 */
	public GroupPropertyDefinitionEvent(GroupTypeId groupTypeId,GroupPropertyId groupPropertyId) {

		if (groupTypeId == null) {
			throw new ContractException(GroupError.NULL_GROUP_TYPE_ID);
		}

		if (groupPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}

		this.groupTypeId = groupTypeId;
		this.groupPropertyId = groupPropertyId;
	}

	/**
	 * Returns the property id of the added property definition 
	 */
	public GroupPropertyId getGroupPropertyId() {
		return groupPropertyId;
	}

	/**
	 * Returns the group type of the added property definition
	 */
	public GroupTypeId getGroupTypeId() {
		return groupTypeId;
	}

}
