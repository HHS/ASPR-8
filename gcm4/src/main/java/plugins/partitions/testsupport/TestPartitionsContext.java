package plugins.partitions.testsupport;

import nucleus.ActorContext;
import nucleus.DataManager;
import nucleus.NucleusError;
import plugins.partitions.support.PartitionsContext;
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
	 *             <li>{@linkplain NucleusError#NULL_DATA_MANAGER_CLASS} if data
	 *             manager class is null</li>
	 *             <li>{@linkplain NucleusError#AMBIGUOUS_DATA_MANAGER_CLASS} if
	 *             more than one data manager matches the given class</li>
	 * 
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