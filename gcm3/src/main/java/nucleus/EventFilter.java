package nucleus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

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

	@Immutable
	public final static class FunctionValue<N> {

		private Object functionId;
		private Function<N, Object> eventFunction;
		private Object targetValue;

		private FunctionValue(Object functionId, Function<N, Object> eventFunction, Object targetValue) {			
			this.functionId = functionId;
			this.eventFunction = eventFunction;
			this.targetValue = targetValue;
		}

		public Object getFunctionId() {
			return functionId;
		}

		public Function<N, Object> getEventFunction() {
			return eventFunction;
		}

		public Object getTargetValue() {
			return targetValue;
		}

	}

	private static class Data<N extends Event> {

		private Class<N> eventClass;

		private List<FunctionValue<N>> functionValues = new ArrayList<>();
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
		 *             <li>{@linkplain NucleusError#NULL_EVENT_FUNCTION_ID} if
		 *             the function id is null</li>
		 *             <li>{@linkplain NucleusError#NULL_EVENT_FUNCTION} if the
		 *             event function is null</li>
		 *             <li>{@linkplain NucleusError#NULL_EVENT_FUNCTION_VALUE}
		 *             if the target value is null</li>
		 */
		public Builder<N> addFunctionValue(Object functionId, Function<N, Object> eventFunction, Object targetValue) {

			if (functionId == null) {
				throw new ContractException(NucleusError.NULL_EVENT_FUNCTION_ID);
			}
			if (eventFunction == null) {
				throw new ContractException(NucleusError.NULL_EVENT_FUNCTION);
			}
			if (targetValue == null) {
				throw new ContractException(NucleusError.NULL_EVENT_FUNCTION_VALUE);
			}

			data.functionValues.add(new FunctionValue<>(functionId, eventFunction, targetValue));
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
	public List<FunctionValue<T>> getFunctionValues() {
		return Collections.unmodifiableList(data.functionValues);
	}

}
