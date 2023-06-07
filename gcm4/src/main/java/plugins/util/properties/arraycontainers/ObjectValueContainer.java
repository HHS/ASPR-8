package plugins.util.properties.arraycontainers;

import java.util.Arrays;

/**
 * An array-based container for Objects that associates non-negative int indices
 * with Objects and returns a default value when no value has been previously
 * set for a particular index. This allows this container to return the default
 * value for indices outside of the range of the internal array.
 * 
 *
 */
public final class ObjectValueContainer {

	private Object[] elements;

	private final Object defaultValue;

	/**
	 * Constructs a new ObjectValueContainer with the given default value and
	 * initial capacity. The default value may be null.
	 * 
	 * @throws IllegalArgumentException
	 *             <li>if capacity is negative
	 */
	public ObjectValueContainer(Object defaultValue, int capacity) {
		if (capacity < 0) {
			throw new IllegalArgumentException("negative capacity: " + capacity);
		}
		elements = new Object[capacity];
		this.defaultValue = defaultValue;
		if (defaultValue != null) {
			for (int i = 0; i < capacity; i++) {
				elements[i] = defaultValue;
			}
		}
	}

	/**
	 * Sets the value at the index.
	 * 
	 * @throws IllegalArgumentException
	 *             if the index is negative
	 * 
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
		if(capacity>elements.length) {
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
	 * Returns the Object value associated with the given index. If no object
	 * value has been associated with the index, returns the default value.
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ObjectValueContainer [elements=");
		builder.append(Arrays.toString(elements));
		builder.append(", defaultValue=");
		builder.append(defaultValue);
		builder.append("]");
		return builder.toString();
	}
	
	
}