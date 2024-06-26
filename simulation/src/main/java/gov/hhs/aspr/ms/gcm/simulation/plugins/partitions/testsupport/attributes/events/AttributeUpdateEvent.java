package gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.attributes.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.attributes.support.AttributeId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import net.jcip.annotations.Immutable;

@Immutable
public record AttributeUpdateEvent(PersonId personId, AttributeId attributeId, Object previousValue,
		Object currentValue) implements Event {

	@SuppressWarnings("unchecked")
	public <T> T getPreviousValue() {
		return (T) previousValue;
	}

	@SuppressWarnings("unchecked")
	public <T> T getCurrentValue() {
		return (T) currentValue;
	}

}
