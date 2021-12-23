package plugins.globals.datacontainers;

import java.util.Set;

import nucleus.Context;
import nucleus.DataView;
import nucleus.NucleusError;
import plugins.globals.support.GlobalComponentId;
import plugins.globals.support.GlobalError;
import plugins.globals.support.GlobalPropertyId;
import plugins.properties.support.PropertyDefinition;
import util.ContractException;

/**
 * Published data view that global component and global property ids.
 * 
 * @author Shawn Hatch
 *
 */
public final class GlobalDataView implements DataView {
	private final GlobalDataManager globalDataManager;
	private final Context context;

	/**
	 * Creates the Global Data View from the given {@link Context} and
	 * {@link GlobalDataManager}.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain NucleusError#NULL_CONTEXT} if the context is
	 *             null</li>
	 *             <li>{@linkplain GlobalError#NULL_GLOBAL_DATA_MANGER} if
	 *             global data manager is null</li>
	 * 
	 */
	public GlobalDataView(Context context, GlobalDataManager globalDataManager) {
		if (context == null) {
			throw new ContractException(NucleusError.NULL_CONTEXT);
		}
		this.context = context;

		if (globalDataManager == null) {
			throw new ContractException(GlobalError.NULL_GLOBAL_DATA_MANGER);
		}
		this.globalDataManager = globalDataManager;
	}

	/**
	 * Returns the property definition for the given {@link GlobalPropertyId}
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GlobalError#NULL_GLOBAL_PROPERTY_ID} if the
	 *             global property id is null</li>
	 *             <li>{@linkplain GlobalError#UNKNOWN_GLOBAL_PROPERTY_ID} if
	 *             the global property id is unknown</li>
	 * 
	 */
	public PropertyDefinition getGlobalPropertyDefinition(final GlobalPropertyId globalPropertyId) {
		validateGlobalPropertyId(globalPropertyId);
		return globalDataManager.getGlobalPropertyDefinition(globalPropertyId);
	}

	/**
	 * Returns the {@link GlobalPropertyId} values
	 */
	public <T extends GlobalPropertyId> Set<T> getGlobalPropertyIds() {
		return globalDataManager.getGlobalPropertyIds();
	}

	/**
	 * Returns the {@link GlobalPropertyId} values
	 */
	public <T extends GlobalComponentId> Set<T> getGlobalComponentIds() {
		return globalDataManager.getGlobalComponentIds();
	}

	/**
	 * Returns the value of the global property.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GlobalError#NULL_GLOBAL_PROPERTY_ID} if the
	 *             global property id is null</li>
	 *             <li>{@linkplain GlobalError#UNKNOWN_GLOBAL_PROPERTY_ID} if
	 *             the global property id is unknown</li>
	 * 
	 */
	public <T> T getGlobalPropertyValue(GlobalPropertyId globalPropertyId) {
		validateGlobalPropertyId(globalPropertyId);
		return globalDataManager.getGlobalPropertyValue(globalPropertyId);
	}

	/**
	 * Returns the time when the of the global property was last assigned.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GlobalError#NULL_GLOBAL_PROPERTY_ID} if the
	 *             global property id is null</li>
	 *             <li>{@linkplain GlobalError#UNKNOWN_GLOBAL_PROPERTY_ID} if
	 *             the global property id is unknown</li>
	 */

	public double getGlobalPropertyTime(GlobalPropertyId globalPropertyId) {
		validateGlobalPropertyId(globalPropertyId);
		return globalDataManager.getGlobalPropertyTime(globalPropertyId);
	}

	private void validateGlobalPropertyId(final GlobalPropertyId globalPropertyId) {
		if (globalPropertyId == null) {
			context.throwContractException(GlobalError.NULL_GLOBAL_PROPERTY_ID);
		}

		if (!globalDataManager.globalPropertyIdExists(globalPropertyId)) {
			context.throwContractException(GlobalError.UNKNOWN_GLOBAL_PROPERTY_ID, globalPropertyId);
		}
	}
}
