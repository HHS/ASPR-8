package nucleus.somethingborrowed;

import java.util.ArrayList;
import java.util.List;

import net.jcip.annotations.NotThreadSafe;
import nucleus.Event;
import nucleus.NucleusError;
import nucleus.util.ContractException;
import util.wrappers.MultiKey;

/**
 * A utility class that implements an event label over an ordered tuple of
 * values
 *
 * @author Shawn Hatch
 *
 */
@NotThreadSafe
public class MultiKeyEventLabelX<T extends Event> implements EventLabelX<T> {

	private static class Data<N extends Event> {
		private List<Object> keys = new ArrayList<>();
		private EventLabelerX<N> labeler;
		private Class<N> eventClass;
	}

	private final EventLabelerX<T> labeler;
	private final MultiKey multiKey;
	private final Class<T> eventClass;
	private final Object primaryKeyValue;

	/**
	 * Returns an instance of the builder for the given class.
	 * 
	 *   @throws ContractException
	 *   <li>{@linkplain NucleusError#NULL_EVENT_CLASS} if the class reference is null </li>
	 *   
	 */
	public static <N extends Event> Builder<N> builder(Class<N> classReference) {
		if (classReference == null) {
			throw new ContractException(NucleusError.NULL_EVENT_CLASS);
		}
		return new Builder<N>(classReference);
	}

	public static class Builder<N extends Event> {
		private Data<N> data = new Data<>();

		private final Class<N> eventClass;

		private Builder(Class<N> classReference) {
			this.eventClass = classReference;
		}

		/**
		 * Constructs a new MultiKeyEventLabel.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain NucleusError.NULL_PRIMARY_KEY_VALUE} if
		 *             no keys were added</li>
		 *             <li>{@linkplain NucleusError.NULL_EVENT_LABELER} if no
		 *             event labeler was set</li>
		 */
		public MultiKeyEventLabelX<N> build() {
			try {
				data.eventClass = this.eventClass;
				return new MultiKeyEventLabelX<>(data);
			} finally {
				data = new Data<>();
			}
		}

		/**
		 * Sets the event labeler for the event label
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain NucleusError#NULL_EVENT_LABELER} if the
		 *             labeler is null</li>
		 */
		public Builder<N> setEventLabeler(EventLabelerX<N> labeler) {
			if (labeler == null) {
				throw new ContractException(NucleusError.NULL_EVENT_LABELER);
			}
			data.labeler = labeler;
			return this;
		}

		/**
		 * Adds a key to the event label
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain NucleusError#NULL_EVENT_LABEL_KEY} if the
		 *             key is null</li>
		 */
		public Builder<N> addKey(Object key) {
			if (key == null) {
				throw new ContractException(NucleusError.NULL_EVENT_LABEL_KEY);
			}
			data.keys.add(key);
			return this;
		}

	}

	private MultiKeyEventLabelX(Data<T> data) {

		if (data.keys.isEmpty()) {
			throw new ContractException(NucleusError.NULL_PRIMARY_KEY_VALUE);
		}

		if (data.labeler == null) {
			throw new ContractException(NucleusError.NULL_EVENT_LABELER);
		}

		this.primaryKeyValue = data.keys.get(0);
		this.eventClass = data.eventClass;
		this.labeler = data.labeler;
		MultiKey.Builder builder = MultiKey.builder();
		for (Object key : data.keys) {
			builder.addKey(key);
		}
		multiKey = builder.build();
	}

	@Override
	public int hashCode() {
		/*
		 * Justify the use of a non-standard approach to equals: this was done
		 * to gain efficiency, but should we use a correct implementation or
		 * force this to be the only implementation class of the event label?
		 */
		return multiKey.hashCode();
	}

	/**
	 * NOTE: Nucleus only checks for equality between event labels when those
	 * labels have the same primary keys, event class types.
	 */

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof MultiKeyEventLabelX)) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		MultiKeyEventLabelX other = (MultiKeyEventLabelX) obj;
		return multiKey.equals(other.multiKey);
	}

	@Override
	public Class<T> getEventClass() {
		return eventClass;
	}

	@Override
	public EventLabelerX<T> getLabeler() {
		return labeler;
	}

	@Override
	public Object getPrimaryKeyValue() {
		return primaryKeyValue;
	}

}
