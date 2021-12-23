package plugins.regions.events.observation;

import net.jcip.annotations.Immutable;
import nucleus.Context;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import nucleus.SimpleEventLabeler;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.regions.datacontainers.RegionDataView;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;

@Immutable
public class PersonRegionChangeObservationEvent implements Event {
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

	private static void validateRegionId(Context context, RegionId regionId) {
		if (regionId == null) {
			context.throwContractException(RegionError.NULL_REGION_ID);
		}
		RegionDataView regionDataView = context.getDataView(RegionDataView.class).get();
		if (!regionDataView.regionIdExists(regionId)) {
			context.throwContractException(RegionError.UNKNOWN_REGION_ID);
		}
	}

	private static void validatePersonId(Context context, PersonId personId) {
		if (personId == null) {
			context.throwContractException(PersonError.NULL_PERSON_ID);
		}
		PersonDataView personDataView = context.getDataView(PersonDataView.class).get();
		if (!personDataView.personExists(personId)) {
			context.throwContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	public static EventLabel<PersonRegionChangeObservationEvent> getEventLabelByArrivalRegion(Context context, RegionId regionId) {
		validateRegionId(context, regionId);
		return new MultiKeyEventLabel<>(PersonRegionChangeObservationEvent.class, LabelerId.ARRIVAL, PersonRegionChangeObservationEvent.class, regionId);
	}

	public static EventLabeler<PersonRegionChangeObservationEvent> getEventLabelerForArrivalRegion() {
		return new SimpleEventLabeler<>(LabelerId.ARRIVAL, PersonRegionChangeObservationEvent.class, (context, event) -> new MultiKeyEventLabel<>(PersonRegionChangeObservationEvent.class, LabelerId.ARRIVAL, PersonRegionChangeObservationEvent.class, event.getCurrentRegionId()));
	}

	public static EventLabel<PersonRegionChangeObservationEvent> getEventLabelByDepartureRegion(Context context, RegionId regionId) {
		validateRegionId(context, regionId);
		return new MultiKeyEventLabel<>(PersonRegionChangeObservationEvent.class, LabelerId.DEPARTURE, PersonRegionChangeObservationEvent.class, regionId);
	}

	public static EventLabeler<PersonRegionChangeObservationEvent> getEventLabelerForDepartureRegion() {
		return new SimpleEventLabeler<>(LabelerId.DEPARTURE, PersonRegionChangeObservationEvent.class, (context, event) -> new MultiKeyEventLabel<>(PersonRegionChangeObservationEvent.class, LabelerId.DEPARTURE, PersonRegionChangeObservationEvent.class, event.getPreviousRegionId()));
	}

	public static EventLabel<PersonRegionChangeObservationEvent> getEventLabelByPerson(Context context, PersonId personId) {
		validatePersonId(context, personId);
		return new MultiKeyEventLabel<>(PersonRegionChangeObservationEvent.class, LabelerId.PERSON, PersonRegionChangeObservationEvent.class, personId);
	}

	public static EventLabeler<PersonRegionChangeObservationEvent> getEventLabelerForPerson() {
		return new SimpleEventLabeler<>(LabelerId.PERSON, PersonRegionChangeObservationEvent.class, (context, event) -> new MultiKeyEventLabel<>(PersonRegionChangeObservationEvent.class, LabelerId.PERSON, PersonRegionChangeObservationEvent.class, event.getPersonId()));
	}

}
