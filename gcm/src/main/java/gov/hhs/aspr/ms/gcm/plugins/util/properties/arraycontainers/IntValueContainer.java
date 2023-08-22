package gov.hhs.aspr.ms.gcm.plugins.util.properties.arraycontainers;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Supplier;

import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyError;
import util.errors.ContractException;

/**
 * A container that maps non-negative int index values to bytes, shorts, ints or
 * longs. Values are stored internally as whatever int-type that logically
 * represents the highest value in this container. For example, if this
 * container contains the values [1,0,14,-20,300] then these would be stored in
 * a short array since 300 exceeds the highest value of byte.
 */
public final class IntValueContainer {

	/**
	 * An enumeration representing the four int-based primitive data types in Java.
	 */
	public static enum IntValueType {

		BYTE(Byte.MIN_VALUE, Byte.MAX_VALUE), //
		SHORT(Short.MIN_VALUE, Short.MAX_VALUE), //
		INT(Integer.MIN_VALUE, Integer.MAX_VALUE), //
		LONG(Long.MIN_VALUE, Long.MAX_VALUE);//

		private final long min;

		private final long max;

		/*
		 * Constructs the IntValueType with its min and max values
		 */
		private IntValueType(long min, long max) {
			this.min = min;
			this.max = max;
		}

		/*
		 * Returns true if and only if this IntValueType represents an array whose min
		 * and max values inclusively bound the given value.
		 */
		private boolean isCompatibleValue(long value) {
			return value >= min && value <= max;
		}

	}

	/*
	 * Rebuilds the subTypeArray to be the most compact representation that is
	 * value-compatible with the given long.
	 */
	private SubTypeArray rebuildSubTypeArray(long value) {
		if (IntValueType.SHORT.isCompatibleValue(value)) {
			return new ShortArray(subTypeArray);
		}
		if (IntValueType.INT.isCompatibleValue(value)) {
			return new IntArray(subTypeArray);
		}
		return new LongArray(subTypeArray);
	}

	/*
	 * Common interface for the four array wrapper classes.
	 */
	private static interface SubTypeArray {
		public Supplier<Iterator<Integer>> getIteratorSupplier();

		public IntValueType getIntValueType();

		public long getDefaultValue();

		public long getValue(int index);

		public void setValue(int index, long value);

		public void setCapacity(int capacity);

		public int getCapacity();

		public String toString();

	}

	/*
	 * SubTypeArray implementor for longs
	 */
	private static class LongArray implements SubTypeArray {
		private long[] values;
		private long defaultValue;
		private final Supplier<Iterator<Integer>> indexIteratorSupplier;

		public LongArray(long defaultValue, Supplier<Iterator<Integer>> indexIteratorSupplier) {
			values = new long[0];
			this.defaultValue = defaultValue;
			this.indexIteratorSupplier = indexIteratorSupplier;
		}

		public LongArray(SubTypeArray subTypeArray) {
			this.defaultValue = subTypeArray.getDefaultValue();
			this.indexIteratorSupplier = subTypeArray.getIteratorSupplier();
			values = new long[subTypeArray.getCapacity()];
			for (int i = 0; i < values.length; i++) {
				values[i] = (int) subTypeArray.getValue(i);
			}
		}

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

		@Override
		public long getValue(int index) {
			return values[index];
		}

		@Override
		public void setValue(int index, long value) {
			if (index >= values.length) {
				grow(index + 1);
			}

			values[index] = value;

		}

		@Override
		public IntValueType getIntValueType() {
			return IntValueType.LONG;
		}

		@Override
		public void setCapacity(int capacity) {
			if (capacity > values.length) {
				grow(capacity);
			}
		}

		@Override
		public long getDefaultValue() {
			return defaultValue;
		}

		@Override
		public int getCapacity() {
			return values.length;
		}

		private String getElementsString() {
			Iterator<Integer> iterator = indexIteratorSupplier.get();

			StringBuilder sb = new StringBuilder();

			boolean first = true;
			sb.append('[');
			int n = values.length;
			while (iterator.hasNext()) {

				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				Integer index = iterator.next();
				sb.append(index);
				sb.append("=");
				if (index < 0 || index >= n) {
					sb.append(defaultValue);
				} else {
					sb.append(values[index]);
				}

			}
			sb.append(']');
			return sb.toString();
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append(getElementsString());
			builder.append(Arrays.toString(values));
			builder.append(", defaultValue=");
			builder.append(defaultValue);
			builder.append("]");
			return builder.toString();
		}

