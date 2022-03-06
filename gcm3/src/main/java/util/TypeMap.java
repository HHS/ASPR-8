package util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class TypeMap<K> {
	public static enum TypeMapError implements ContractError {

		NULL_INSTANCE("A null instance was added to the type map"),;

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

	private Map<Class<?>, K> baseMap = new LinkedHashMap<>();

	private Map<Class<?>, K> workingMap = new LinkedHashMap<>();

	/**
	 * Returns the optional contain the instance stored for the given class
	 * 
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <T extends K> Optional<T> get(Class<T> classRef) {

		K k = workingMap.get(classRef);
		if (k == null) {
			List<Class<?>> candidates = new ArrayList<>();
			for (Class<?> c : baseMap.keySet()) {
				if (classRef.isAssignableFrom(c)) {
					candidates.add(c);
				}
			}
			if (candidates.size() > 1) {
				throw new RuntimeException("Class reference matches multiple values");
			}
			if (candidates.size() == 1) {
				k = baseMap.get(candidates.get(0));
				workingMap.put(classRef, k);
			}
		}
		return Optional.ofNullable((T) k);
	}

	/**
	 * Returns a typed Builder instance for TypeMap
	 */
	public static <N> Builder<N> builder(Class<N> n) {
		return new Builder<N>();
	}

	/**
	 * A type builder class for TypeMap
	 * 
	 * @author Shawn Hatch
	 *
	 */
	public static class Builder<N> {
		private Builder() {
		}

		private Map<Class<?>, N> map = new LinkedHashMap<>();

		/**
		 * Returns the TypeMap instance composed from the inputs to this
		 * builder.
		 */
		public TypeMap<N> build() {
			try {
				TypeMap<N> result = new TypeMap<>();
				result.baseMap = map;
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

	public Set<K> getContents() {
		Set<K> result = new LinkedHashSet<>();
		for (K value : baseMap.values()) {
			result.add(value);
		}
		return result;
	}

}
