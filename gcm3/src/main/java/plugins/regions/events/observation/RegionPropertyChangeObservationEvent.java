package plugins.regions.events.observation;

import net.jcip.annotations.Immutable;
import nucleus.SimulationContext;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import nucleus.SimpleEventLabeler;
import plugins.regions.datacontainers.RegionDataView;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;

@Immutable
public class RegionPropertyChangeObservationEvent implements Event {
	private final RegionId regionId;
	private final RegionPropertyId regionPropertyId;
	private final Object previousPropertyValue;
	private final Object currentPropertyValue;

	public RegionPropertyChangeObservationEvent(RegionId regionId, RegionPropertyId regionPropertyId, Object previousPropertyValue, Object currentPropertyValue) {
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
		return "RegionPropertyChangeObservationEvent [regionId=" + regionId + ", regionPropertyId=" + regionPropertyId + ", previousPropertyValue=" + previousPropertyValue + ", currentPropertyValue=" + currentPropertyValue + "]";
	}

	private static enum LabelerId implements EventLabelerId {
		PROPERTY, REGION_PROPERTY
	}

	private static void validateRegionPropertyId(SimulationContext simulationContext, RegionPropertyId regionPropertyId) {
		RegionDataView regionDataView = simulationContext.getDataView(RegionDataView.class).get();
		regionDataView.getRegionPropertyDefinition(regionPropertyId);
	}

	private static void validateRegionId(SimulationContext simulationContext, RegionId regionId) {
		if (regionId == null) {
			simulationContext.throwContractException(RegionError.NULL_REGION_ID);
		}
		RegionDataView regionDataView = simulationContext.getDataView(RegionDataView.class).get();
		if (!regionDataView.regionIdExists(regionId)) {
			simulationContext.throwContractException(RegionError.UNKNOWN_REGION_ID);
		}
	}

	public static EventLabel<RegionPropertyChangeObservationEvent> getEventLabelByProperty(SimulationContext simulationContext, RegionPropertyId regionPropertyId) {
		validateRegionPropertyId(simulationContext, regionPropertyId);
		return new MultiKeyEventLabel<>(regionPropertyId, LabelerId.PROPERTY, RegionPropertyChangeObservationEvent.class, regionPropertyId);
	}

	public static EventLabel<RegionPropertyChangeObservationEvent> getEventLabelByRegionAndProperty(SimulationContext simulationContext, RegionId regionId, RegionPropertyId regionPropertyId) {
		validateRegionId(simulationContext, regionId);
		validateRegionPropertyId(simulationContext, regionPropertyId);
		return new MultiKeyEventLabel<>(regionPropertyId, LabelerId.REGION_PROPERTY, RegionPropertyChangeObservationEvent.class, regionId, regionPropertyId);
	}

	public static EventLabeler<RegionPropertyChangeObservationEvent> getEventLabelerForRegionAndProperty() {
		return new SimpleEventLabeler<>(LabelerId.REGION_PROPERTY, RegionPropertyChangeObservationEvent.class, (context, event) -> new MultiKeyEventLabel<>(event.getRegionPropertyId(), LabelerId.REGION_PROPERTY, RegionPropertyChangeObservationEvent.class, event.getRegionId(), event.getRegionPropertyId()));
	}

	public static EventLabeler<RegionPropertyChangeObservationEvent> getEventLabelerForProperty() {
		return new SimpleEventLabeler<>(LabelerId.PROPERTY, RegionPropertyChangeObservationEvent.class, (context, event) -> new MultiKeyEventLabel<>(event.getRegionPropertyId(), LabelerId.PROPERTY, RegionPropertyChangeObservationEvent.class, event.getRegionPropertyId()));
	}

	@Override
	public Object getPrimaryKeyValue() {
		return regionPropertyId;
	}

}
