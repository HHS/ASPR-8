package plugins.regions.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.SimulationContext;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import util.errors.ContractException;

@Immutable
public final class PersonRegionUpdateEvent implements Event {
	private final PersonId personId;
	private final RegionId previousRegionId;
	private final RegionId currentRegionId;

	public PersonRegionUpdateEvent(final PersonId personId, final RegionId previousRegionId, final RegionId currentRegionId) {
		super();
		this.personId = personId;
		this.previousRegionId = previousRegionId;
		this.currentRegionId = currentRegionId;
	}

	public RegionId getCurrentRegionId() {
		return currentRegionId;
	}

	public PersonId getPersonId() {
		return personId;
	}

	public RegionId getPreviousRegionId() {
		return previousRegionId;
	}

	@Override
	public String toString() {
		return "PersonRegionUpdateEvent [personId=" + personId + ", previousRegionId=" + previousRegionId + ", currentRegionId=" + currentRegionId + "]";
	}

	private static enum LabelerId implements EventLabelerId {
		ARRIVAL, DEPARTURE, PERSON
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

	private static void validatePersonId(SimulationContext simulationContext, PersonId personId) {
		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
		PeopleDataManager peopleDataManager = simulationContext.getDataManager(PeopleDataManager.class);
		if (!peopleDataManager.personExists(personId)) {
			throw new ContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	public static EventLabel<PersonRegionUpdateEvent> getEventLabelByArrivalRegion(SimulationContext simulationContext, RegionId regionId) {
		validateRegionId(simulationContext, regionId);
		return _getEventLabelByArrivalRegion(regionId);//
	}
	
	private static EventLabel<PersonRegionUpdateEvent> _getEventLabelByArrivalRegion(RegionId regionId) {
		
		return EventLabel	.builder(PersonRegionUpdateEvent.class)//
							.setEventLabelerId(LabelerId.ARRIVAL)//
							.addKey(PersonRegionUpdateEvent.class)//
							.addKey(regionId)//
							.build();//
	}

	public static EventLabeler<PersonRegionUpdateEvent> getEventLabelerForArrivalRegion() {
		return EventLabeler	.builder(PersonRegionUpdateEvent.class)//
							.setEventLabelerId(LabelerId.ARRIVAL)//
							.setLabelFunction((context, event) -> _getEventLabelByArrivalRegion(event.getCurrentRegionId()))//
							.build();
	}

	public static EventLabel<PersonRegionUpdateEvent> getEventLabelByDepartureRegion(SimulationContext simulationContext, RegionId regionId) {
		validateRegionId(simulationContext, regionId);
		return _getEventLabelByDepartureRegion(regionId);//
	}
	
	private static EventLabel<PersonRegionUpdateEvent> _getEventLabelByDepartureRegion(RegionId regionId) {
		
		return EventLabel	.builder(PersonRegionUpdateEvent.class)//
							.setEventLabelerId(LabelerId.DEPARTURE)//
							.addKey(PersonRegionUpdateEvent.class)//
							.addKey(regionId)//
							.build();//
	}

	public static EventLabeler<PersonRegionUpdateEvent> getEventLabelerForDepartureRegion() {
		return EventLabeler	.builder(PersonRegionUpdateEvent.class)//
							.setEventLabelerId(LabelerId.DEPARTURE)//
							.setLabelFunction((context, event) -> _getEventLabelByDepartureRegion(event.getPreviousRegionId()))//
							.build();
	}

	public static EventLabel<PersonRegionUpdateEvent> getEventLabelByPerson(SimulationContext simulationContext, PersonId personId) {
		validatePersonId(simulationContext, personId);
		return _getEventLabelByPerson(personId);//
	}
	
	private static EventLabel<PersonRegionUpdateEvent> _getEventLabelByPerson(PersonId personId) {
		
		return EventLabel	.builder(PersonRegionUpdateEvent.class)//
							.setEventLabelerId(LabelerId.PERSON)//
							.addKey(PersonRegionUpdateEvent.class)//
							.addKey(personId)//
							.build();//
	}

	public static EventLabeler<PersonRegionUpdateEvent> getEventLabelerForPerson() {
		return EventLabeler	.builder(PersonRegionUpdateEvent.class)//
							.setEventLabelerId(LabelerId.PERSON)//
							.setLabelFunction((context, event) -> _getEventLabelByPerson(event.getPersonId()))//
							.build();
	}

}
