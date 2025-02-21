package gov.hhs.aspr.ms.gcm.simulation.plugins.people.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonConstructionData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import net.jcip.annotations.Immutable;

/**
 * An event for notifying plugins that a person is being added to the
 * simulation. During this process, the person is not fully formed until all
 * subscribers to the event have been invoked.
 */
@Immutable
public record PersonImminentAdditionEvent(PersonId personId, PersonConstructionData personConstructionData)
		implements Event {
}
