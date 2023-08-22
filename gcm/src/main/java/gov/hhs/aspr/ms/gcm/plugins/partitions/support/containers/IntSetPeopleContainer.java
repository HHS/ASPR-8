package gov.hhs.aspr.ms.gcm.plugins.partitions.support.containers;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;

/**
 * PeopleContainer implementor that uses hash-bucketed ArrayLists and an
 * array-based tree to contain the people. Uses ~ 45 bits per person contained.
 */
public final class IntSetPeopleContainer implements PeopleContainer {

	/**
	 * The general best practice bucket depth for ArrayIntSets containing millions
	 * of entries.
	 */
	public final static float DEFAULT_TARGET_DEPTH = 80;

	/*
	 * An array of ArrayLists that hold the values in the set.
	 */
	private List<PersonId>[] buckets;

	private int[] tree;

	/*
	 * An array that is the same length as the values array that tracks the maximum
	 * size each ArrayList instance in values has reached during its life-span. This
	 * value is used to trigger an occasional rebuild of these ArrayLists to reduce
	 * instance size.
	 */
	// private int[] maxSizes;
	private static enum MaxMode {
		BYTE, SHORT, INT
	}

	private static class MaxSizeManager {

		private MaxMode maxMode = MaxMode.BYTE;

		private int[] iMaxSizes;
		private short[] sMaxSizes;
		private byte[] bMaxSizes;

		public MaxSizeManager(int capacity) {
			bMaxSizes = new byte[capacity];
		}

		public void setMaxSize(int index, int size) {

			switch (maxMode) {
			case BYTE:
				if (size > Byte.MAX_VALUE) {
					if (size > Short.MAX_VALUE) {
						int bLength = bMaxSizes.length;
						iMaxSizes = new int[bLength];
						for (int i = 0; i < bLength; i++) {
							iMaxSizes[i] = bMaxSizes[i];
						}
						bMaxSizes = null;
						maxMode = MaxMode.INT;

					} else {
						int bLength = bMaxSizes.length;
						sMaxSizes = new short[bLength];
						for (int i = 0; i < bLength; i++) {
							sMaxSizes[i] = bMaxSizes[i];
						}
						bMaxSizes = null;
						maxMode = MaxMode.SHORT;

					}
				}

				break;
			case INT:
				// do nothing
				break;
			case SHORT:
				if (size > Short.MAX_VALUE) {
					int bLength = bMaxSizes.length;
					iMaxSizes = new int[bLength];
					for (int i = 0; i < bLength; i++) {
						iMaxSizes[i] = bMaxSizes[i];
					}
					sMaxSizes = null;
					maxMode = MaxMode.INT;
				}
				break;
			default:

				throw new RuntimeException("unhandled case " + maxMode);
			}

			switch (maxMode) {
			case BYTE:
				bMaxSizes[index] = (byte) size;
				break;
			case INT:
				iMaxSizes[index] = size;
				break;
			case SHORT:
				sMaxSizes[index] = (short) size;
				break;
			default:

				throw new RuntimeException("unhandled case " + maxMode);
			}
		}

		public int getMaxSize(int index) {
			switch (maxMode) {
			case BYTE:
				return bMaxSizes[index];

			case INT:
				return iMaxSizes[index];

			case SHORT:
				return sMaxSizes[index];
			default:

				throw new RuntimeException("unhandled case " + maxMode);
			}
		}
	}

	private MaxSizeManager maxSizeManager;

	/*
	 * The number of Integers stored in this set
	 */
	private int size;

	/*
	 * The targeted list depth that is used to determine the number of buckets(i.e.
	 * the length of the values array) in this set.
	 */
	private final float targetDepth;

	/**
	 * Constructs an ArrayIntSet having the DEFAULT_TARGET_DEPTH and tolerance of
	 * duplicate values.
	 * 
	 * @throws IllegalArgumentException
	 *                                  <li>the target depth is not positive</li>
	 *                                  </ul>
	 */
	public IntSetPeopleContainer() {
		this.targetDepth = DEFAULT_TARGET_DEPTH;
	}

	/*
	 * Grows the values and maxSizes arrays to achieve an average bucket depth that
	 * closer to the target bucket depth.
	 */
	@SuppressWarnings("unchecked")
	private void grow() {
		if (buckets == null) {
			// establish a single bucket
			buckets = new List[1];
			// maxSizes = new int[1];
			maxSizeManager = new MaxSizeManager(1);

			tree = new int[2];
		} else {
			// double the number of buckets
			rebuild(buckets.length << 1);
		}
	}

	/*
	 * Shrinks the values and maxSizes arrays to achieve an average bucket depth
	 * that closer to the target bucket depth.
	 */
	private void shrink() {
		if (buckets.length > 1) {
			// halve the number of buckets
			rebuild(buckets.length >> 1);
		}
	}

