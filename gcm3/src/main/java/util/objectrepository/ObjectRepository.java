package util.objectrepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A container for objects that supports retrieval of contents via class and
 * interface types.
 * 
 * @author Shawn Hatch
 * 
 */
public final class ObjectRepository {

	private static class Scaffold {
		private List<Object> objects = new ArrayList<>();
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Scaffold scaffold = new Scaffold();

		private Builder() {

		}

		public Builder add(Object object) {
			if (object != null) {
				scaffold.objects.add(object);
			}
			return this;
		}

		public ObjectRepository build() {
			try {
				return new ObjectRepository(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}
	}

	private final MutableObjectRepository mutableObjectRepository = new MutableObjectRepository();

	private ObjectRepository(Scaffold scaffold) {
		for (Object object : scaffold.objects) {
			mutableObjectRepository.add(object);
		}
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
		return mutableObjectRepository.getMember(c);
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
	public <T> List<T> getMembers(Class<T> c) {
		return mutableObjectRepository.getMembers(c);
	}

	/**
	 * Returns true if and only if the object is in the container. Containment
	 * is determined by the equals contract of Object.
	 * 
	 * @param obj
	 * @return
	 */
	public boolean contains(Object obj) {
		return mutableObjectRepository.contains(obj);
	}

}
