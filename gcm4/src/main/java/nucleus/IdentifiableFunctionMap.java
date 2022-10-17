package nucleus;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import net.jcip.annotations.Immutable;
import util.errors.ContractException;

@Immutable
public final class IdentifiableFunctionMap<N> {
	private static class Data<T> {
		private Map<Object, IdentifiableFunction<T>> functionMap = new LinkedHashMap<>();
	}

	public static <T> Builder<T> builder(Class<T> c) {
		return new Builder<>();
	}

	public static class Builder<T> {
		private Data<T> data = new Data<>();

		private Builder() {
		}

		public IdentifiableFunctionMap<T> build() {
			try {
				return new IdentifiableFunctionMap<>(data);
			} finally {
				data = new Data<>();
			}

		}

		public Builder<T> put(Object id, Function<T, Object> eventFunction) {
			if(id == null) {
				throw new ContractException(NucleusError.NULL_EVENT_FUNCTION_ID);
			}
			
			if(eventFunction == null) {
				throw new ContractException(NucleusError.NULL_EVENT_FUNCTION);
			}
			
			data.functionMap.put(id, new IdentifiableFunction<>(id, eventFunction));
			return this;
		}

	}

	public IdentifiableFunction<N> get(Object id) {
		if (id == null) {
			throw new ContractException(NucleusError.NULL_EVENT_FUNCTION_ID);
		}
		IdentifiableFunction<N> result = data.functionMap.get(id);
		if (result == null) {
			throw new ContractException(NucleusError.UNKNOWN_EVENT_FUNCTION_ID);
		}
		return result;
	}

	private final Data<N> data;

	private IdentifiableFunctionMap(Data<N> data) {
		this.data = data;
	}

}
