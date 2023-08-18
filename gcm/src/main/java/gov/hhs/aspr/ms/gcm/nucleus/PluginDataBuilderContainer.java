package gov.hhs.aspr.ms.gcm.nucleus;

import util.errors.ContractException;

/**
 * A container for PluginDataBuilder instances.
 */
public interface PluginDataBuilderContainer {

	/**
	 * Returns the stored item matching the given class reference. Class reference
	 * matching is performed against all plugin data builders in this container. A
	 * ContractException is thrown if there is not exactly one matching instance.
	 * 
	 * @throws util.errors.ContractException
	 *                           <ul>
	 *                           <li>{@linkplain NucleusError#AMBIGUOUS_PLUGIN_DATA_BUILDER_CLASS}
	 *                           if more than one plugin data builder matches the
	 *                           given class reference</li>
	 *                           <li>{@linkplain NucleusError#UNKNOWN_PLUGIN_DATA_BUILDER_CLASS}
	 *                           if no plugin data builder matches the given class
	 *                           reference</li>
	 *                           </ul>
	 */
	public <T extends PluginDataBuilder> T getPluginDataBuilder(Class<T> classRef);

}
