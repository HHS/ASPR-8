package nucleus.eventfiltering;

import java.util.function.Function;

import net.jcip.annotations.NotThreadSafe;
import nucleus.Event;
import nucleus.NucleusError;
import util.errors.ContractException;

@NotThreadSafe
public final class IdentifiedFunction<T extends Event> {

	private static class Data<N extends Event> {

		private Class<N> eventClass;

		private Object eventFunctionId;

		private Function<N, Object> eventFunction;
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

		private void validate() {
			if (data.eventFunctionId == null) {
				throw new ContractException(NucleusError.NULL_EVENT_FUNCTION_ID);
			}

			if (data.eventFunction == null) {
				throw new ContractException(NucleusError.NULL_EVENT_FUNCTION);
			}
		}

		/**
		 * Constructs a new EventLabel.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain NucleusError#NULL_PRIMARY_KEY_VALUE} if
		 *             no keys were added</li>
		 *             <li>{@linkplain NucleusError#NULL_LABELER_ID_IN_EVENT_LABEL}
		 *             if no event labeler was set</li>
		 */
		public IdentifiedFunction<N> build() {
			try {
				
				validate();
				data.eventClass = this.eventClass;
				return new IdentifiedFunction<>(data);
			} finally {
				data = new Data<>();
			}
		}

		/**
		 * Sets the function id
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain NucleusError#NULL_EVENT_FUNCTION_ID} if the
		 *             event function id is null</li>
		 */
		public Builder<N> setFunctionId(Object eventFunctionId) {
			if (eventFunctionId == null) {
				throw new ContractException(NucleusError.NULL_EVENT_FUNCTION_ID);
			}
			data.eventFunctionId = eventFunctionId;
			return this;
		}

		/**
		 * Sets the event labeler for the event label
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain NucleusError#NULL_EVENT_FUNCTION} if
		 *             the event function is null</li>
		 */
		public Builder<N> setEventFunction(Function<N,Object> eventFunction) {
			if (eventFunction == null) {
				throw new ContractException(NucleusError.NULL_EVENT_FUNCTION);
			}
			data.eventFunction = eventFunction;
			return this;
		}

	}

	private final Data<T> data;
	
	private IdentifiedFunction(Data<T> data) {
		this.data = data;		
	}
	
	/**
	 * Returns the event subclass required by the event function
	 */
	public Class<T> getEventClass() {
		return data.eventClass;
	}

	/**
	 * Returns the event function id.
	 */
	public Object getEventFunctionId() {
		return data.eventFunctionId;
	}

	/**
	 * Returns the event function.
	 */
	public Function<T,Object> getEventFuntion() {
		return data.eventFunction;
	}

}
