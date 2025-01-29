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

	private static class Data<T extends Event> {

		private Class<T> eventClass;

		private List<Pair<IdentifiableFunction<T>, Object>> functionValuePairs = new ArrayList<>();

		private boolean locked;

		private Data() {
		}

		private Data(Data<T> data) {
			eventClass = data.eventClass;
			functionValuePairs.addAll(data.functionValuePairs);
			locked = data.locked;
		}
	}

	/**
	 * Returns a new instance of the Builder class
	 * 
	 * @throws ContractException {@linkplain NucleusError#NULL_EVENT_CLASS } if the
	 *                           class reference is null
	 */
	public static <T extends Event> Builder<T> builder(Class<T> classReference) {
		if (classReference == null) {
			throw new ContractException(NucleusError.NULL_EVENT_CLASS);
		}
		return new Builder<>(classReference);
	}

	/**
	 * Builder class for EventFilter
	 */
	public static class Builder<T extends Event> {

		private Data<T> data;

		private Builder(Data<T> data) {
			this.data = data;
		}

		private Builder(Class<T> classReference) {
			this.data = new Data<>();
			this.data.eventClass = classReference;
		}

		/**
		 * Constructs a new EventLabel from the collected data.
		 */
		public EventFilter<T> build() {
			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new EventFilter<>(data);
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
		public Builder<T> addFunctionValuePair(IdentifiableFunction<T> identifiableFunction, Object targetValue) {
			if (identifiableFunction == null) {
				throw new ContractException(NucleusError.NULL_IDENTIFIABLE_FUNCTION);
			}

			if (targetValue == null) {
				throw new ContractException(NucleusError.NULL_FUNCTION_VALUE);
			}
			ensureDataMutability();
			data.functionValuePairs.add(new Pair<>(identifiableFunction, targetValue));

			return this;
		}

		private void ensureDataMutability() {
			if (data.locked) {
				data = new Data<T>(data);
				data.locked = false;
			}
		}

		private void ensureImmutability() {
			if (!data.locked) {
				data.locked = true;
			}
		}

		private void validateData() {
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

	public Builder<T> toBuilder() {
		return new Builder<>(data);
	}

}
