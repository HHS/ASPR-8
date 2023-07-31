package plugins.partitions.testsupport.attributes.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.SimulationContext;
import plugins.partitions.testsupport.attributes.AttributesDataManager;
import plugins.partitions.testsupport.attributes.support.AttributeError;
import plugins.partitions.testsupport.attributes.support.AttributeId;
import plugins.people.support.PersonId;
import util.errors.ContractException;

@Immutable
public class AttributeUpdateEvent implements Event {
	private final PersonId personId;
	private final AttributeId attributeId;
	private final Object previousValue;
	private final Object currentValue;

	public AttributeUpdateEvent(final PersonId personId, final AttributeId attributeId, final Object previousValue, final Object currentValue) {
		super();
		this.personId = personId;
		this.attributeId = attributeId;
		this.previousValue = previousValue;
		this.currentValue = currentValue;
	}

	public Object getCurrentValue() {
		return currentValue;
	}

	public AttributeId getAttributeId() {
		return attributeId;
	}

	public PersonId getPersonId() {
		return personId;
	}

	public Object getPreviousValue() {
		return previousValue;
	}

	@Override
	public Object getPrimaryKeyValue() {
		return attributeId;
	}

	private static enum LabelerId implements EventLabelerId {
		ATTRIBUTE
	}

	/**
	 * Returns an event label used to subscribe to {@link AttributeUpdateEvent}
	 * events. Matches on attribute id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain AttributeError#NULL_ATTRIBUTE_ID} if the
	 *             attribute id is null</li>
	 *             <li>{@linkplain AttributeError#UNKNOWN_ATTRIBUTE_ID} if the
	 *             attribute id is not known</li>
	 */
	public static EventLabel<AttributeUpdateEvent> getEventLabel(final SimulationContext simulationContext, final AttributeId attributeId) {
		validateAttributed(simulationContext, attributeId);
		return EventLabel.builder(AttributeUpdateEvent.class)//
				.setEventLabelerId(LabelerId.ATTRIBUTE)
				.addKey(attributeId)
				.build();
	}
	
	private static EventLabel<AttributeUpdateEvent> _getEventLabel(final AttributeId attributeId) {
		
		return EventLabel.builder(AttributeUpdateEvent.class)//
				.setEventLabelerId(LabelerId.ATTRIBUTE)
				.addKey(attributeId)
				.build();
	}

	/**
	 * Returns an event labeler for {@link AttributeUpdateEvent} events that
	 * uses only the attribute id. Automatically added at initialization.
	 */
	public static EventLabeler<AttributeUpdateEvent> getEventLabeler() {
		return EventLabeler	.builder(AttributeUpdateEvent.class)//
							.setEventLabelerId(LabelerId.ATTRIBUTE)//
							.setLabelFunction((context, event) -> _getEventLabel(event.getAttributeId()))//
							.build();
	}

	private static void validateAttributed(final SimulationContext simulationContext, final AttributeId attributeId) {
		if (attributeId == null) {
			throw new ContractException(AttributeError.NULL_ATTRIBUTE_ID);
		}
		final AttributesDataManager attributesDataManager = simulationContext.getDataManager(AttributesDataManager.class);
		if (!attributesDataManager.attributeExists(attributeId)) {
			throw new ContractException(AttributeError.UNKNOWN_ATTRIBUTE_ID);
		}
	}
}