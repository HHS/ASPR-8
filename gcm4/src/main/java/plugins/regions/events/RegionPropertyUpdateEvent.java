package plugins.regions.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;

@Immutable
public final class RegionPropertyUpdateEvent implements Event {
	private final RegionId regionId;
	private final RegionPropertyId regionPropertyId;
	private final Object previousPropertyValue;
	private final Object currentPropertyValue;

	public RegionPropertyUpdateEvent(RegionId regionId, RegionPropertyId regionPropertyId, Object previousPropertyValue, Object currentPropertyValue) {
		super();
		this.regionId = regionId;
		this.regionPropertyId = regionPropertyId;
		this.previousPropertyValue = previousPropertyValue;
		this.currentPropertyValue = currentPropertyValue;
	}

	public RegionId getRegionId() {
		return regionId;
	}

	public RegionPropertyId getRegionPropertyId() {
		return regionPropertyId;
	}

	public Object getPreviousPropertyValue() {
		return previousPropertyValue;
	}

	public Object getCurrentPropertyValue() {
		return currentPropertyValue;
	}

	@Override
	public String toString() {
		return "RegionPropertyUpdateEvent [regionId=" + regionId + ", regionPropertyId=" + regionPropertyId + ", previousPropertyValue=" + previousPropertyValue + ", currentPropertyValue="
				+ currentPropertyValue + "]";
	}

}
