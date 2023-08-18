package gov.hhs.aspr.ms.gcm.nucleus;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import net.jcip.annotations.Immutable;
import util.errors.ContractException;

@Immutable
public final class IdentifiableFunctionMap<N> {
	private static class Data<T> {
		private Map<Object, IdentifiableFunction<T>> functionMap = new LinkedHashMap<>();

		public Data() {
		}

		public Data(Data<T> data) {
			functionMap.putAll(data.functionMap);
		}
	}

	/**
	 * Returns a builder instance that will build an IdentifiableFunctionMap of the
	 * given type
	 */
	public static <T> Builder<T> builder(Class<T> type) {
		if (type == null) {
			throw new ContractException(NucleusError.NULL_CLASS_REFERENCE);
		}
		return new Builder<>();
	}

	public static class Builder<T> {
		private Data<T> data = new Data<>();

		private Builder() {
		}

		public IdentifiableFunctionMap<T> build() {
			return new IdentifiableFunctionMap<>(new Data<>(data));
		}

		/**
		 * Puts the function at the id, replacing any existing function.
		 * 
		 * @throws util.errors.ContractException
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

			data.functionMap.put(id, new IdentifiableFunction<>(id, function));
			return this;
		}

	}

	/**
	 * Gets the function associated with the given id
	 * 
	 * @throws util.errors.ContractException
	 *                           <ul>
	 *                           <li>{@linkplain NucleusError#NULL_FUNCTION_ID} if
	 *                           the function id is null</li>
	 *                           <li>{@linkplain NucleusError#UNKNOWN_FUNCTION_ID}
	 *                           if the function id is not in this map</li>
	 *                           </ul>
	 */
	public IdentifiableFunction<N> get(Object id) {
		if (id == null) {
			throw new ContractException(NucleusError.NULL_FUNCTION_ID);
		}
		IdentifiableFunction<N> result = data.functionMap.get(id);
		if (result == null) {
			throw new ContractException(NucleusError.UNKNOWN_FUNCTION_ID);
		}
		return result;
	}

	private final Data<N> data;

	private IdentifiableFunctionMap(Data<N> data) {
		this.data = data;
	}

}
