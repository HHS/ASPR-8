package gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.support.PersonPropertyId;
import net.jcip.annotations.Immutable;

/**
 * An event released by the people data manager whenever a person property
 * definition is added to the simulation.
 */
@Immutable
public record PersonPropertyDefinitionEvent(PersonPropertyId personPropertyId) implements Event {
}
