package util;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * 
 * 
 * @author Shawn Hatch
 *
 */
public class PredicateManager {

	private class MetaPredicate<T> {

		private final Predicate<T> predicate;

		public MetaPredicate(Predicate<T> predicate) {
			this.predicate = predicate;
		}

		@SuppressWarnings("unchecked")
		public boolean test(Object input) {
			return predicate.test((T) input);
		}
	}

	private Map<Class<?>, MetaPredicate<?>> predicateMap = new LinkedHashMap<>();

	public <T> void setPredicate(Class<T> eventClass, Predicate<T> predicate) {
		MetaPredicate<T> metaPredicate = new MetaPredicate<>(predicate);
		predicateMap.put(eventClass, metaPredicate);
	}

	private String getClassName(Object input) {
		if (input == null) {
			return "null";
		}
		return input.getClass().getCanonicalName();
	}

	public boolean test(Object input) {
		if (input == null) {
			throw new RuntimeException("null input");
		}
		MetaPredicate<?> metaPredicate = predicateMap.get(input.getClass());
		if (metaPredicate == null) {
			Set<Class<?>> matchingKeys = new LinkedHashSet<>();
			for (Class<?> x : predicateMap.keySet()) {
				if (x.isAssignableFrom(input.getClass())) {
					matchingKeys.add(x);
				}
			}

			switch (matchingKeys.size()) {
			case 0:
				throw new RuntimeException("no predicate for " + getClassName(input));
			case 1:
				metaPredicate = predicateMap.get(matchingKeys.iterator().next());
				predicateMap.put(input.getClass(), metaPredicate);
				break;
			default:
				throw new RuntimeException("multiple predicates for " + getClassName(input));
			}
		}

		return metaPredicate.test(input);

	}

	@SuppressWarnings("unchecked")
	public <T> Set<Class<? extends T>> getHandledTypes(Class<T> t) {
		Set<Class<? extends T>> result = new LinkedHashSet<>();
		for (Class<?> x : predicateMap.keySet()) {
			if (t.isAssignableFrom(x)) {
				result.add((Class<T>) x);
			}
		}
		return result;
	}

}
