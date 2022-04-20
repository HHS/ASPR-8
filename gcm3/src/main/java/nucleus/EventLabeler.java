package nucleus;

import java.util.function.BiFunction;

import nucleus.util.ContractException;

/**
 * A generics-based class that is used to filter event observations.
 * 
 * See {@linkplain EventLabelX} for details.
 * 
 * @author Shawn Hatch
 *
 * @param <T>
 */
public final class EventLabeler<T extends Event> {

	private static class Data<N extends Event> {
		private Class<N> eventClass;
		private BiFunction<SimulationContext, N, EventLabel<N>> labelFunction;
		private EventLabelerId eventLabelerId;
	}

	/**
	 * Returns an instance of the builder class that will build EventLabelers of
	 * the type specified by the class reference.
	 * 
	 * @throws ContractException
	 * <li>{@linkplain NucleusError#NULL_EVENT_CLASS} if the class reference is null</li>
	 */
	public static <N extends Event> Builder<N> builder(Class<N> classReference) {
		if (classReference == null) {
			throw new ContractException(NucleusError.NULL_EVENT_CLASS);
		}
		return new Builder<N>(classReference);
	}

	public static class Builder<N extends Event> {
		private final Class<N> classReference;

		private Builder(Class<N> classReference) {
			this.classReference = classReference;
		}

		private Data<N> data = new Data<>();

		private void validate() {
			if (data.eventLabelerId == null) {
				throw new ContractException(NucleusError.NULL_LABELER_ID_IN_EVENT_LABELER);
			}
			if (data.labelFunction == null) {
				throw new ContractException(NucleusError.NULL_EVENT_LABEL_FUNCTION);
			}
		}

		/**
		 * Builds the event labeler from the given input
		 * 
		 * @throws ContractException
		 * <li>{@linkplain NucleusError#NULL_LABELER_ID_IN_EVENT_LABELER} if the id is not set</li> 
		 * <li>{@linkplain NucleusError#NULL_EVENT_LABEL_FUNCTION} if the label function </li>
		 * 
		 */
		public EventLabeler<N> build() {
			try {
				validate();
				data.eventClass = classReference;
				return new EventLabeler<>(data);
			} finally {
				data = new Data<>();
			}
		}

		/**
		 * Sets the label function
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain NucleusError#NULL_EVENT_LABEL_FUNCTION} id the label function is null </li>
		 */
		public Builder<N> setLabelFunction(BiFunction<SimulationContext, N, EventLabel<N>> labelFunction) {
			if (labelFunction == null) {
				throw new ContractException(NucleusError.NULL_EVENT_LABEL_FUNCTION);
			}
			data.labelFunction = labelFunction;
			return this;
		}

		/**
		 * Sets the event labeler id
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain NucleusError#NULL_EVENT_LABELER_ID} if the event labeler id is null</li>
		 */
		public Builder<N> setEventLabelerId(EventLabelerId eventLabelerId) {
			if (eventLabelerId == null) {
				throw new ContractException(NucleusError.NULL_EVENT_LABELER_ID);
			}
			data.eventLabelerId = eventLabelerId;
			return this;
		}

	}

	/**
	 * Constructs the event labeler from the given labeler id, event class and
	 * function for producing a label from an event.
	 */
	private final Data<T> data;

	private EventLabeler(Data<T> data) {
		this.data = data;
	}

	/**
	 * Returns the event class of T
	 */
	public final Class<T> getEventClass() {
		return data.eventClass;
	}

	/**
	 * Returns an event label from a given event. This event label must 1)have
	 * the same labeler id as this labeler 2) have the same primary key as the
	 * event and 3) have the same event class as this labeler.
	 */
	public EventLabel<T> getEventLabel(SimulationContext simulationContext, T event) {
		return data.labelFunction.apply(simulationContext, event);
	}

	/**
	 * Returns the unique id of this labeler
	 */
	public EventLabelerId getEventLabelerId() {
		return data.eventLabelerId;
	}

}
