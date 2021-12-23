package plugins.regions.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.NucleusError;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import util.ContractException;

/**
 * Sets property value for the given region and property.
 *
 */
@Immutable
public final class RegionPropertyValueAssignmentEvent implements Event {

	private final RegionId regionId;

	private final RegionPropertyId regionPropertyId;

	private final Object regionPropertyValue;

	/**
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_REGION_ID} if the region id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_REGION_ID} if the region id
	 *             is unknown
	 *             <li>{@link NucleusError#NULL_REGION_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_REGION_PROPERTY_ID} if the
	 *             property id is unknown
	 *             <li>{@link NucleusError#NULL_REGION_PROPERTY_VALUE} if the
	 *             value is null
	 *             <li>{@link NucleusError#INCOMPATIBLE_VALUE} if the value is
	 *             incompatible with the defined type for the property
	 *             <li>{@link NucleusError#IMMUTABLE_VALUE} if the property has
	 *             been defined as immutable
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if the
	 *             invoker is not a global component or the given region
	 *
	 */
	public RegionPropertyValueAssignmentEvent(RegionId regionId, RegionPropertyId regionPropertyId, Object regionPropertyValue) {
		super();
		this.regionId = regionId;
		this.regionPropertyId = regionPropertyId;
		this.regionPropertyValue = regionPropertyValue;
	}

	public RegionId getRegionId() {
		return regionId;
	}

	public RegionPropertyId getRegionPropertyId() {
		return regionPropertyId;
	}

	public Object getRegionPropertyValue() {
		return regionPropertyValue;
	}

}
