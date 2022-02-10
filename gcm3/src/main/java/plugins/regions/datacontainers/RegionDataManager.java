package plugins.regions.datacontainers;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import nucleus.SimulationContext;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.PropertyValueRecord;
import plugins.regions.RegionPlugin;
import plugins.regions.initialdata.RegionInitialData;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;

/**
 * Mutable data manager that backs the {@linkplain RegionDataView}. This data
 * manager is for internal use by the {@link RegionPlugin} and should not be
 * published.
 * 
 * All regions and region properties are established during construction and
 * cannot be changed. Region property values are mutable. Limited validation of
 * inputs are performed and mutation methods have invocation ordering
 * requirements.
 * 
 * @author Shawn Hatch
 *
 */
public final class RegionDataManager {

	private final SimulationContext simulationContext;

	/**
	 * Creates a Region Data Manager from the given resolver context.
	 * Preconditions: The context must be a valid and non-null.
	 */
	public RegionDataManager(SimulationContext simulationContext) {
		this.simulationContext = simulationContext;
	}

	private Map<RegionId, Map<RegionPropertyId, PropertyValueRecord>> regionPropertyMap = new LinkedHashMap<>();

	private Set<RegionPropertyId> regionPropertyIds = new LinkedHashSet<>();

	private Map<RegionPropertyId, PropertyDefinition> regionPropertyDefinitions = new LinkedHashMap<>();

	/**
	 * Returns the {@link PropertyDefinition} associated with the given
	 * {@link RegionPropertyId}. Returns null if the property id is not valid.
	 * 
	 */
	public PropertyDefinition getRegionPropertyDefinition(final RegionPropertyId regionPropertyId) {
		return regionPropertyDefinitions.get(regionPropertyId);
	}

	/**
	 * Returns the region property ids.
	 */
	@SuppressWarnings("unchecked")
	public <T extends RegionPropertyId> Set<T> getRegionPropertyIds() {
		Set<T> result = new LinkedHashSet<>(regionPropertyDefinitions.keySet().size());
		for (RegionPropertyId regionPropertyId : regionPropertyDefinitions.keySet()) {
			result.add((T) regionPropertyId);
		}
		return result;
	}

	/**
	 * Return true if and only if the given {@link RegionId} exits. Null
	 * tolerant.
	 */
	public boolean regionIdExists(RegionId regionId) {
		return regionPropertyMap.containsKey(regionId);
	}

	/**
	 * Returns true if and only if the given {@link RegionPropertyId} exists.
	 * Tolerates nulls.
	 */
	public boolean regionPropertyIdExists(RegionPropertyId regionPropertyId) {
		return regionPropertyDefinitions.containsKey(regionPropertyId);
	}

	/**
	 * Returns the set of {@link RegionId} values that are defined by the
	 * {@link RegionInitialData}.
	 */
	@SuppressWarnings("unchecked")
	public <T extends RegionId> Set<T> getRegionIds() {
		Set<T> result = new LinkedHashSet<>(regionPropertyMap.size());
		for (RegionId regionId : regionPropertyMap.keySet()) {
			result.add((T) regionId);
		}
		return result;

	}

	/**
	 * Sets the value of the region property. Preconditions: (1)All inputs are
	 * non-null (2)The region and region property definitions were previously
	 * added. No validation is performed.
	 * 
	 * @throws RuntimeException
	 *             <li>if the region id is null</li>
	 *             <li>if the region id was not previously added</li>
	 *             <li>if the region property id is null</li>
	 *             <li>if the region property was previously added</li>
	 */
	public void setRegionPropertyValue(RegionId regionId, RegionPropertyId regionPropertyId, Object regionPropertyValue) {
		regionPropertyMap.get(regionId).get(regionPropertyId).setPropertyValue(regionPropertyValue);
	}

