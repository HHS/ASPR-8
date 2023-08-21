package gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport;

import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.nucleus.DataManager;
import gov.hhs.aspr.ms.gcm.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PartitionsContext;
import util.errors.ContractException;

/**
 * Test support implementor of PartitionsContext that uses an ActorContext
 * instead of the usual DataManagerContext to ease testing.
 */
public final class TestPartitionsContext implements PartitionsContext {
	private final ActorContext actorContext;

	public TestPartitionsContext(ActorContext actorContext) {
		this.actorContext = actorContext;
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
		return actorContext.getDataManager(dataManagerClass);
	}

	/**
	 * Returns the current time in the simulation
	 */
	public double getTime() {
		return actorContext.getTime();
	}
}