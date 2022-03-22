package plugins.regions.datamanagers;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import nucleus.SimpleEventLabeler;
import nucleus.SimulationContext;
import nucleus.util.ContractException;
import plugins.people.PersonDataManager;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;

@Immutable
public final class PersonRegionChangeObservationEvent implements Event {
	private final PersonId personId;
	private final RegionId previousRegionId;
	private final RegionId currentRegionId;


	public PersonRegionChangeObservationEvent(final PersonId personId, final RegionId previousRegionId, final RegionId currentRegionId) {
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
		return "PersonRegionChangeObservationEvent [personId=" + personId + ", previousRegionId=" + previousRegionId + ", currentRegionId=" + currentRegionId + "]";
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

	public static EventLabel<PersonRegionChangeObservationEvent> getEventLabelByArrivalRegion(SimulationContext simulationContext, RegionId regionId) {
		validateRegionId(simulationContext, regionId);
		return new MultiKeyEventLabel<>(PersonRegionChangeObservationEvent.class, LabelerId.ARRIVAL, PersonRegionChangeObservationEvent.class, regionId);
	}

	public static EventLabeler<PersonRegionChangeObservationEvent> getEventLabelerForArrivalRegion() {
		return new SimpleEventLabeler<>(LabelerId.ARRIVAL, PersonRegionChangeObservationEvent.class, (context, event) -> new MultiKeyEventLabel<>(PersonRegionChangeObservationEvent.class, LabelerId.ARRIVAL, PersonRegionChangeObservationEvent.class, event.getCurrentRegionId()));
	}

	public static EventLabel<PersonRegionChangeObservationEvent> getEventLabelByDepartureRegion(SimulationContext simulationContext, RegionId regionId) {
		validateRegionId(simulationContext, regionId);
		return new MultiKeyEventLabel<>(PersonRegionChangeObservationEvent.class, LabelerId.DEPARTURE, PersonRegionChangeObservationEvent.class, regionId);
	}

	public static EventLabeler<PersonRegionChangeObservationEvent> getEventLabelerForDepartureRegion() {
		return new SimpleEventLabeler<>(LabelerId.DEPARTURE, PersonRegionChangeObservationEvent.class, (context, event) -> new MultiKeyEventLabel<>(PersonRegionChangeObservationEvent.class, LabelerId.DEPARTURE, PersonRegionChangeObservationEvent.class, event.getPreviousRegionId()));
	}

	public static EventLabel<PersonRegionChangeObservationEvent> getEventLabelByPerson(SimulationContext simulationContext, PersonId personId) {
		validatePersonId(simulationContext, personId);
		return new MultiKeyEventLabel<>(PersonRegionChangeObservationEvent.class, LabelerId.PERSON, PersonRegionChangeObservationEvent.class, personId);
	}

	public static EventLabeler<PersonRegionChangeObservationEvent> getEventLabelerForPerson() {
		return new SimpleEventLabeler<>(LabelerId.PERSON, PersonRegionChangeObservationEvent.class, (context, event) -> new MultiKeyEventLabel<>(PersonRegionChangeObservationEvent.class, LabelerId.PERSON, PersonRegionChangeObservationEvent.class, event.getPersonId()));
	}

}