	/**
	 * 
	 * Returns the value of the region property. Preconditions: (1)All inputs
	 * are non-null and valid. No validation is performed.
	 * 
	 * @throws RuntimeException
	 * 
	 *             <li>if the region id is null</li>
	 *             <li>if the region id was not previously added</li>
	 *             <li>if the region property id is null</li>
	 *             <li>if the region property was not previously added</li>
	 */
	@SuppressWarnings("unchecked")
	public <T> T getRegionPropertyValue(RegionId regionId, RegionPropertyId regionPropertyId) {
		return (T) regionPropertyMap.get(regionId).get(regionPropertyId).getValue();
	}

	/**
	 * Returns the time value for the last assignment to the given
	 * {@linkplain RegionId} and {@linkplain}RegionPropertyId}. The
	 * {@link RegionId} and {@link RegionPropertyId} must be valid.
	 * 
	 * @throws RuntimeException
	 *             <li>if the region id is null</li>
	 *             <li>if the region id is unknown</li>
	 *             <li>if the region property id is null</li>
	 *             <li>if the region property id is unknown</li>
	 * 
	 */
	public double getRegionPropertyTime(RegionId regionId, RegionPropertyId regionPropertyId) {
		return regionPropertyMap.get(regionId).get(regionPropertyId).getAssignmentTime();
	}

	/**
	 * Adds the region id. Preconditions: the region must be non-null and added
	 * exactly once.
	 * 
	 * @throws RuntimeException
	 *             <li>if the region id is null</li>
	 *             <li>if the region was previously added</li>
	 *             <li>if the region properties were previously added</li>
	 */
	public void addRegionId(RegionId regionId) {
		validateRegionIdForAddition(regionId);
		regionPropertyMap.put(regionId, new LinkedHashMap<>());
	}

	private void validateRegionIdForAddition(RegionId regionId) {
		if (regionId == null) {
			throw new RuntimeException("region id is null");
		}
		if (regionPropertyMap.containsKey(regionId)) {
			throw new RuntimeException("region id previously added");
		}

		if (!regionPropertyIds.isEmpty()) {
			throw new RuntimeException("cannot add regions after region properties are defined");
		}
	}

	/**
	 * Adds the region property definition. Preconditions: (1)All inputs are
	 * non-null. (2) The region property was not previously added. (4) the
	 * property if valid definition contains a default value.
	 * 
	 * 
	 * @throws RuntimeException
	 *             <li>if the region property id is null</li>
	 *             <li>if the region property was previously added</li>
	 *             <li>if the property definition does not contain a default
	 *             value</li>
	 * 
	 */
	public void addRegionPropertyDefinition(RegionPropertyId regionPropertyId, PropertyDefinition propertyDefinition) {
		validateRegionPropertyAddition(regionPropertyId, propertyDefinition);
		regionPropertyIds.add(regionPropertyId);
		regionPropertyDefinitions.put(regionPropertyId, propertyDefinition);
		for (RegionId regionId : regionPropertyMap.keySet()) {
			Map<RegionPropertyId, PropertyValueRecord> map = regionPropertyMap.get(regionId);
			PropertyValueRecord propertyValueRecord = new PropertyValueRecord(simulationContext);
			if (propertyDefinition.getDefaultValue().isPresent()) {
				propertyValueRecord.setPropertyValue(propertyDefinition.getDefaultValue().get());
			}
			/*
			 * Potentially, the property value record can have a null value
			 * here. However, the initial data guarantees that there will be a
			 * property value stored for the region and thus the property value
			 * record will get a valid value once the resolver writes those
			 * values to the data manager.
			 */
			map.put(regionPropertyId, propertyValueRecord);
		}
	}

	private void validateRegionPropertyAddition(RegionPropertyId regionPropertyId, PropertyDefinition propertyDefinition) {

		if (regionPropertyId == null) {
			throw new RuntimeException("null region property id");
		}
		if (regionPropertyIdExists(regionPropertyId)) {
			throw new RuntimeException("region property previously added");
		}

	}

}
