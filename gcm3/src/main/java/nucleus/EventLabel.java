package nucleus;

import net.jcip.annotations.NotThreadSafe;
import nucleus.util.ContractException;
import util.wrappers.MultiKey;

/**
 * 
 * A generics-based class that is used to filter event observations.
 * 
 * When an actor subscribes to observe a particular type of event, it may need
 * to filter such events.
 * 
 * For example, suppose a fox(actor) has subscribed to actor movement events.
 * The fox is only interested in rabbits that are close by and is unconcerned
 * with distant rabbits or other animals. When the fox subscribes to movement
 * event observation, it uses an event label to describe this filtering. When
 * any animal moves, a movement observation event is generated by some plugin.
 * Nucleus will then generate an event label from the event using a registered
 * event-labeler. If this generated event label matches, via equality, the event
 * label used by the fox, then the fox will receive the movement observation
 * event.
 * 
 * Event labels are paired with event labelers. A data manager registers an
 * event labeler in anticipation of actors needing to use the corresponding
 * event labels. Event labels remain active until the actor unsubscribes them.
 * 
 * 
 * @author Shawn Hatch
 *
 * @param <T>
 */
@NotThreadSafe
public final class EventLabel<T extends Event>  {

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
	public EventLabel(final Object primaryKeyValue, final EventLabelerId labelerId, final Class<T> eventClass, final Object... keys) {
		
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
		if (!(obj instanceof EventLabel)) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		EventLabel other = (EventLabel) obj;
		return multiKey.equals(other.multiKey);
	}

	
	public Class<T> getEventClass() {
		return eventClass;
	}

	
	public EventLabelerId getLabelerId() {
		return labelerId;
	}

	
	public Object getPrimaryKeyValue() {
		return primaryKeyValue;
	}

}
