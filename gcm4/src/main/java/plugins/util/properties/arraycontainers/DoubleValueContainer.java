package plugins.util.properties.arraycontainers;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Supplier;

import plugins.util.properties.PropertyError;
import util.errors.ContractException;

/**
 * A container that maps non-negative int index values to doubles by storing
 * each double in an array. Returns a default double value for every
 * non-negative int index value until the value is explicitly set by an
 * invocation to the set() method.
 */
public final class DoubleValueContainer {

	/*
	 * The array for storing the values
	 */
	private double[] values;

	private final Supplier<Iterator<Integer>> indexIteratorSupplier;

	/*
	 * The value returned for any non-negative index that has not been set via an
	 * invocation of setValue().
	 */
	private double defaultValue;

	/*
	 * Grows the length of the values array to be the greater of the given capacity
	 * and 125% of its current length, filling empty elements in the array with the
	 * default value.
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
	 *                           <li>{@linkplain PropertyError#NEGATIVE_INDEX} if
	 *                           index is negative</li>
	 */
	public double getValue(int index) {
		double result;

		if (index < 0) {
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
	 * Sets the capacity to the given capacity if the current capacity is less than
	 * the one given.
	 */
	public void setCapacity(int capacity) {
		if (capacity > values.length) {
			grow(capacity);
		}
	}

	/**
	 * Returns the capacity of this container. Capacity is guaranteed to be greater
	 * than or equal to size.
	 */
	public int getCapacity() {
		return values.length;
	}

	/**
	 * Returns the default value
	 */
	public double getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Constructs the DoubleValueContainer with the given default value and initial
	 * capacity
	 * 
	 * @param defaultValue
	 * @param capacity
	 * @throws NegativeArraySizeException if the capacity is negative
	 */
	public DoubleValueContainer(double defaultValue, Supplier<Iterator<Integer>> indexIteratorSupplier) {
		values = new double[0];
		this.defaultValue = defaultValue;
		this.indexIteratorSupplier = indexIteratorSupplier;
	}

	/**
	 * Sets the value at the index to the given value
	 * 
	 * @throws ContractException
	 *                           <li>{@linkplain PropertyError#NEGATIVE_INDEX} if
	 *                           index is negative</li>
	 */
	public void setValue(int index, double value) {
		if (index < 0) {
			throw new ContractException(PropertyError.NEGATIVE_INDEX);
		}
		if (index >= values.length) {
			grow(index + 1);
		}

		values[index] = value;
	}

	private String getElementsString() {
		Iterator<Integer> iterator = indexIteratorSupplier.get();
		StringBuilder sb = new StringBuilder();

		boolean first = true;
		sb.append('[');

		while (iterator.hasNext()) {
			double value = getValue(iterator.next());

			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(value);

		}
		sb.append(']');
		return sb.toString();
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
