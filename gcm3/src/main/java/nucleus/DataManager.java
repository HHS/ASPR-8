package nucleus;

import nucleus.util.ContractException;

/**
 * Base class for all data managers.
 * 
 * 
 * @author Shawn Hatch
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

}
