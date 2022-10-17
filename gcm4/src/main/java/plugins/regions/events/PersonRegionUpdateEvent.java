package plugins.regions.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.people.support.PersonId;
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

}
