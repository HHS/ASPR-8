package gov.hhs.aspr.ms.gcm.nucleus;

import util.errors.ContractException;

/**
 * Base class for all data managers.
 * 
 * 
 *
 */
public class DataManager {
	/**
	 * Package access used by the simulation to help ensure that init() is
	 * invoked exactly once.
	 */
	boolean isInitialized() {
		return initialized;
	}

	private boolean initialized;

	/**
	 * Initializes the data manager. This method should only be invoked by the
	 * simulation. All data manager descendant classes that override this method
	 * must invoke the super.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain NucleusError#DATA_MANAGER_DUPLICATE_INITIALIZATION}
	 *             if init() is invoked more than once</li>
	 * 
	 */
	public void init(DataManagerContext dataManagerContext) {
		if (initialized) {
			throw new ContractException(NucleusError.DATA_MANAGER_DUPLICATE_INITIALIZATION);
		}
		initialized = true;
	}

	/**
	 * Returns the string representation of a data manager's internal DATA state
	 * that can be accessed via public methods. Note that this is not to include
	 * external references and is used primarily to test run continuity.
	 */
	@Override
	public String toString() {
		return "DataManager[]";
	}

}