	/*
	 * Rebuilds the buckets to the new size for the values and maxSizes arrays.
	 */
	private void rebuild(int newSize) {
		@SuppressWarnings("unchecked")
		/*
		 * create a new values array to the new size
		 */
		List<PersonId>[] newValues = new List[newSize];
		/*
		 * Rebuild the maxSizes array to the correct length. The old values in the
		 * maxSizes array can be forgotten.
		 */
		// maxSizes = new int[newValues.length];
		maxSizeManager = new MaxSizeManager(newValues.length);
		/*
		 * Place the values from the old values array into the new values array.
		 */
		tree = new int[newSize * 2];

		for (int i = 0; i < buckets.length; i++) {
			List<PersonId> list = buckets[i];
			if (list != null) {
				for (PersonId value : list) {
					/*
					 * Since the length of the values array is always a power of 2, we can use a
					 * bit-wise math trick to calculate the modulus of value with values.length to
					 * derive the index of the bucket where the value should be stored.
					 */
					int index = value.getValue() & (newValues.length - 1);
					/*
					 * Get the list where the value should be stored or create it if it does not yet
					 * exist.
					 */
					List<PersonId> newList = newValues[index];
					if (newList == null) {
						newList = new ArrayList<>();
						newValues[index] = newList;
					}

					int treeIndex = index + newSize;
					while (treeIndex > 0) {
						tree[treeIndex]++;
						treeIndex /= 2;
					}

					/*
					 * Place the value in the list.
					 */
					newList.add(value);
				}
				/*
				 * Null out the ArrayList instance to encourage the GC to collect the list now.
				 * 
				 */
				buckets[i] = null;
			}
		}
		/*
		 * We no longer need the old values array, so we replace it with the new values
		 * array.
		 */
		buckets = newValues;
		/*
		 * Finally, we establish the maxSizes array values.
		 */
		for (int i = 0; i < buckets.length; i++) {
			List<PersonId> list = buckets[i];
			if (list != null) {
				// maxSizes[i] = list.size();
				maxSizeManager.setMaxSize(i, list.size());
			}
		}
	}

	@Override
	public boolean safeAdd(PersonId personId) {
		if (contains(personId)) {
			return false;
		}
		/*
		 * We have show that the person id is not contained so we can invoke the unsafe
		 * add
		 */
		return unsafeAdd(personId);
	}

	@Override
	public boolean unsafeAdd(PersonId personId) {

		if (buckets == null) {
			grow();
		}
		/*
		 * The bucket index is value % values.length, but this is a bit faster since we
		 * know the values array length is a power of two.
		 */
		int index = personId.getValue() & (buckets.length - 1);

		/*
		 * Add the value to list located at the index
		 */
		List<PersonId> list = buckets[index];
		if (list == null) {
			list = new ArrayList<>();
			buckets[index] = list;
		}

		list.add(personId);

		size++;

		// increment the values int the tree
		int treeIndex = index + buckets.length;
		while (treeIndex > 0) {
			tree[treeIndex]++;
			treeIndex /= 2;
		}

		/*
		 * Update the maxSizes
		 */
		// if (maxSizes[index] < list.size()) {
		// maxSizes[index] = list.size();
		// }
		if (maxSizeManager.getMaxSize(index) < list.size()) {
			// maxSizes[index] = list.size();
			maxSizeManager.setMaxSize(index, list.size());
		}

		/*
		 * If the averageDepth exceeds the target depth then we should grow.
		 * (size/values.length)>targetDepth
		 * 
		 */
		if (size > targetDepth * buckets.length) {
			grow();
		}
		return true;
	}

	@Override
	public boolean remove(PersonId personId) {
		if (buckets == null) {
			return false;
		}
		/*
		 * The bucket index is value % values.length, but this is a bit faster since we
		 * know the values array length is a power of two.
		 */
		int index = personId.getValue() & (buckets.length - 1);
		/*
		 * Remove the value from the list
		 */
		List<PersonId> list = buckets[index];

		if (list == null) {
			return false;
		}

		if (list.remove(personId)) {
			size--;
			/*
			 * If the list is now less than half its maxSize in the past, then we should
			 * rebuild the list and record the new maxSize for the list.
			 */
			// decrement the values int the tree
			int treeIndex = index + buckets.length;
			while (treeIndex > 0) {
				tree[treeIndex]--;
				treeIndex /= 2;
			}

			if (list.size() * 2 < maxSizeManager.getMaxSize(index)) {
				if (list.size() > 0) {
					buckets[index] = new ArrayList<>(list);
					maxSizeManager.setMaxSize(index, list.size());
				} else {
					buckets[index] = null;
					maxSizeManager.setMaxSize(index, 0);
				}
			}
			/*
			 * If the averageDepth is less than half the target depth then we should shrink.
			 * (size/values.length)*2<targetDepth
			 * 
			 */
			if (size * 2 < targetDepth * buckets.length) {
				shrink();
			}
			return true;
		}
		return false;

	}

	@Override
	public List<PersonId> getPeople() {
		List<PersonId> result = new ArrayList<>(size);
		if (buckets != null) {
			for (List<PersonId> list : buckets) {
				if (list != null) {
					result.addAll(list);
				}
			}
		}
		return result;
	}

	@Override
	public PersonId getRandomPersonId(RandomGenerator randomGenerator) {

		if (size == 0) {
			return null;
		}

		int targetCount = randomGenerator.nextInt(size);

		/*
		 * Find the mid point of the tree. Think of the tree array as a triangle with a
		 * single root node at the top. This will be the first array element in the last
		 * row(last half) in the tree. This is the row that maps to the blocks in the
		 * bitset.
		 */
		int midTreeIndex = buckets.length;
		int treeIndex = 1;

		/*
		 * Walk downward in the tree. If we move to the right, we have to reduce the
		 * target value.
		 */
		while (treeIndex < midTreeIndex) {
			// move to the left child
			treeIndex *= 2;
			// if the left child is less than the target count, then reduce the
			// target count
			// by the number in the left child and move to the right child
			if (tree[treeIndex] <= targetCount) {
				targetCount -= tree[treeIndex];
				treeIndex++;
			}
		}
		/*
		 * We have arrived at the element of the tree array that corresponds to desired
		 * bucket
		 */
		List<PersonId> targetList = buckets[treeIndex - midTreeIndex];
		return targetList.get(targetCount);

	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean contains(PersonId personId) {
		if (buckets == null) {
			return false;
		}
		int index = personId.getValue() & (buckets.length - 1);
		List<PersonId> list = buckets[index];
		if (list == null) {
			return false;
		}
		return list.contains(personId);
	}

}