		@Override
		public Supplier<Iterator<Integer>> getIteratorSupplier() {
			return indexIteratorSupplier;
		}
	}

	/*
	 * SubTypeArray implementor for ints
	 */
	private static class IntArray implements SubTypeArray {

		private int[] values;
		private int defaultValue;
		private final Supplier<Iterator<Integer>> indexIteratorSupplier;

		public IntArray(int defaultValue, Supplier<Iterator<Integer>> indexIteratorSupplier) {
			values = new int[0];
			this.defaultValue = defaultValue;
			this.indexIteratorSupplier = indexIteratorSupplier;
		}

		public IntArray(SubTypeArray subTypeArray) {
			this.defaultValue = (int) subTypeArray.getDefaultValue();
			this.indexIteratorSupplier = subTypeArray.getIteratorSupplier();
			values = new int[subTypeArray.getCapacity()];
			for (int i = 0; i < values.length; i++) {
				values[i] = (int) subTypeArray.getValue(i);
			}
		}

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

		@Override
		public long getValue(int index) {
			return values[index];
		}

		@Override
		public void setValue(int index, long value) {
			if (index >= values.length) {
				grow(index + 1);
			}

			values[index] = (int) value;
		}

		@Override
		public IntValueType getIntValueType() {
			return IntValueType.INT;
		}

		@Override
		public void setCapacity(int capacity) {
			if (capacity > values.length) {
				grow(capacity);
			}
		}

		@Override
		public long getDefaultValue() {
			return defaultValue;
		}

		@Override
		public int getCapacity() {
			return values.length;
		}

		private String getElementsString() {
			Iterator<Integer> iterator = indexIteratorSupplier.get();

			StringBuilder sb = new StringBuilder();

			boolean first = true;
			sb.append('[');
			int n = values.length;
			while (iterator.hasNext()) {

				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				Integer index = iterator.next();
				sb.append(index);
				sb.append("=");
				if (index < 0 || index >= n) {
					sb.append(defaultValue);
				} else {
					sb.append(values[index]);
				}

			}
			sb.append(']');
			return sb.toString();
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("IntArray [values=");
			builder.append(getElementsString());
			builder.append(", defaultValue=");
			builder.append(defaultValue);
			builder.append("]");
			return builder.toString();
		}

		@Override
		public Supplier<Iterator<Integer>> getIteratorSupplier() {
			return indexIteratorSupplier;
		}

	}

	/*
	 * SubTypeArray implementor for shorts
	 */
	private static class ShortArray implements SubTypeArray {

		private short[] values;
		private short defaultValue;
		private final Supplier<Iterator<Integer>> indexIteratorSupplier;

		public ShortArray(short defaultValue, Supplier<Iterator<Integer>> indexIteratorSupplier) {
			values = new short[0];
			this.defaultValue = defaultValue;
			this.indexIteratorSupplier = indexIteratorSupplier;
		}

		public ShortArray(SubTypeArray subTypeArray) {
			this.defaultValue = (short) subTypeArray.getDefaultValue();
			this.indexIteratorSupplier = subTypeArray.getIteratorSupplier();

			values = new short[subTypeArray.getCapacity()];
			for (int i = 0; i < values.length; i++) {
				values[i] = (short) subTypeArray.getValue(i);
			}
		}

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

		@Override
		public long getValue(int index) {
			return values[index];
		}

		@Override
		public void setValue(int index, long value) {
			if (index >= values.length) {
				grow(index + 1);
			}

			values[index] = (short) value;
		}

		@Override
		public IntValueType getIntValueType() {
			return IntValueType.SHORT;
		}

		@Override
		public void setCapacity(int capacity) {
			if (capacity > values.length) {
				grow(capacity);
			}
		}

		@Override
		public long getDefaultValue() {
			return defaultValue;
		}

		@Override
		public int getCapacity() {
			return values.length;
		}

		private String getElementsString() {
			Iterator<Integer> iterator = indexIteratorSupplier.get();

			StringBuilder sb = new StringBuilder();

			boolean first = true;
			sb.append('[');
			int n = values.length;
			while (iterator.hasNext()) {

				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}

				Integer index = iterator.next();
				sb.append(index);
				sb.append("=");
				if (index < 0 || index >= n) {
					sb.append(defaultValue);
				} else {
					sb.append(values[index]);
				}

			}
			sb.append(']');
			return sb.toString();
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("ShortArray [values=");
			builder.append(getElementsString());
			builder.append(", defaultValue=");
			builder.append(defaultValue);
			builder.append("]");
			return builder.toString();
		}

