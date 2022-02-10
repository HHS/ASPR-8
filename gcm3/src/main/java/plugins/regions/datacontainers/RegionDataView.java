package plugins.regions.datacontainers;

import java.util.Set;

import nucleus.SimulationContext;
import nucleus.DataView;
import plugins.properties.support.PropertyDefinition;
import plugins.regions.initialdata.RegionInitialData;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import util.ContractException;

/**
 * Published data view that provides region property information.
 * 
 * @author Shawn Hatch
 *
 */
public final class RegionDataView implements DataView {
	private final RegionDataManager regionDataManager;
	private final SimulationContext simulationContext;

	/**
	 * Creates the Region Data View from the given {@link SimulationContext} and
	 * {@link RegionDataManager}. Not null tolerant.
	 * 
	 */
	public RegionDataView(SimulationContext simulationContext, RegionDataManager regionDataManager) {
		this.simulationContext = simulationContext;
		this.regionDataManager = regionDataManager;
	}

	/**
	 * Returns the property definition for the given {@link RegionPropertyId}
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain RegionError#NULL_REGION_PROPERTY_ID}
	 *             if the region property id is null</li>
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_PROPERTY_ID}
	 *             if the region property id is unknown
	 * 
	 */
	public PropertyDefinition getRegionPropertyDefinition(final RegionPropertyId regionPropertyId) {
		validateRegionPropertyId(regionPropertyId);
		return regionDataManager.getRegionPropertyDefinition(regionPropertyId);
	}

	/**
	 * Returns the {@link RegionPropertyId} values
	 */
	public <T extends RegionPropertyId> Set<T> getRegionPropertyIds() {
		return regionDataManager.getRegionPropertyIds();
	}

	/**
	 * Return true if and only if the given {@link RegionId} exits. Null
	 * tolerant.
	 */
	public boolean regionIdExists(RegionId regionId) {
		return regionDataManager.regionIdExists(regionId);
	}

	
	/**
	 * Returns the set of {@link RegionId} values that are defined by the
	 * {@link RegionInitialData}.
	 */
	public <T extends RegionId> Set<T> getRegionIds() {
		return regionDataManager.getRegionIds();
	}

	/**
	 * Returns the value of the region property.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the
	 *             region id is null</li>
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if
	 *             the region id is not known</li>
	 *             <li>{@linkplain RegionError#NULL_REGION_PROPERTY_ID}
	 *             if the region property id is null</li>
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_PROPERTY_ID}
	 *             if the region property id is unknown</li>
	 */
	public <T> T getRegionPropertyValue(RegionId regionId, RegionPropertyId regionPropertyId) {
		validateRegionId(regionId);
		validateRegionPropertyId(regionPropertyId);
		return regionDataManager.getRegionPropertyValue(regionId, regionPropertyId);
	}

	/**
	 * Returns the time when the of the region property was last assigned.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the
	 *             region id is null</li>
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if
	 *             the region id is not known</li>
	 *             <li>{@linkplain RegionError#NULL_REGION_PROPERTY_ID}
	 *             if the region property id is null</li>
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_PROPERTY_ID}
	 *             if the region property id is unknown</li>
	 */

	public double getRegionPropertyTime(RegionId regionId, RegionPropertyId regionPropertyId) {
		validateRegionId(regionId);
		validateRegionPropertyId(regionPropertyId);
		return regionDataManager.getRegionPropertyTime(regionId, regionPropertyId);
	}

	private void validateRegionId(final RegionId regionId) {

		if (regionId == null) {
			simulationContext.throwContractException(RegionError.NULL_REGION_ID);
		}

		if (!regionDataManager.regionIdExists(regionId)) {
			simulationContext.throwContractException(RegionError.UNKNOWN_REGION_ID, regionId);
		}
	}

	private void validateRegionPropertyId(final RegionPropertyId regionPropertyId) {
		if (regionPropertyId == null) {
			simulationContext.throwContractException(RegionError.NULL_REGION_PROPERTY_ID);
		}
		if (!regionDataManager.regionPropertyIdExists(regionPropertyId)) {
			simulationContext.throwContractException(RegionError.UNKNOWN_REGION_PROPERTY_ID, regionPropertyId);
		}
	}

}
