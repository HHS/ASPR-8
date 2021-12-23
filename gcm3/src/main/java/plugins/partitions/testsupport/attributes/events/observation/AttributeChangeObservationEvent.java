package plugins.partitions.testsupport.attributes.events.observation;

import net.jcip.annotations.Immutable;
import nucleus.Context;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import nucleus.SimpleEventLabeler;
import plugins.partitions.testsupport.attributes.datacontainers.AttributesDataView;
import plugins.partitions.testsupport.attributes.support.AttributeError;
import plugins.partitions.testsupport.attributes.support.AttributeId;
import plugins.people.support.PersonId;
import util.ContractException;

@Immutable
public class AttributeChangeObservationEvent implements Event {
	private final PersonId personId;
	private final AttributeId attributeId;
	private final Object previousValue;
	private final Object currentValue;

	public AttributeChangeObservationEvent(final PersonId personId, final AttributeId attributeId, final Object previousValue, final Object currentValue) {
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
	 * {@link AttributeChangeObservationEvent} events. Matches on attribute id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain AttributeError#NULL_ATTRIBUTE_ID} if the attribute id
	 *             is null</li>
	 *             <li>{@linkplain AttributeError#UNKNOWN_ATTRIBUTE_ID} if the attribute
	 *             id is not known</li>
	 */
	public static EventLabel<AttributeChangeObservationEvent> getEventLabel(final Context context, final AttributeId attributeId) {
		validateAttributed(context, attributeId);
		return new MultiKeyEventLabel<>(attributeId, LabelerId.ATTRIBUTE, AttributeChangeObservationEvent.class, attributeId);
	}
	
	/**
	 * Returns an event labeler for
	 * {@link AttributeChangeObservationEvent} events that uses only
	 * the attribute id. Automatically added at initialization.
	 */
	public static EventLabeler<AttributeChangeObservationEvent> getEventLabeler() {
		return new SimpleEventLabeler<>(LabelerId.ATTRIBUTE, AttributeChangeObservationEvent.class,
				(context, event) -> new MultiKeyEventLabel<>(event.getAttributeId(), LabelerId.ATTRIBUTE, AttributeChangeObservationEvent.class, event.getAttributeId()));
	}

	
	private static void validateAttributed(final Context context, final AttributeId attributeId) {
		if (attributeId == null) {
			context.throwContractException(AttributeError.NULL_ATTRIBUTE_ID);
		}
		final AttributesDataView attributesDataView = context.getDataView(AttributesDataView.class).get();
		if (!attributesDataView.attributeExists(attributeId)) {
			context.throwContractException(AttributeError.UNKNOWN_ATTRIBUTE_ID);
		}
	}
}
