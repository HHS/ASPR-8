package plugins.regions.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.SimulationContext;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import util.errors.ContractException;

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

	private static enum LabelerId implements EventLabelerId {
		PROPERTY, REGION_PROPERTY
	}

	private static void validateRegionPropertyId(SimulationContext simulationContext, RegionPropertyId regionPropertyId) {
		RegionsDataManager regionsDataManager = simulationContext.getDataManager(RegionsDataManager.class);
		regionsDataManager.getRegionPropertyDefinition(regionPropertyId);
	}

	private static void validateRegionId(SimulationContext simulationContext, RegionId regionId) {
		if (regionId == null) {
			throw new ContractException(RegionError.NULL_REGION_ID);
		}
		RegionsDataManager regionsDataManager = simulationContext.getDataManager(RegionsDataManager.class);
		if (!regionsDataManager.regionIdExists(regionId)) {
			throw new ContractException(RegionError.UNKNOWN_REGION_ID);
		}
	}

	public static EventLabel<RegionPropertyUpdateEvent> getEventLabelByRegionAndProperty(SimulationContext simulationContext, RegionId regionId, RegionPropertyId regionPropertyId) {
		validateRegionId(simulationContext, regionId);
		validateRegionPropertyId(simulationContext, regionPropertyId);
		return _getEventLabelByRegionAndProperty(regionId, regionPropertyId);//
	}
	
	private static EventLabel<RegionPropertyUpdateEvent> _getEventLabelByRegionAndProperty(RegionId regionId, RegionPropertyId regionPropertyId) {
		
		return EventLabel	.builder(RegionPropertyUpdateEvent.class)//
							.setEventLabelerId(LabelerId.REGION_PROPERTY)//
							.addKey(regionPropertyId)//
							.addKey(regionId)//
							.build();//
	}

	public static EventLabeler<RegionPropertyUpdateEvent> getEventLabelerForRegionAndProperty() {
		return EventLabeler	.builder(RegionPropertyUpdateEvent.class)//
							.setEventLabelerId(LabelerId.REGION_PROPERTY)//
							.setLabelFunction((context, event) -> _getEventLabelByRegionAndProperty(event.getRegionId(), event.getRegionPropertyId()))//
							.build();
	}

	public static EventLabel<RegionPropertyUpdateEvent> getEventLabelByProperty(SimulationContext simulationContext, RegionPropertyId regionPropertyId) {
		validateRegionPropertyId(simulationContext, regionPropertyId);
		return _getEventLabelByProperty(regionPropertyId);//
	}
	
	private static EventLabel<RegionPropertyUpdateEvent> _getEventLabelByProperty(RegionPropertyId regionPropertyId) {
		
		return EventLabel	.builder(RegionPropertyUpdateEvent.class)//
							.setEventLabelerId(LabelerId.PROPERTY)//
							.addKey(regionPropertyId)//
							.build();//

	}

	public static EventLabeler<RegionPropertyUpdateEvent> getEventLabelerForProperty() {
		return EventLabeler	.builder(RegionPropertyUpdateEvent.class)//
							.setEventLabelerId(LabelerId.PROPERTY)//
							.setLabelFunction((context, event) -> _getEventLabelByProperty(event.getRegionPropertyId()))//
							.build();
	}

	@Override
	public Object getPrimaryKeyValue() {
		return regionPropertyId;
	}

}