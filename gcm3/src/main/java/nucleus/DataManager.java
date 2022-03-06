package nucleus;

import util.ContractException;

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
	 * 
	 */
	public void init(DataManagerContext dataManagerContext) {
		if (initialized) {
			throw new ContractException(NucleusError.DATA_MANAGER_DUPLICATE_INITIALIZATION);
		}
		initialized = true;
	}

}
