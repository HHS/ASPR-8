package plugins.util.properties.arraycontainers;

import java.util.Arrays;

import plugins.util.properties.PropertyError;
import util.errors.ContractException;

/**
 * A container that maps non-negative int index values to doubles by storing
 * each double in an array. Returns a default double value for every
 * non-negative int index value until the value is explicitly set by an
 * invocation to the set() method.
 * 
 *
 */
public final class DoubleValueContainer {
	
	private int highestNonUllageIndex = -1;

	/*
	 * The array for storing the values
	 */
	private double[] values;
	

	/*
	 * The value returned for any non-negative index that has not been set via
	 * an invocation of setValue().
	 */
	private double defaultValue;

	/*
	 * Grows the length of the values array to be the greater of the given
	 * capacity and 125% of its current length, filling empty elements in the
	 * array with the default value.
	 */
	private void grow(int capacity) {
		int oldCapacity = values.length;
		int newCapacity = Math.max(capacity, values.length + (values.length >> 2));
		values = Arrays.copyOf(values, newCapacity);
		if (defaultValue != 0) {
			for (int i = oldCapacity; i < newCapacity; i++) {
				values[i] = defaultValue;
			}
		}
	}

	/**
	 * Returns the value at index
	 * 
	 * @param index
	 * @return
	 * @throws ContractException
	 *             <li>{@linkplain PropertyError#NEGATIVE_INDEX} if index is negative</li>
	 * 
	 */
	public double getValue(int index) {
		double result;

		if(index<0) {
			throw new ContractException(PropertyError.NEGATIVE_INDEX);
		}
		
		if (index < values.length) {
			result = values[index];
		} else {
			result = defaultValue;
		}

		return result;
	}
	

	/**
	 * Sets the capacity to the given capacity if the current capacity is less
	 * than the one given.
	 */
	public void setCapacity(int capacity) {
		if (capacity > values.length) {
			grow(capacity);
		}
	}

	/**
	 * Returns the capacity of this container. Capacity is guaranteed to be
	 * greater than or equal to size.
	 */
	public int getCapacity() {
		return values.length;
	}

	/**
	 * Returns the default value
	 * 
	 */
	public double getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Constructs the DoubleValueContainer with the given default value.
	 * 
	 * @param defaultValue
	 */
	public DoubleValueContainer(double defaultValue) {
		this(defaultValue, 16);
	}

	/**
	 * Constructs the DoubleValueContainer with the given default value and
	 * initial capacity
	 * 
	 * @param defaultValue
	 * @param capacity
	 * 
	 * @throws NegativeArraySizeException
	 *             if the capacity is negative
	 */
	public DoubleValueContainer(double defaultValue, int capacity) {
		values = new double[capacity];
		if (defaultValue != 0) {
			for (int i = 0; i < capacity; i++) {
				values[i] = defaultValue;
			}
		}
		this.defaultValue = defaultValue;

	}

	/**
	 * Sets the value at the index to the given value
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PropertyError#NEGATIVE_INDEX} if index is
	 *             negative</li>
	 */
	public void setValue(int index, double value) {
		if (index < 0) {
			throw new ContractException(PropertyError.NEGATIVE_INDEX);
		}
		if (index >= values.length) {
			grow(index + 1);
		}	
		if(index> highestNonUllageIndex) {
			highestNonUllageIndex = index;
		}
		values[index] = value;
	}
	
	private String getElementsString() {

		if (highestNonUllageIndex == -1) {
			return "[]";
		}

		StringBuilder b = new StringBuilder();
		b.append('[');
		for (int i = 0;; i++) {
			b.append(String.valueOf(values[i]));
			if (i == highestNonUllageIndex) {
				return b.append(']').toString();
			}
			b.append(", ");
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DoubleValueContainer [values=");
		builder.append(getElementsString());
		builder.append(", defaultValue=");
		builder.append(defaultValue);
		builder.append("]");
		return builder.toString();
	}
	
	

}
