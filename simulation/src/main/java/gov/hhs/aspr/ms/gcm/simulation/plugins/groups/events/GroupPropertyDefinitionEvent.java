package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support.GroupPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support.GroupTypeId;
import net.jcip.annotations.Immutable;

/**
 * An event released by the groups data manager whenever a group property
 * definition is added to the simulation.
 */
@Immutable
public record GroupPropertyDefinitionEvent(GroupTypeId groupTypeId, GroupPropertyId groupPropertyId) implements Event {
}
