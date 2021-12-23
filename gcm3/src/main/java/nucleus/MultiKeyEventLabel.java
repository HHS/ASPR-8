package nucleus;

import net.jcip.annotations.NotThreadSafe;
import util.ContractException;
import util.MultiKey;

/**
 * A utility class that implements an event label over an ordered tuple of
 * values
 *
 * @author Shawn Hatch
 *
 */
@NotThreadSafe
public class MultiKeyEventLabel<T extends Event> implements EventLabel<T> {

	private final MultiKey multiKey;

	private final Class<T> eventClass;

	private final EventLabelerId labelerId;

	private final Object primaryKeyValue;

	/**
	 * Constructs a new MultiKeyEventLabel.
	 * 
	 * @throws ContractException
	 *             <li>if the primary key value is null
	 *             <li>if the event labeler id is null
	 *             <li>if the event class is null
	 * 
	 * @param primaryKeyValue
	 *            -- the primary key that will be returned by this event label
	 * @param labelerId
	 *            -- the labelerId that will be returned by this event label
	 * @param eventClass
	 *            -- the event class that will be returned by this event label
	 * @param keys
	 *            -- the various order-sensitive keys that will used for
	 *            equality comparisons between event labels
	 * 
	 * 
	 */
	public MultiKeyEventLabel(final Object primaryKeyValue, final EventLabelerId labelerId, final Class<T> eventClass, final Object... keys) {
		
		if(primaryKeyValue == null) {
			throw new ContractException(NucleusError.NULL_PRIMARY_KEY_VALUE);
		}
		
		if(labelerId == null) {
			throw new ContractException(NucleusError.NULL_LABELER_ID_IN_EVENT_LABEL);
		}
		
		if(eventClass == null) {
			throw new ContractException(NucleusError.NULL_EVENT_CLASS_IN_EVENT_LABEL);
		}

		this.primaryKeyValue = primaryKeyValue;
		this.eventClass = eventClass;
		this.labelerId = labelerId;
		multiKey = new MultiKey(keys);
	}

	@Override
	public int hashCode() {
		/*
		 * Justify the use of a non-standard approach to equals: this
		 * was done to gain efficiency, but should we use a correct
		 * implementation or force this to be the only implementation class of
		 * the event label?
		 */
		return multiKey.hashCode();
	}

	/**
	 * NOTE: Nucleus only checks for equality between event labels when those
	 * labels have the same primary keys, event class types and labeler ids.
	 */

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof MultiKeyEventLabel)) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		MultiKeyEventLabel other = (MultiKeyEventLabel) obj;
		return multiKey.equals(other.multiKey);
	}

	@Override
	public Class<T> getEventClass() {
		return eventClass;
	}

	@Override
	public EventLabelerId getLabelerId() {
		return labelerId;
	}

	@Override
	public Object getPrimaryKeyValue() {
		return primaryKeyValue;
	}

}
