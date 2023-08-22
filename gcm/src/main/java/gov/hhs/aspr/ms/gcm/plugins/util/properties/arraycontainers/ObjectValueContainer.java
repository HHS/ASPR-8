package gov.hhs.aspr.ms.gcm.plugins.util.properties.arraycontainers;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Supplier;

/**
 * An array-based container for Objects that associates non-negative int indices
 * with Objects and returns a default value when no value has been previously
 * set for a particular index. This allows this container to return the default
 * value for indices outside of the range of the internal array.
 */
public final class ObjectValueContainer {

	private Object[] elements;

	private final Object defaultValue;

	private final Supplier<Iterator<Integer>> indexIteratorSupplier;

	/**
	 * Constructs a new ObjectValueContainer with the given default value and
	 * initial capacity. The default value may be null.
	 * 
	 * @throws IllegalArgumentException
	 *                                  <li>if capacity is negative</li>
	 *                                  </ul>
	 */
	public ObjectValueContainer(Object defaultValue, Supplier<Iterator<Integer>> indexIteratorSupplier) {
		elements = new Object[0];
		this.defaultValue = defaultValue;
		this.indexIteratorSupplier = indexIteratorSupplier;
	}

	/**
	 * Sets the value at the index.
	 * 
	 * @throws IllegalArgumentException if the index is negative
	 */
	public void setValue(int index, Object value) {

		if (index < 0) {
			throw new IllegalArgumentException("negative index: " + index);
		}
		if (index >= elements.length) {
			grow(index + 1);
		}

		elements[index] = value;
	}

	/**
	 * Returns the current capacity of this container
	 */
	public int getCapacity() {
		return elements.length;
	}

	public void setCapacity(int capacity) {
		if (capacity > elements.length) {
			grow(capacity);
		}
	}

	/*
	 * Grows the capacity of the elements array by at least 1/4 or to the new
	 * desired capacity, whichever is larger. The empty indices of the array are
	 * filled with the default value.
	 */
	private void grow(int capacity) {
		int oldCapacity = elements.length;
		int newCapacity = Math.max(capacity, elements.length + (elements.length >> 2));
		elements = Arrays.copyOf(elements, newCapacity);
		if (defaultValue != null) {
			for (int i = oldCapacity; i < newCapacity; i++) {
				elements[i] = defaultValue;
			}
		}
	}

	/**
	 * Returns the Object value associated with the given index. If no object value
	 * has been associated with the index, returns the default value.
	 * 
	 * @param index
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getValue(int index) {
		if (index < 0) {
			throw new IllegalArgumentException("negative index: " + index);
		}
		if (index >= elements.length) {
			return (T) defaultValue;
		}
		return (T) elements[index];
	}

	private String getElementsString() {

		Iterator<Integer> iterator = indexIteratorSupplier.get();

		StringBuilder sb = new StringBuilder();
		sb.append('[');
		boolean first = true;
		while (iterator.hasNext()) {
			Integer index = iterator.next();
			Object value = getValue(index);
			if (value == null) {
				continue;
			}
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(index);
			sb.append("=");
			sb.append(value);
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * Returns the string representation of this container. Excludes the ullage
	 * values beyond the highest value that was explicitly set.
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ObjectValueContainer [elements=");
		builder.append(getElementsString());
		builder.append(", defaultValue=");
		builder.append(defaultValue);
		builder.append("]");
		return builder.toString();
	}

}