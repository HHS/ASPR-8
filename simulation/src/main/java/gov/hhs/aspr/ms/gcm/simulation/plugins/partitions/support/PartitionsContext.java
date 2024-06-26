package gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.DataManager;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.NucleusError;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * A limited context that grants access to the simulation via the partition data
 * manager's own data manager context.
 */
public interface PartitionsContext {

	/**
	 * Returns the data manager from the given class reference
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain NucleusError#NULL_DATA_MANAGER_CLASS}
	 *                           if data manager class is null</li>
	 *                           <li>{@linkplain NucleusError#AMBIGUOUS_DATA_MANAGER_CLASS}
	 *                           if more than one data manager matches the given
	 *                           class</li>
	 *                           </ul>
	 */
	public <T extends DataManager> T getDataManager(Class<T> dataManagerClass);

	/**
	 * Returns the current time in the simulation
	 */
	public double getTime();
}
