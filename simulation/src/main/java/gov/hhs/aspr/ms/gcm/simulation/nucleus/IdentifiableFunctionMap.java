package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.Immutable;

@Immutable
public final class IdentifiableFunctionMap<T> {
	private static class Data<N> {
		private Map<Object, IdentifiableFunction<N>> functionMap = new LinkedHashMap<>();
		private boolean locked;

		private Data() {
		}

		private Data(Data<N> data) {
			functionMap.putAll(data.functionMap);
			locked = data.locked;
		}
	}

	/**
	 * Returns a builder instance that will build an IdentifiableFunctionMap of the
	 * given type
	 */
	public static <K> Builder<K> builder(Class<K> type) {
		if (type == null) {
			throw new ContractException(NucleusError.NULL_CLASS_REFERENCE);
		}
		return new Builder<>(new Data<>());
	}

	public static class Builder<T> {
		private Data<T> data;

		private Builder(Data<T> data) {
			this.data = data;
		}

		public IdentifiableFunctionMap<T> build() {
			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new IdentifiableFunctionMap<>(data);
		}

		/**
		 * Puts the function at the id, replacing any existing function.
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain NucleusError#NULL_FUNCTION_ID} if
		 *                           the function id is null</li>
		 *                           <li>{@linkplain NucleusError#NULL_FUNCTION} if the
		 *                           function is null</li>
		 *                           </ul>
		 */
		public Builder<T> put(Object id, Function<T, Object> function) {
			if (id == null) {
				throw new ContractException(NucleusError.NULL_FUNCTION_ID);
			}

			if (function == null) {
				throw new ContractException(NucleusError.NULL_FUNCTION);
			}
			ensureDataMutability();
			data.functionMap.put(id, new IdentifiableFunction<>(id, function));
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

	/**
	 * Gets the function associated with the given id
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain NucleusError#NULL_FUNCTION_ID} if
	 *                           the function id is null</li>
	 *                           <li>{@linkplain NucleusError#UNKNOWN_FUNCTION_ID}
	 *                           if the function id is not in this map</li>
	 *                           </ul>
	 */
	public IdentifiableFunction<T> get(Object id) {
		if (id == null) {
			throw new ContractException(NucleusError.NULL_FUNCTION_ID);
		}
		IdentifiableFunction<T> result = data.functionMap.get(id);
		if (result == null) {
			throw new ContractException(NucleusError.UNKNOWN_FUNCTION_ID);
		}
		return result;
	}

	private final Data<T> data;

	private IdentifiableFunctionMap(Data<T> data) {
		this.data = data;
	}

	/**
	 * Returns a new builder instance that is pre-filled with the current state of
	 * this instance.
	 */
	public Builder<T> toBuilder() {
		return new Builder<>(data);
	}

}
