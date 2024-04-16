package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.Immutable;

/**
 * A generics-based data class for collecting an ordered list of predicates of
 * the form F(event) = value that are used in conjunction to filter events.
 */
@Immutable
public final class EventFilter<T extends Event> {

	private static class Data<N extends Event> {

		private Class<N> eventClass;

		private List<Pair<IdentifiableFunction<N>, Object>> functionValuePairs = new ArrayList<>();

		public Data() {

		}

		public Data(Data<N> data) {
			eventClass = data.eventClass;
			functionValuePairs.addAll(data.functionValuePairs);
		}
	}

	/**
	 * Returns a new instance of the Builder class
	 * 
	 * @throws ContractException {@linkplain NucleusError#NULL_EVENT_CLASS } if the
	 *                           class reference is null
	 */
	public static <N extends Event> Builder<N> builder(Class<N> classReference) {
		if (classReference == null) {
			throw new ContractException(NucleusError.NULL_EVENT_CLASS);
		}
		return new Builder<N>(classReference);
	}

	/**
	 * Builder class for EventFilter
	 */
	public static class Builder<N extends Event> {

		private Data<N> data = new Data<>();

		private final Class<N> eventClass;

		private Builder(Class<N> classReference) {
			this.eventClass = classReference;
		}

		/**
		 * Constructs a new EventLabel from the collected data.
		 */
		public EventFilter<N> build() {
			data.eventClass = this.eventClass;
			return new EventFilter<>(new Data<>(data));
		}

		/**
		 * Adds an event function and its associated target value.
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain NucleusError#NULL_IDENTIFIABLE_FUNCTION}
		 *                           if the identifiable function is null</li>
		 *                           <li>{@linkplain NucleusError#NULL_FUNCTION_VALUE}
		 *                           if the target value is null</li>
		 *                           </ul>
		 */
		public Builder<N> addFunctionValuePair(IdentifiableFunction<N> identifiableFunction, Object targetValue) {
			if (identifiableFunction == null) {
				throw new ContractException(NucleusError.NULL_IDENTIFIABLE_FUNCTION);
			}

			if (targetValue == null) {
				throw new ContractException(NucleusError.NULL_FUNCTION_VALUE);
			}

			data.functionValuePairs.add(new Pair<>(identifiableFunction, targetValue));

			return this;
		}
	}

	private final Data<T> data;

	private EventFilter(Data<T> data) {
		this.data = data;
	}

	/**
	 * Returns the event subclass associated with all function values
	 */
	public Class<T> getEventClass() {
		return data.eventClass;
	}

	/**
	 * Returns an unmodifiable list of the function values
	 */
	public List<Pair<IdentifiableFunction<T>, Object>> getFunctionValuePairs() {
		return Collections.unmodifiableList(data.functionValuePairs);
	}

}
