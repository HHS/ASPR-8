package nucleus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.math3.util.Pair;

import net.jcip.annotations.Immutable;
import util.errors.ContractException;

/**
 * 
 * A generics-based data class for collecting an ordered list of predicates of
 * the form F(event) = value that are used in conjunction to filter events.
 * 
 * 
 * @author Shawn Hatch
 *
 * 
 */
@Immutable
public final class EventFilter<T extends Event> {

	public final static class IdentifiableFunction<N> {
		
		private Object functionId;
		
		private Function<N, Object> eventFunction;

		
		public IdentifiableFunction(Object functionId, Function<N, Object> eventFunction) {
			if (functionId == null) {
				throw new ContractException(NucleusError.NULL_EVENT_FUNCTION_ID);
			}
			if (eventFunction == null) {
				throw new ContractException(NucleusError.NULL_EVENT_FUNCTION);
			}
			
			this.functionId = functionId;
			this.eventFunction = eventFunction;
		}

		public Function<N, Object> getEventFunction() {
			return eventFunction;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((functionId == null) ? 0 : functionId.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof IdentifiableFunction)) {
				return false;
			}
			IdentifiableFunction<?> other = (IdentifiableFunction<?>) obj;
			if (functionId == null) {
				if (other.functionId != null) {
					return false;
				}
			} else if (!functionId.equals(other.functionId)) {
				return false;
			}
			return true;
		}

	}

	private static class Data<N extends Event> {

		private Class<N> eventClass;

		private List<Pair<IdentifiableFunction<N>,Object>> functionValuePairs = new ArrayList<>();
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

	/**
	 * Builder class for EventFilter
	 * 
	 * @author Shawn Hatch
	 * 
	 */
	public static class Builder<N extends Event> {

		private Data<N> data = new Data<>();

		private final Class<N> eventClass;

		private Builder(Class<N> classReference) {
			this.eventClass = classReference;
		}

		/**
		 * Constructs a new EventLabel from the collected data.
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
		 * Adds an event function and its associated target value.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain NucleusError#NULL_IDENTIFIABLE_FUNCTION} if
		 *             the identifiable function is null</li>
		 *             
		 *             <li>{@linkplain NucleusError#NULL_EVENT_FUNCTION_VALUE}
		 *             if the target value is null</li>
		 */
		public Builder<N> addFunctionValuePair(IdentifiableFunction<N> identifiableFunction, Object targetValue) {
			if (identifiableFunction == null) {
				throw new ContractException(NucleusError.NULL_IDENTIFIABLE_FUNCTION);
			}
			
			if (targetValue == null) {
				throw new ContractException(NucleusError.NULL_EVENT_FUNCTION_VALUE);
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
	public List<Pair<IdentifiableFunction<T>,Object>> getFunctionValuePairs() {
		return Collections.unmodifiableList(data.functionValuePairs);
	}

}
