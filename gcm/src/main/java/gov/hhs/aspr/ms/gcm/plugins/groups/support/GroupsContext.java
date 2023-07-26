package gov.hhs.aspr.ms.gcm.plugins.groups.support;

import gov.hhs.aspr.ms.gcm.nucleus.DataManager;
import gov.hhs.aspr.ms.gcm.nucleus.NucleusError;
import util.errors.ContractException;

/**
 * 
 * A limited context that grants access to the simulation via the groups data
 * manager's own data manager context.
 *
 */
public interface GroupsContext {
	

	/**
	 * Returns the data manager from the given class reference
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain NucleusError#NULL_DATA_MANAGER_CLASS} if data
	 *             manager class is null</li>
	 *             <li>{@linkplain NucleusError#AMBIGUOUS_DATA_MANAGER_CLASS} if
	 *             more than one data manager matches the given class</li>
	 * 
	 */
	public <T extends DataManager> T getDataManager(Class<T> dataManagerClass);

	/**
	 * Returns the current time in the simulation
	 */
	public double getTime();
}