		@Override
		public Supplier<Iterator<Integer>> getIteratorSupplier() {
			return indexIteratorSupplier;
		}

	}

	/*
	 * SubTypeArray implementor for bytes
	 */
	private static class ByteArray implements SubTypeArray {

		private byte[] values;
		private byte defaultValue;
		private final Supplier<Iterator<Integer>> indexIteratorSupplier;

		public ByteArray(byte defaultValue, Supplier<Iterator<Integer>> indexIteratorSupplier) {
			values = new byte[0];
			this.defaultValue = defaultValue;
			this.indexIteratorSupplier = indexIteratorSupplier;
		}

		@Override
		public long getValue(int index) {
			return values[index];
		}

		@Override
		public void setValue(int index, long value) {
			if (index >= values.length) {
				grow(index + 1);
			}
			values[index] = (byte) value;
		}

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

		@Override
		public IntValueType getIntValueType() {
			return IntValueType.BYTE;
		}

		@Override
		public void setCapacity(int capacity) {
			if (capacity > values.length) {
				grow(capacity);
			}
		}

		@Override
		public long getDefaultValue() {
			return defaultValue;
		}

		@Override
		public int getCapacity() {
			return values.length;
		}

		private String getElementsString() {

			Iterator<Integer> iterator = indexIteratorSupplier.get();

			StringBuilder sb = new StringBuilder();

			boolean first = true;
			sb.append('[');
			int n = values.length;
			while (iterator.hasNext()) {
				Integer index = iterator.next();
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(index);
				sb.append("=");
				if (index < 0 || index >= n) {
					sb.append(defaultValue);
				} else {
					sb.append(values[index]);
				}
			}
			sb.append(']');
			return sb.toString();
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("ByteArray [values=");
			builder.append(getElementsString());
			builder.append(", defaultValue=");
			builder.append(defaultValue);
			builder.append("]");
			return builder.toString();
		}

		@Override
		public Supplier<Iterator<Integer>> getIteratorSupplier() {
			return indexIteratorSupplier;
		}

	}

	/*
	 * The array holding instance.
	 */
	private SubTypeArray subTypeArray;

	/**
	 * Returns the default value as a byte.
	 * 
	 * @throws RuntimeException if the default value is not compatible with byte
	 */
	public byte getDefaultValueAsByte() {
		long result = subTypeArray.getDefaultValue();
		if (!IntValueType.BYTE.isCompatibleValue(result)) {
			throw new RuntimeException("incompatible value found " + result);
		}
		return (byte) result;
	}

	/**
	 * Returns the default value as a short.
	 * 
	 * @throws RuntimeException if the default value is not compatible with short
	 */
	public short getDefaultValueAsShort() {
		long result = subTypeArray.getDefaultValue();
		if (!IntValueType.SHORT.isCompatibleValue(result)) {
			throw new RuntimeException("incompatible value found " + result);
		}
		return (short) result;
	}

	/**
	 * Returns the default value as an int.
	 * 
	 * @throws RuntimeException if the default value is not compatible with int
	 */
	public int getDefaultValueAsInt() {
		long result = subTypeArray.getDefaultValue();
		if (!IntValueType.INT.isCompatibleValue(result)) {
			throw new RuntimeException("incompatible value found " + result);
		}
		return (int) result;
	}

	/**
	 * Returns the default value as a long.
	 */
	public long getDefaultValueAsLong() {
		long result = subTypeArray.getDefaultValue();
		return result;
	}

	/**
	 * Constructs the IntValueContainer with the given default value and initial
	 * capacity
	 * 
	 * @throws NegativeArraySizeException if the capacity is negative
	 */
	public IntValueContainer(long defaultValue, Supplier<Iterator<Integer>> indexIteratorSupplier) {

		if (IntValueType.BYTE.isCompatibleValue(defaultValue)) {
			subTypeArray = new ByteArray((byte) defaultValue, indexIteratorSupplier);
		} else if (IntValueType.SHORT.isCompatibleValue(defaultValue)) {
			subTypeArray = new ShortArray((short) defaultValue, indexIteratorSupplier);
		} else if (IntValueType.INT.isCompatibleValue(defaultValue)) {
			subTypeArray = new IntArray((int) defaultValue, indexIteratorSupplier);
		} else {
			subTypeArray = new LongArray(defaultValue, indexIteratorSupplier);
		}
	}

	/**
	 * Sets the capacity to the given capacity if the current capacity is less than
	 * the one given.
	 */
	public void setCapacity(int capacity) {
		subTypeArray.setCapacity(capacity);
	}

	/**
	 * Returns the value at index as a byte.
	 * 
	 * @param index
	 * @return
	 * @throws ContractException
	 *                               <ul>
	 *                               <li>{@linkplain PropertyError#NEGATIVE_INDEX}
	 *                               if index is negative</li>
	 * @throws RuntimeException</li>
	 *                               <li>if index < 0</li>
	 *                               <li>if the value to return is not compatible
	 *                               with byte</li>
	 *                               </ul>
	 */
	public byte getValueAsByte(int index) {
		if (index < 0) {
			throw new ContractException(PropertyError.NEGATIVE_INDEX);
		}
		long result;
		if (index < subTypeArray.getCapacity()) {
			result = subTypeArray.getValue(index);
		} else {
			result = subTypeArray.getDefaultValue();
		}
		if (!IntValueType.BYTE.isCompatibleValue(result)) {
			throw new RuntimeException("incompatible value found " + result);
		}
		return (byte) result;
	}

	/**
	 * Returns the value at index as a short.
	 * 
	 * @param index
	 * @return
	 * @throws ContractException
	 *                               <ul>
	 *                               <li>{@linkplain PropertyError#NEGATIVE_INDEX}
	 *                               if index is negative</li>
	 * @throws RuntimeException</li>
	 *                               <li>if index < 0</li>
	 *                               <li>if the value to return is not compatible
	 *                               with short</li>
	 *                               </ul>
	 */
	public short getValueAsShort(int index) {
		if (index < 0) {
			throw new ContractException(PropertyError.NEGATIVE_INDEX);
		}
		long result;
		if (index < subTypeArray.getCapacity()) {
			result = subTypeArray.getValue(index);
		} else {
			result = subTypeArray.getDefaultValue();
		}
		if (!IntValueType.SHORT.isCompatibleValue(result)) {
			throw new RuntimeException("incompatible value found " + result);
		}
		return (short) result;

	}

	/**
	 * Returns the value at index as a long.
	 * 
	 * @param index
	 * @return
	 * @throws ContractException
	 *                               <ul>
	 *                               <li>{@linkplain PropertyError#NEGATIVE_INDEX}
	 *                               if index is negative</li>
	 * @throws RuntimeException</li>
	 *                               <li>if the value to return is not compatible
	 *                               with long</li>
	 *                               </ul>
	 */
	public int getValueAsInt(int index) {
		long result;
		if (index < 0) {
			throw new ContractException(PropertyError.NEGATIVE_INDEX);
		}
		if (index < subTypeArray.getCapacity()) {
			result = subTypeArray.getValue(index);
		} else {
			result = subTypeArray.getDefaultValue();
		}
		if (!IntValueType.INT.isCompatibleValue(result)) {
			throw new RuntimeException("incompatible value found " + result + " at index " + index);
		}
		return (int) result;
	}

	/**
	 * Returns the value at index as a long.
	 * 
	 * @param index
	 * @return
	 * @throws ContractException {@linkplain PropertyError#NEGATIVE_INDEX} if index
	 *                           is negative
	 */
	public long getValueAsLong(int index) {
		if (index < 0) {
			throw new ContractException(PropertyError.NEGATIVE_INDEX);
		}
		long result;
		if (index < subTypeArray.getCapacity()) {
			result = subTypeArray.getValue(index);
		} else {
			result = subTypeArray.getDefaultValue();
		}

		return result;
	}

	/**
	 * Sets the value at the index to the given byte
	 *
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PropertyError#NEGATIVE_INDEX} if
	 *                           index is negative</li>
	 *                           </ul>
	 */
	public void setByteValue(int index, byte value) {
		if (index < 0) {
			throw new ContractException(PropertyError.NEGATIVE_INDEX);
		}
		subTypeArray.setValue(index, value);
	}

	/**
	 * Sets the value at the index to the given short
	 * 
	 * @throws ContractException {@linkplain PropertyError#NEGATIVE_INDEX} if index
	 *                           is negative
	 */
	public void setShortValue(int index, short value) {
		if (index < 0) {
			throw new ContractException(PropertyError.NEGATIVE_INDEX);
		}
		if (!subTypeArray.getIntValueType().isCompatibleValue(value)) {
			subTypeArray = rebuildSubTypeArray(value);
		}

		subTypeArray.setValue(index, value);
	}

	/**
	 * Sets the value at the index to the given int
	 * 
	 * @throws ContractException {@linkplain PropertyError#NEGATIVE_INDEX} if index
	 *                           is negative
	 */
	public void setIntValue(int index, int value) {
		if (index < 0) {
			throw new ContractException(PropertyError.NEGATIVE_INDEX);
		}
		if (!subTypeArray.getIntValueType().isCompatibleValue(value)) {
			subTypeArray = rebuildSubTypeArray(value);
		}

		subTypeArray.setValue(index, value);
	}

	/**
	 * Sets the value at the index to the given long
	 * 
	 * @throws ContractException {@linkplain PropertyError#NEGATIVE_INDEX} if index
	 *                           is negative
	 */
	public void setLongValue(int index, long value) {
		if (index < 0) {
			throw new ContractException(PropertyError.NEGATIVE_INDEX);
		}
		if (!subTypeArray.getIntValueType().isCompatibleValue(value)) {
			subTypeArray = rebuildSubTypeArray(value);
		}

		subTypeArray.setValue(index, value);
	}

	/**
	 * Returns the IntValueType for this container. Each IntValueType corresponds to
	 * the current implementation type of the underlying array of primitives.
	 */
	public IntValueType getIntValueType() {
		return subTypeArray.getIntValueType();
	}

	/**
	 * Returns the capacity of this container. Capacity is guaranteed to be greater
	 * than or equal to size.
	 */
	public int getCapacity() {
		return subTypeArray.getCapacity();
	}

	/**
	 * Increments the value at the index by the given byte
	 *
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PropertyError#NEGATIVE_INDEX} if
	 *                           index is negative</li>
	 *                           </ul>
	 */
	public void incrementByteValue(int index, byte value) {

		long incrementedValue = Math.addExact(getValueAsLong(index), value);
		setLongValue(index, incrementedValue);
	}

	/**
	 * Increments the value at the index by the given short
	 *
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PropertyError#NEGATIVE_INDEX} if
	 *                           index is negative</li>
	 *                           </ul>
	 */
	public void incrementShortValue(int index, short value) {

		long incrementedValue = Math.addExact(getValueAsLong(index), value);
		setLongValue(index, incrementedValue);
	}

	/**
	 * Increments the value at the index by the given int
	 *
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PropertyError#NEGATIVE_INDEX} if
	 *                           index is negative</li>
	 *                           </ul>
	 */
	public void incrementIntValue(int index, int value) {

		long incrementedValue = Math.addExact(getValueAsLong(index), value);
		setLongValue(index, incrementedValue);
	}

	/**
	 * Increments the value at the index by the given long
	 *
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PropertyError#NEGATIVE_INDEX} if
	 *                           index is negative</li>
	 *                           </ul>
	 */
	public void incrementLongValue(int index, long value) {

		long incrementedValue = Math.addExact(getValueAsLong(index), value);
		setLongValue(index, incrementedValue);
	}

	/**
	 * Decrements the value at the index by the given byte
	 *
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PropertyError#NEGATIVE_INDEX} if
	 *                           index is negative</li>
	 *                           </ul>
	 */
	public void decrementByteValue(int index, byte value) {

		long decrementedValue = Math.subtractExact(getValueAsLong(index), value);
		setLongValue(index, decrementedValue);
	}

	/**
	 * Decrements the value at the index by the given short
	 *
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PropertyError#NEGATIVE_INDEX} if
	 *                           index is negative</li>
	 *                           </ul>
	 */
	public void decrementShortValue(int index, short value) {

		long decrementedValue = Math.subtractExact(getValueAsLong(index), value);
		setLongValue(index, decrementedValue);
	}

	/**
	 * Decrements the value at the index by the given int
	 *
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PropertyError#NEGATIVE_INDEX} if
	 *                           index is negative</li>
	 *                           </ul>
	 */
	public void decrementIntValue(int index, int value) {

		long decrementedValue = Math.subtractExact(getValueAsLong(index), value);
		setLongValue(index, decrementedValue);
	}

	/**
	 * Decrements the value at the index by the given long
	 *
	 * @throws ContractException
	 *                               <ul>
	 *                               <li>{@linkplain PropertyError#NEGATIVE_INDEX}
	 *                               if index is negative</li>
	 * @throws RuntimeException</li>
	 *                               <li>if index is negative</li>
	 *                               <li>if the value causes an overflow</li>
	 *                               </ul>
	 */
	public void decrementLongValue(int index, long value) {

		long decrementedValue = Math.subtractExact(getValueAsLong(index), value);
		setLongValue(index, decrementedValue);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("IntValueContainer [subTypeArray=");
		builder.append(subTypeArray);
		builder.append("]");
		return builder.toString();
	}

}
