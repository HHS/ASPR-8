package plugins.globalproperties.dataViews;

import java.util.Set;

import nucleus.DataView;
import plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import util.errors.ContractException;

/**
 * Data view of the GlogbalPropertiesDataManager
 *
 */

public final class GlobalPropertiesDataView implements DataView {

	private final GlobalPropertiesDataManager globalPropertiesDataManager;

	/**
	 * Constructs this view from the corresponding data manager 
	 * 
	 */
	public GlobalPropertiesDataView(GlobalPropertiesDataManager globalPropertiesDataManager) {
		this.globalPropertiesDataManager = globalPropertiesDataManager;

	}

	/**
	 * Returns the property definition for the given {@link GlobalPropertyId}
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the global
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             global property id is unknown</li>
	 * 
	 */

	public PropertyDefinition getGlobalPropertyDefinition(final GlobalPropertyId globalPropertyId) {
		return globalPropertiesDataManager.getGlobalPropertyDefinition(globalPropertyId);
	}

	/**
	 * Returns the set of global property ids
	 */

	public <T extends GlobalPropertyId> Set<T> getGlobalPropertyIds() {
		return globalPropertiesDataManager.getGlobalPropertyIds();
	}

	/**
	 * Returns the value of the global property.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the global
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             global property id is unknown</li>
	 * 
	 */
	public <T> T getGlobalPropertyValue(GlobalPropertyId globalPropertyId) {
		return globalPropertiesDataManager.getGlobalPropertyValue(globalPropertyId);
	}

	/**
	 * Returns the time when the of the global property was last assigned.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the global
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             global property id is unknown</li>
	 */

	public double getGlobalPropertyTime(GlobalPropertyId globalPropertyId) {
		return globalPropertiesDataManager.getGlobalPropertyTime(globalPropertyId);
	}

	/**
	 * Returns true if and only if the global property id exists. Returns false
	 * for null input.
	 */
	public boolean globalPropertyIdExists(final GlobalPropertyId globalPropertyId) {
		return globalPropertiesDataManager.globalPropertyIdExists(globalPropertyId);
	}

}
