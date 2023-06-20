package plugins.util.properties.arraycontainers;

import java.util.BitSet;
import java.util.function.IntPredicate;

/**
 * A container that maps non-negative int index values to booleans by storing
 * each boolean as a single bit in a BitSet. Returns a default boolean value for
 * every non-negative int index value until the value is explicitly set by an
 * invocation to the set() method.
 * 
 *
 */
public final class BooleanContainer {
	/*
	 * The default value to return for all indexes that are greater than or
	 * equal to the bounding index.
	 */
	private final boolean defaultValue;

	/*
	 * The lowest index value that has not been the subject of an explicit
	 * invocation of the set() method.
	 */
	private int boundingIndex;

	public BooleanContainer(boolean defaultValue, IntPredicate indexValidator) {
		this.defaultValue = defaultValue;
		bitSet = new BitSet();
		this.indexValidator = indexValidator;
	}

	public void expandCapacity(int count) {
		if(count>0) {
			set(boundingIndex+count-1,defaultValue);
		}
	}

	/*
	 * The bitSet containing the bit level representation of the Booleans.
	 */
	private BitSet bitSet;
	
	private final IntPredicate indexValidator;

	/**
	 * Returns the boolean value associated with the given index
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if the specified index is negative
	 */
	public boolean get(int index) {		
		/*
		 * If the index is beyond any we have had set, then return the default
		 * value.
		 */
		if (index >= boundingIndex) {
			return defaultValue;
		}
		return bitSet.get(index);
	}

	/**
	 * Set the boolean value associated with the given index
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if the specified index is negative
	 * @param index
	 * @param value
	 */
	public void set(int index, boolean value) {
		if (index < 0) {			
			throw new IndexOutOfBoundsException("index = " + index);
		}
		// if the index is new to us, then fill the bitSet with the default from
		// the bounding index to the index.
		if (index >= boundingIndex) {
			int newBoundingIndex = index + 1;
			bitSet.set(boundingIndex, newBoundingIndex, defaultValue);
			boundingIndex = newBoundingIndex;
		}
		bitSet.set(index, value);
	}
	
	
	private String getElementsString() {

		StringBuilder sb = new StringBuilder();

		boolean first = true;
		sb.append('[');
		int n = bitSet.size();
		for (int i = 0; i < n; i++) {
			if (indexValidator.test(i)) {
				if(first) {
					first = false;
				}else {
					sb.append(", ");
				}
				sb.append(bitSet.get(i));
			}
		}
		sb.append(']');
		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BooleanContainer [defaultValue=");
		builder.append(defaultValue);		
		builder.append(", bitSet=");
		builder.append(getElementsString());
		builder.append("]");
		return builder.toString();
	}
	
	

}