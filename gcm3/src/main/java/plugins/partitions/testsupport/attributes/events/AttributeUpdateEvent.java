package plugins.partitions.testsupport.attributes.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import nucleus.SimpleEventLabeler;
import nucleus.SimulationContext;
import nucleus.util.ContractException;
import plugins.partitions.testsupport.attributes.AttributesDataManager;
import plugins.partitions.testsupport.attributes.support.AttributeError;
import plugins.partitions.testsupport.attributes.support.AttributeId;
import plugins.people.support.PersonId;

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
	 * Returns an event label used to subscribe to
	 * {@link AttributeUpdateEvent} events. Matches on attribute id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain AttributeError#NULL_ATTRIBUTE_ID} if the attribute id
	 *             is null</li>
	 *             <li>{@linkplain AttributeError#UNKNOWN_ATTRIBUTE_ID} if the attribute
	 *             id is not known</li>
	 */
	public static EventLabel<AttributeUpdateEvent> getEventLabel(final SimulationContext simulationContext, final AttributeId attributeId) {
		validateAttributed(simulationContext, attributeId);
		return new MultiKeyEventLabel<>(attributeId, LabelerId.ATTRIBUTE, AttributeUpdateEvent.class, attributeId);
	}
	
	/**
	 * Returns an event labeler for
	 * {@link AttributeUpdateEvent} events that uses only
	 * the attribute id. Automatically added at initialization.
	 */
	public static EventLabeler<AttributeUpdateEvent> getEventLabeler() {
		return new SimpleEventLabeler<>(LabelerId.ATTRIBUTE, AttributeUpdateEvent.class,
				(context, event) -> new MultiKeyEventLabel<>(event.getAttributeId(), LabelerId.ATTRIBUTE, AttributeUpdateEvent.class, event.getAttributeId()));
	}

	
	private static void validateAttributed(final SimulationContext simulationContext, final AttributeId attributeId) {
		if (attributeId == null) {
			throw new ContractException(AttributeError.NULL_ATTRIBUTE_ID);
		}
		final AttributesDataManager attributesDataManager = simulationContext.getDataManager(AttributesDataManager.class).get();
		if (!attributesDataManager.attributeExists(attributeId)) {
			throw new ContractException(AttributeError.UNKNOWN_ATTRIBUTE_ID);
		}
	}
}
