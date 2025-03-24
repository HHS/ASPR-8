package gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.containers;

import java.util.Arrays;

public class Bucket {
	private int size;
	private int[] values;
	
	
	public Bucket() {}
	
	public Bucket(Bucket bucket) {
		values = Arrays.copyOf(bucket.values, bucket.size);
		size = bucket.size;
	}

	public void unsafeAdd(int value) {
		if (values == null) {
			values = new int[1];
		} else {
			if (size == values.length) {
				int newLength;
				if (size == 1) {
					newLength = 2;
				} else {
					newLength = (values.length * 3) / 2;
				}
				values = Arrays.copyOf(values, newLength);
			}
		}
		values[size++] = value;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Bucket [size=");
		builder.append(size);
		builder.append(", values=");
		builder.append(Arrays.toString(values));
		builder.append("]");
		return builder.toString();
	}

	public void safeAdd(int value) {
		if (indexOf(value) < 0) {
			unsafeAdd(value);
		}
	}

	public boolean remove(int value) {
		int index = indexOf(value);
		if (index >= 0) {
			size--;
			if (size > index) {
				System.arraycopy(values, index + 1, values, index, size - index);
			}
			return true;
		}
		return false;
	}

	public boolean contains(int value) {
		return indexOf(value)>=0;
	}
	
	private int indexOf(int value) {
		for (int i = 0; i < size; i++) {
			if (values[i] == value) {
				return i;
			}
		}
		return -1;
	}

	public int get(int index) {
		return values[index];
	}

	public int size() {
		return size;
	}

//	    private void fastRemove(Object[] es, int i) {
//	        modCount++;
//	        final int newSize;
//	        if ((newSize = size - 1) > i)
//	            System.arraycopy(es, i + 1, es, i, newSize - i);
//	        es[size = newSize] = null;
//	    }
//		private void add(E e, Object[] elementData, int s) {
//	        if (s == elementData.length)
//	            elementData = grow();
//	        elementData[s] = e;
//	        size = s + 1;
//	    }
//		  private Object[] grow() {
//		        return grow(size + 1);
//		    }
//		 private Object[] grow(int minCapacity) {
//		        int oldCapacity = elementData.length;
//		        if (oldCapacity > 0 || elementData != DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
//		            int newCapacity = ArraysSupport.newLength(oldCapacity,
//		                    minCapacity - oldCapacity, /* minimum growth */
//		                    oldCapacity >> 1           /* preferred growth */);
//		            return elementData = Arrays.copyOf(elementData, newCapacity);
//		        } else {
//		            return elementData = new Object[Math.max(DEFAULT_CAPACITY, minCapacity)];
//		        }
//		    }

}
