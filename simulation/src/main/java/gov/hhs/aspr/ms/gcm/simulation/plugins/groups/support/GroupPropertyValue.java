package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support;

import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.util.errors.ContractException;

public record GroupPropertyValue(GroupPropertyId groupPropertyId, Object value) {
	/**
	 * Creates the record.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the property id is null</li>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_VALUE}
	 *                           if the property value is null</li>
	 *                           </ul>
	 */
	public GroupPropertyValue {

		if (groupPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}

		if (value == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
		}
	}
}
