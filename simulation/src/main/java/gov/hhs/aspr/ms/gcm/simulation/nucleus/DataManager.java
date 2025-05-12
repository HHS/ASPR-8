package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import java.util.Optional;

import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * Base class for all data managers.
 */
public class DataManager {
	/**
	 * Package access used by the simulation to help ensure that init() is invoked
	 * exactly once.
	 */
	boolean isInitialized() {
		return initialized;
	}

	private boolean initialized;

	private DataManagerId dataManagerId;

	/**
	 * Initializes the data manager. This method should only be invoked by the
	 * simulation. All data manager descendant classes that override this method
	 * must invoke the super. <br>
	 * 
	 * @param dataManagerContext
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>
	 *                           {@linkplain NucleusError#NULL_DATA_MANAGER_CONTEXT}
	 *                           if the data manager context is null</li>
	 *                           <li>
	 *                           {@linkplain NucleusError#DATA_MANAGER_DUPLICATE_INITIALIZATION}
	 *                           if init() is invoked more than once</li>
	 *                           </ul>
	 */
	public void init(DataManagerContext dataManagerContext) {
		if (dataManagerContext == null) {
			throw new ContractException(NucleusError.NULL_DATA_MANAGER_CONTEXT);
		}

		if (initialized) {
			throw new ContractException(NucleusError.DATA_MANAGER_DUPLICATE_INITIALIZATION);
		}
		initialized = true;

		dataManagerId = dataManagerContext.getDataManagerId();
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

	/**
	 * Returns the data manager's id. Result will be present after simulation
	 * initialization is complete for data managers.
	 */
	public final Optional<DataManagerId> getDataManagerId() {
		return Optional.ofNullable(dataManagerId);
	}

}
