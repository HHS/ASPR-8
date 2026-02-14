package gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.DataManager;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.DataManagerContext;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.NucleusError;
import gov.hhs.aspr.ms.util.errors.ContractException;

public final class PartitionsContextImpl implements PartitionsContext {
	
	private final DataManagerContext dataManagerContext;

	public PartitionsContextImpl(DataManagerContext dataManagerContext) {
		this.dataManagerContext = dataManagerContext;
	}

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
	public <T extends DataManager> T getDataManager(Class<T> dataManagerClass) {
		return dataManagerContext.getDataManager(dataManagerClass);
	}

	/**
	 * Returns the current time in the simulation
	 */
	public double getTime() {
		return dataManagerContext.getTime();
	}
}
