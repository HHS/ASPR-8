package nucleus.eventfiltering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import nucleus.Event;
import nucleus.NucleusError;
import util.errors.ContractException;

public class EventFilter<T extends Event> {

	private static class Data<N extends Event> {

		private Class<N> eventClass;

		private List<Pair<IdentifiedFunction<N>, Object>> eventFunctions = new ArrayList<>();
	}

	/**
	 * Returns a new instance of the Builder class
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain NucleusError#NULL_EVENT_CLASS } if the class
	 *             reference is null</li>
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
		 * Constructs a new EventLabel.
		 * 
		 */
		public EventFilter<N> build() {
			try {
				data.eventClass = this.eventClass;
				return new EventFilter<>(data);
			} finally {
				data = new Data<>();
			}
		}

		/**
		 * Sets the function id
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain NucleusError#NULL_EVENT_FUNCTION} if the
		 *             event function is null</li>
		 */
		public Builder<N> addFunctionValuePair(IdentifiedFunction<N> eventFunction, Object value) {
			if (eventFunction == null) {
				throw new ContractException(NucleusError.NULL_EVENT_FUNCTION);
			}
			if (value == null) {
				throw new ContractException(NucleusError.NULL_EVENT_FUNCTION_VALUE);
			}

			data.eventFunctions.add(new Pair<>(eventFunction, value));
			return this;
		}
	}

	private final Data<T> data;

	private EventFilter(Data<T> data) {
		this.data = data;
	}

	/**
	 * Returns the event subclass required by the event function
	 */
	public Class<T> getEventClass() {
		return data.eventClass;
	}

	/**
	 * Returns an unmodifiable list of the the event function/value pairs
	 */
	public List<Pair<IdentifiedFunction<T>, Object>> getEventFunctionPairs() {
		return Collections.unmodifiableList(data.eventFunctions);
	}

}
