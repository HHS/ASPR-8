package util.objectrepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A mutable container for objects that supports retrieval of contents via class
 * and interface types.
 * 
 * @author Shawn Hatch
 * 
 */
public final class MutableObjectRepository {

	private Map<Class<?>, Set<Object>> map = new LinkedHashMap<>();

	private Map<Class<?>, Set<Class<?>>> typeMap = new LinkedHashMap<>();

	/**
	 * Adds an object to the repository. Add replaces any current object stored.
	 * Formally if Object A is currently in the container and Object B is added
	 * to the container with B.equals(A) returning true, then Object A is
	 * removed from the container.
	 * 
	 * @param obj
	 */
	public void add(Object obj) {
		if (obj == null) {
			return;
		}
		Class<?> x = obj.getClass();
		Set<Class<?>> classSet = typeMap.get(x);
		if (classSet == null) {
			classSet = getClasses(x);
			for (Class<?> c : classSet) {
				Set<Class<?>> classSet2 = typeMap.get(c);
				if (classSet2 == null) {
					classSet2 = new LinkedHashSet<>();
					typeMap.put(c, classSet2);
				}
				classSet2.add(x);
			}
		}

		Set<Object> set = map.get(x);
		if (set == null) {
			set = new LinkedHashSet<>();
			map.put(x, set);
		}
		set.add(obj);
	}

	/**
	 * Adds all the objects in the collection to the repository. Replacement of
	 * equal objects is enforced as in the add() method.
	 * 
	 * @param list
	 */
	public void addAll(Collection<?> collection) {
		for (Object obj : collection) {
			add(obj);
		}
	}

	/**
	 * Returns true if and only if the object is in the container. Containment
	 * is determined by the equals contract of Object.
	 * 
	 * @param obj
	 * @return
	 */
	public boolean contains(Object obj) {
		Set<Object> objects = map.get(obj.getClass());
		if (objects == null) {
			return false;
		}
		return objects.contains(obj);
	}

	/**
	 * Returns all stored objects in the repository that are either of the same
	 * class or subclass of the specified Class parameter, or are implementing
	 * classes of the interface represented by the specified Class parameter.
	 * Returns an empty set when there are no such objects stored in the
	 * container,
	 * 
	 * @param <T>
	 * @param c
	 * @return List<T>
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getMembers(Class<T> c) {
		List<T> result = new ArrayList<>();
		Set<Class<?>> classSet = typeMap.get(c);
		if (classSet == null) {
			return result;
		}
		for (Class<?> c2 : classSet) {
			Set<? extends T> set = (Set<? extends T>) map.get(c2);
			result.addAll(set);
		}
		return result;
	}

	/**
	 * Returns the single member matching the given class reference.
	 * 
	 * @throws RuntimeException
	 *             if there is more than one member matching the class reference
	 * 
	 * 
	 */
	public <T> Optional<T> getMember(Class<T> c) {
		T result = null;
		Set<Class<?>> classSet = typeMap.get(c);
		if (classSet != null) {
			for (Class<?> c2 : classSet) {
				@SuppressWarnings("unchecked")
				Set<? extends T> set = (Set<? extends T>) map.get(c2);
				if (set.size() > 1) {
					throw new RuntimeException("repository contains multiple matches");
				}
				if (set.size() == 1) {
					result = set.iterator().next();
				}
			}
		}
		return Optional.ofNullable(result);
	}

	/**
	 * Removes the specified object from the repository based on equals
	 * comparison.
	 * 
	 * @param obj
	 */
	public void remove(Object obj) {
		Set<Object> set = map.get(obj.getClass());
		if (set == null) {
			return;
		}
		set.remove(obj);
		if (set.size() == 0) {
			clearTypeMap(obj.getClass());
		}
	}

	/**
	 * Removes all objects in the repository that are either of the same class
	 * or subclass of the specified Class parameter, or are implementing classes
	 * of the interface represented by the specified Class parameter.
	 * 
	 * @param <T>
	 * @param c
	 */
	public <T> void removeAll(Class<T> c) {
		Set<Class<?>> classes = typeMap.get(c);
		if (classes == null) {
			return;
		}
		classes = new LinkedHashSet<>(classes);
		for (Class<?> c2 : classes) {
			clearTypeMap(c2);
		}

	}

	private void clearTypeMap(Class<?> x) {
		map.remove(x);
		Set<Class<?>> classes = getClasses(x);
		for (Class<?> c : classes) {
			Set<Class<?>> classes2 = typeMap.get(c);
			classes2.remove(x);
			if (classes2.size() == 0) {
				typeMap.remove(c);
			}
		}
	}

	private Set<Class<?>> getClasses(Class<?> c) {
		Set<Class<?>> set = new LinkedHashSet<>();
		Class<?> x = c;
		while (x != null) {
			set.add(x);
			getInterfaces(x, set);
			x = x.getSuperclass();
		}
		return set;
	}

	private void getInterfaces(Class<?> c, Set<Class<?>> set) {
		Class<?>[] interfaces = c.getInterfaces();
		for (Class<?> d : interfaces) {
			set.add(d);
			getInterfaces(d, set);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("PostIndexObjectRepository [\n");
		sb.append("map=\n");
		for (Class<?> c : map.keySet()) {
			sb.append("\t");
			sb.append(c.getSimpleName());
			sb.append(":");
			Set<Object> objects = map.get(c);
			boolean useComma = false;
			for (Object object : objects) {
				if (useComma) {
					sb.append(",");
				} else {
					useComma = true;
				}
				sb.append(object.toString());
			}
			sb.append("\n");
		}
		sb.append("typemap=\n");
		for (Class<?> c : typeMap.keySet()) {
			sb.append("\t");
			sb.append(c.getSimpleName());
			sb.append(":");
			Set<Class<?>> classes = typeMap.get(c);
			boolean useComma = false;
			for (Class<?> c2 : classes) {
				if (useComma) {
					sb.append(",");
				} else {
					useComma = true;
				}
				sb.append(c2.getSimpleName());
			}
			sb.append("\n");
		}

		sb.append("]");

		return sb.toString();
	}

}
