package plugins.regions.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.EventLabel;
import nucleus.SimulationContext;
import nucleus.util.ContractException;
import plugins.people.PersonDataManager;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.regions.datamanagers.RegionDataManager;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;

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
		RegionDataManager regionDataManager = simulationContext.getDataManager(RegionDataManager.class).get();
		if (!regionDataManager.regionIdExists(regionId)) {
			throw new ContractException(RegionError.UNKNOWN_REGION_ID);
		}
	}

	private static void validatePersonId(SimulationContext simulationContext, PersonId personId) {
		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
		PersonDataManager personDataManager = simulationContext.getDataManager(PersonDataManager.class).get();
		if (!personDataManager.personExists(personId)) {
			throw new ContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	public static EventLabel<PersonRegionUpdateEvent> getEventLabelByArrivalRegion(SimulationContext simulationContext, RegionId regionId) {
		validateRegionId(simulationContext, regionId);
		return new EventLabel<>(PersonRegionUpdateEvent.class, LabelerId.ARRIVAL, PersonRegionUpdateEvent.class, regionId);
	}

	public static EventLabeler<PersonRegionUpdateEvent> getEventLabelerForArrivalRegion() {
		return EventLabeler	.builder(PersonRegionUpdateEvent.class)//
							.setEventLabelerId(LabelerId.ARRIVAL)//
							.setLabelFunction((context, event) -> getEventLabelByArrivalRegion(context, event.getCurrentRegionId()))//
							.build();
	}

	public static EventLabel<PersonRegionUpdateEvent> getEventLabelByDepartureRegion(SimulationContext simulationContext, RegionId regionId) {
		validateRegionId(simulationContext, regionId);
		return new EventLabel<>(PersonRegionUpdateEvent.class, LabelerId.DEPARTURE, PersonRegionUpdateEvent.class, regionId);
	}

	public static EventLabeler<PersonRegionUpdateEvent> getEventLabelerForDepartureRegion() {
		return EventLabeler	.builder(PersonRegionUpdateEvent.class)//
							.setEventLabelerId(LabelerId.DEPARTURE)//
							.setLabelFunction((context, event) -> getEventLabelByDepartureRegion(context, event.getPreviousRegionId()))//
							.build();
	}

	public static EventLabel<PersonRegionUpdateEvent> getEventLabelByPerson(SimulationContext simulationContext, PersonId personId) {
		validatePersonId(simulationContext, personId);
		return new EventLabel<>(PersonRegionUpdateEvent.class, LabelerId.PERSON, PersonRegionUpdateEvent.class, personId);
	}

	public static EventLabeler<PersonRegionUpdateEvent> getEventLabelerForPerson() {
		return EventLabeler	.builder(PersonRegionUpdateEvent.class)//
							.setEventLabelerId(LabelerId.PERSON)//
							.setLabelFunction((context, event) -> getEventLabelByPerson(context, event.getPersonId()))//
							.build();
	}

}
