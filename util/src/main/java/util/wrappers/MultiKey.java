package util.wrappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.jcip.annotations.NotThreadSafe;

/**
 * A utility class that allows an ordered set of objects to be used as a key.
 *
 *
 */
@NotThreadSafe
public final class MultiKey {
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * A convenience builder class for MultiKey for situations where it is not
	 * practical to use the ellipsis based constructor.
	 *
	 */
	public static class Builder {

		private Builder() {
		}

		private List<Object> keys = new ArrayList<>();

		public Builder addKey(final Object key) {
			keys.add(key);
			return this;
		}

		public MultiKey build() {

			try {
				final MultiKey result = new MultiKey();
				result.objects = keys.toArray();
				return result;
			} finally {
				keys = new ArrayList<>();
			}
		}
	}

	private Object[] objects;

	private int hashCode;

	private boolean hashCodeDerived;

	/**
	 * Create a MultiKey by providing a sequence of Objects.
	 *
	 * @param objects
	 */
	public MultiKey(final Object... objects) {
		this.objects = Arrays.copyOf(objects, objects.length);
	}

	private void deriveHashCode() {
		hashCode = Arrays.hashCode(objects);
		hashCodeDerived = true;
	}

	/**
	 * Two MultiKeys are equal if and only if their keys are equal.
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof MultiKey)) {
			return false;
		}
		final MultiKey other = (MultiKey) obj;
		return Arrays.equals(objects, other.objects);
	}

	/**
	 * Returns the indexed key from the keys provided during construction.
	 *
	 * @throws ArrayIndexOutOfBoundsException
	 *             if the index is non-negative or greater than or equal to the
	 *             number of keys used to create this MultiKey.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getKey(final int index) {
		return (T) objects[index];
	}

	/**
	 * Returns the keys that form this Multikey as an Object array.
	 *
	 */
	public Object[] getKeys() {
		return Arrays.copyOf(objects, objects.length);
	}

	/**
	 * Returns the hasCode for this MultiKey. Returns the value given by
	 * Arrays.hashCode(this.getKeys()).
	 */
	@Override
	public int hashCode() {
		if (!hashCodeDerived) {
			deriveHashCode();
		}
		return hashCode;
	}

	/**
	 * Returns the number of keys used to create this MultiKey
	 */
	public int size() {
		return objects.length;
	}

	/**
	 * Alternative to toString() that is equivalent to
	 * Arrays.toString((this.getKeys()).
	 */
	public String toKeyString() {
		return Arrays.toString(objects);
	}

	public String toTabString() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Object object : objects) {
			if (first) {
				first = false;
			} else {
				sb.append("\t");
			}
			sb.append(object);
		}
		return sb.toString();
	}

	/**
	 * Returns a standard string for MultiKey that lists each key to string
	 * separated by commas.
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("MultiKey [objects=");
		builder.append(Arrays.toString(objects));
		builder.append("]");
		return builder.toString();
	}

}
