package util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import util.ContractError;
import util.ContractException;

public final class TypeMap<K> {
	public static enum TypeMapError implements ContractError {

		NULL_INSTANCE("A null instance was added to the type map"),
		;

		private final String description;

		private TypeMapError(final String description) {
			this.description = description;
		}

		@Override
		public String getDescription() {
			return description;
		}
	}
	
	private TypeMap() {
	}

	private Map<Class<?>, K> map = new LinkedHashMap<>();

	/**
	 * Returns the optional contain the instance stored for the given class
	 */
	@SuppressWarnings("unchecked")
	public <T extends K> Optional<T> get(Class<T> c) {
		return Optional.ofNullable((T) map.get(c));
	}

	/**
	 * Returns a typed Builder instance for TypeMap
	 */
	public static <N> Builder<N> builder(Class<N> n) {
		return new Builder<N>();
	}

	/**
	 * A type builder class for TypeMap
	 * @author Shawn Hatch
	 *
	 */
	public static class Builder<N> {
		private Builder() {
		}

		private Map<Class<?>, N> map = new LinkedHashMap<>();

		/**
		 * Returns the TypeMap instance composed from the inputs to this builder.
		 */
		public TypeMap<N> build() {
			try {
				TypeMap<N> result = new TypeMap<>();
				result.map = map;
				return result;
			} finally {
				map = new LinkedHashMap<>();
			}
		}

		/**
		 * @throws ContractException
		 *             <li>{@linkplain TypeMapError#NULL_INSTANCE}</li>
		 * 
		 */
		public <T extends N> Builder<N> add(T t) {
			if (t == null) {
				throw new ContractException(TypeMapError.NULL_INSTANCE);
			}
			map.put(t.getClass(), t);
			return this;
		}
	}

}
