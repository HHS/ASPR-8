package plugins.regions.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.NucleusError;
import plugins.people.support.PersonId;
import plugins.regions.support.RegionId;
import util.ContractException;

/**
 * Sets the person's region. Region assignment may only be set by the owning
 * compartment, except for person creation.
 *
 *
 */
@Immutable
public final class PersonRegionAssignmentEvent implements Event {

	private final PersonId personId;

	private final RegionId regionId;

	/**
	 * @throws ContractException
	 *
	 *
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the person id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the person id
	 *             is unknown
	 *             <li>{@link NucleusError#NULL_REGION_ID} if the region id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_REGION_ID} if the region id
	 *             is unknown
	 *             <li>{@link NucleusError#SAME_REGION} if the region is the
	 *             current region for the person
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if the
	 *             invoker is not a global component or the person's current
	 *             region
	 *
	 */
	public PersonRegionAssignmentEvent(PersonId personId, RegionId regionId) {
		super();
		this.personId = personId;
		this.regionId = regionId;
	}

	public PersonId getPersonId() {
		return personId;
	}

	public RegionId getRegionId() {
		return regionId;
	}

}
