package gov.hhs.aspr.ms.gcm.plugins.properties.support.arraycontainers;

import java.util.Iterator;
import java.util.function.Supplier;

/**
 * A container for retaining enum values from a single enumeration class indexed
 * by int index. It is designed to generally require approximately 1 byte per
 * index rather than a 4 or 8 byte object reference.
 */
public final class EnumContainer {

	/*
	 * We store the enum values by their ord equivalents.
	 */
	private IntValueContainer intValueContainer;

	/*
	 * We convert an ord value for an enum into enum member instance via an
	 * ObjectValueContainer.
	 */
	private ObjectValueContainer objectValueContainer;

	/*
	 * The class reference for the enum type
	 */
	private final Class<?> enumClass;
	private final Supplier<Iterator<Integer>> indexIteratorSupplier;

	/**
	 * Constructs an instance of EnumContainer.
	 * 
	 * @throws IllegalArgumentException
	 *                                  <ul>
	 *                                  <li>if the class is null</li>
	 *                                  <li>if the class is not an enumeration</li>
	 *                                  <li>if the default is not null and not a
	 *                                  member of the enumeration</li>
	 *                                  </ul>
	 */
	public EnumContainer(Class<?> c, Object defaultValue, Supplier<Iterator<Integer>> indexIteratorSupplier) {
		if (c == null) {
			throw new IllegalArgumentException("null class reference");
		}

		if (!Enum.class.isAssignableFrom(c)) {
			throw new IllegalArgumentException("cannot construct from class " + c.getName());
		}

		if (defaultValue != null && defaultValue.getClass() != c) {
			throw new IllegalArgumentException("default value " + defaultValue + " does not match enum class " + c);
		}

		this.indexIteratorSupplier = indexIteratorSupplier;

		objectValueContainer = new ObjectValueContainer(null, null);
		enumClass = c;
		if (defaultValue != null) {
			Enum<?> e = (Enum<?>) defaultValue;
			objectValueContainer.setValue(e.ordinal(), defaultValue);
			intValueContainer = new IntValueContainer(e.ordinal(), null);
		} else {
			intValueContainer = new IntValueContainer(-1, null);
		}
	}

	/**
	 * Set the value at the given index.
	 * 
	 * @throws IllegalArgumentException
	 *                                  <ul>
	 *                                  <li>if the index is negative</li>
	 *                                  <li>if the value is null</li>
	 *                                  <li>if the value is not a member of the
	 *                                  enumeration</li>
	 *                                  </ul>
	 */
	public void setValue(int index, Object value) {

		if (index < 0) {
			throw new IllegalArgumentException("negative index: " + index);
		}

		if (value == null) {
			throw new IllegalArgumentException("null value");
		}
		if (value.getClass() != enumClass) {
			throw new IllegalArgumentException("improper class type for value " + value + ", expected " + enumClass);
		}

		/*
		 * Convert the object value into an instance of Enum
		 */
		Enum<?> e = (Enum<?>) value;

		if (objectValueContainer.getValue(e.ordinal()) == null) {
			objectValueContainer.setValue(e.ordinal(), value);
		}

		intValueContainer.setIntValue(index, e.ordinal());

	}

	/**
	 * Set the value at the given index.
	 * 
	 * @throws ArrayIndexOutOfBoundsException if the index is negative
	 */
	public Object getValue(int index) {
		/*
		 * Retrieve the ordinal value from the index
		 */
		int ordinal = intValueContainer.getValueAsInt(index);
		/*
		 * Convert the ordinal value into the enum member instance.
		 */
		if (ordinal >= 0) {
			return objectValueContainer.getValue(ordinal);
		} else {
			return null;
		}
	}

	public int getCapacity() {
		return intValueContainer.getCapacity();
	}

	public void setCapacity(int capacity) {
		intValueContainer.setCapacity(capacity);
	}

	private String getElementsString() {
		Iterator<Integer> iterator = indexIteratorSupplier.get();
		StringBuilder sb = new StringBuilder();

		boolean first = true;
		sb.append('[');

		while (iterator.hasNext()) {
			Integer index = iterator.next();
			Object value = getValue(index);

			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}

			sb.append(index);
			sb.append("=");
			sb.append(value);

		}
		sb.append(']');
		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EnumContainer [values=");
		builder.append(getElementsString());
		builder.append(", enumClass=");
		builder.append(enumClass);
		builder.append("]");
		return builder.toString();
	}

}