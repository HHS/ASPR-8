package plugins.groups.support;

import util.errors.ContractException;

public record GroupPropertyValue(GroupPropertyId groupPropertyId,Object value) {
	/**
	 * Creates the record.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError.NULL_GROUP_PROPERTY_ID} if
	 *             the property id is null</li>
	 *             <li>{@linkplain GroupError#NULL_GROUP_PROPERTY_VALUE}
	 *             if the property value is null</li>
	 */
	public GroupPropertyValue {

		if (groupPropertyId == null) {
			throw new ContractException(GroupError.NULL_GROUP_PROPERTY_ID);
		}

		if (value == null) {
			throw new ContractException(GroupError.NULL_GROUP_PROPERTY_VALUE);
		}
	}
}
