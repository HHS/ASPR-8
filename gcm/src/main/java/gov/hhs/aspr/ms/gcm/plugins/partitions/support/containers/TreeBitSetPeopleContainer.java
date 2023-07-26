package gov.hhs.aspr.ms.gcm.plugins.partitions.support.containers;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PartitionError;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import util.errors.ContractException;


/**
 * 
 * PeopleContainer implementor that uses a BitSet to record a boolean value of
 * true for each person contained. Uses ~1.3 bits for each person in the ENTIRE
 * POPULATION OF THE SIMULATION.
 * 
 */
public class TreeBitSetPeopleContainer implements PeopleContainer {

	private final int blockSize;
	private int[] intNodes;
	private short[] shortNodes;
	private byte[] byteNodes;
	private int blockStartingIndex;
	private int exclusizeMaxId;
	private int size;

	// bitSet holds the values for each person
	private BitSet bitSet;
	// the tree holds summation nodes in an array that is length two the
	// power
	// private TreeHolder treeHolder;

	private final PeopleDataManager peopleDataManager;

	
	/**
	 * Constructs the people container
	 * 
	 * @throws ContractException
	 * <li>{@linkplain PartitionError#NULL_PERSON_DATA_VIEW} if the person data view is null</li>
	 * 
	 */
	public TreeBitSetPeopleContainer(PeopleDataManager personDataManger) {
		if(personDataManger==null) {
			throw new ContractException(PartitionError.NULL_PERSON_DATA_VIEW);
		}
		blockSize = 63;
		this.peopleDataManager = personDataManger;
		// initialize the size of the bitSet to that of the full population,
		// including removed people
		int capacity = personDataManger.getPersonIdLimit();
		bitSet = new BitSet(capacity);
		initTrees(capacity, false);
	}

	private void initTrees(int capacity, boolean fillUllage) {

		long baseLayerBlockCount = capacity / blockSize;
		if (capacity % blockSize != 0) {
			baseLayerBlockCount++;
		}
		baseLayerBlockCount = FastMath.max(baseLayerBlockCount, 1);

		long baseLayerPower = 0;
		long nodeCount = 1;
		while (nodeCount < baseLayerBlockCount) {
			baseLayerPower++;
			nodeCount *= 2;
		}
		blockStartingIndex = 1 << baseLayerPower;

		if (fillUllage) {
			baseLayerBlockCount = blockStartingIndex;
		}

		exclusizeMaxId = (int) baseLayerBlockCount * blockSize;

		int byteNodeCount = 0;
		int shortNodeCount = 0;
		int intNodeCount = 0;

		long maxNodeValue = blockSize;
		for (long power = baseLayerPower; power >= 0; power--) {
			long nodesOnLayer;
			if (power == baseLayerPower) {
				nodesOnLayer = baseLayerBlockCount;
			} else {
				nodesOnLayer = 1 << power;
			}
			if (maxNodeValue <= Byte.MAX_VALUE) {
				byteNodeCount += nodesOnLayer;
			} else if (maxNodeValue <= Short.MAX_VALUE) {
				shortNodeCount += nodesOnLayer;
			} else {
				intNodeCount += nodesOnLayer;
			}
			maxNodeValue *= 2;
		}
		if (intNodeCount > 0) {
			intNodeCount++;
		} else if (shortNodeCount > 0) {
			shortNodeCount++;
		} else {
			byteNodeCount++;
		}

		intNodes = new int[intNodeCount];
		shortNodes = new short[shortNodeCount];
		byteNodes = new byte[byteNodeCount];

	}

	private int get(int index) {
		if (index < intNodes.length) {
			return intNodes[index];
		}
		index -= intNodes.length;
		if (index < shortNodes.length) {
			return shortNodes[index];
		}
		index -= shortNodes.length;
		return byteNodes[index];
	}

	/*
	 * Grows the tree to allow the given pid to exist, filling the ullage in the
	 * base layer of the tree as required
	 */
	private void grow(int pid) {
		int capacity = pid + 1;

		BitSet oldBitSet = bitSet;

		initTrees(capacity, true);
		bitSet = new BitSet(capacity);

		size = 0;
		for (int i = 0; i < exclusizeMaxId; i++) {
			if (oldBitSet.get(i)) {
				safeAdd(peopleDataManager.getBoxedPersonId(i).get());
			}
		}
	}

	// private void grow_smart(int pid) {
	// // determine the new size of the tree array
	// // int newTreeSize = getNearestPowerOfTwo(pid) >> (BLOCK_POWER - 2);
	//
	// int numberOfBlocks = pid / blockSize + 1;
	// int treeTop = getNextPowerOfTwo(numberOfBlocks);
	// int newTreeSize = treeTop * 2;
	//
	// /*
	// * The tree array grows by powers of two. We determine how many power
	// levels the
	// * new tree array is over the existing one to help us transport values
	// from the
	// * old array into the new array rather than recalculate those values.
	// * Essentially, the old tree will slide down the left hand side of the new
	// tree,
	// * while leaving a trail of the old tree's head value behind.
	// *
	// */
	//
	// // moving the old tree's values into the new tree
	// int power = getPower2(treeHolder.length());
	// int powerShift = getPower2(newTreeSize) - power;
	// // int[] newTree = new int[newTreeSize];
	// TreeHolder newTreeHolder = new TreeHolder(pid+1,blockSize,true);
	// int base = 1;
	// int newBase = base << powerShift;
	// for (int p = 0; p < power; p++) {
	// for (int i = 0; i < base; i++) {
	// // newTree[newBase + i] = tree[i + base];
	// int index = i + base;
	// if (index < treeHolder.length()) {
	// newTreeHolder.set(newBase + i, treeHolder.get(index));
	// }
	// }
	// base <<= 1;
	// newBase <<= 1;
	// }
	// /*
	// * The old tree's root value now has to propagate up the new tree to its
	// root
	// */
	// base = 1 << powerShift;
	// while (base > 1) {
	// base >>= 1;
	// // newTree[base] = tree[1];
	// newTreeHolder.set(base, treeHolder.get(1));
	// }
	// // swap the tree
	// // tree = newTree;
	// treeHolder = newTreeHolder;
	// }

	@Override
	public boolean unsafeAdd(PersonId personId) {
		return safeAdd(personId);
	}

	@Override
	public boolean safeAdd(PersonId personId) {
		int value = personId.getValue();

		// do we need to grow?
		if (value >= exclusizeMaxId) {
			grow(value);
		}
		// add the value
		if (!bitSet.get(value)) {
			bitSet.set(value);
			// select the block(index) that will receive the bit flip.
			int block = value / blockSize;
			block += blockStartingIndex;
			/*
			 * Propagate the change up through the tree to the root node
			 */
			while (block > 0) {
				increment(block);
				block /= 2;
			}
			size++;
			return true;
		}
		return false;
	}

	private void increment(int index) {

		if (index < intNodes.length) {
			intNodes[index]++;
		} else {
			index -= intNodes.length;
			if (index < shortNodes.length) {
				shortNodes[index]++;
			} else {
				index -= shortNodes.length;
				if (index < byteNodes.length) {
					byteNodes[index]++;
				}
			}
		}
	}

	@Override
	public boolean remove(PersonId personId) {
		int value = personId.getValue();
		/*
		 * If the person is not contained, then don't try to remove them. This
		 * protects us from removals that are >= exclusizeMaxId.
		 */
		if (bitSet.get(value)) {
			bitSet.set(value, false);
			// select the block(index) that will receive the bit flip.
			int block = value / blockSize;
			block += blockStartingIndex;
			/*
			 * Propagate the change up through the tree to the root node
			 */
			while (block > 0) {
				decrement(block);
				block /= 2;
			}
			size--;
			return true;
		}
		return false;
	}

	private void decrement(int index) {
		if (index < intNodes.length) {
			intNodes[index]--;
		} else {
			index -= intNodes.length;
			if (index < shortNodes.length) {
				shortNodes[index]--;
			} else {
				index -= shortNodes.length;
				if (index < byteNodes.length) {
					byteNodes[index]--;
				}
			}
		}
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean contains(PersonId personId) {
		return bitSet.get(personId.getValue());
	}

	@Override
	public PersonId getRandomPersonId(RandomGenerator randomGenerator) {
		if(size==0) {
			return null;
		}
		
		int index = randomGenerator.nextInt(size);

		/*
		 * We need to use an integer that is at least one, so we add one to the
		 * selected index. We will reduce this amount until it reaches zero.
		 */
		int targetCount = index + 1;

		/*
		 * Find the mid point of the tree. Think of the tree array as a triangle
		 * with a single root node at the top. This will be the first array
		 * element in the last row(last half) in the tree. This is the row that
		 * maps to the blocks in the bitset.
		 */
		int treeIndex = 1;

		/*
		 * Walk downward in the tree. If we move to the right, we have to reduce
		 * the target value.
		 */
		while (treeIndex < blockStartingIndex) {
			// move to the left child
			treeIndex = treeIndex << 1;
			/*
			 * If the left child is less than the target count, then reduce the
			 * target count by the number in the left child and move to the
			 * right child
			 */
			int nodeValue = get(treeIndex);
			if (nodeValue < targetCount) {
				targetCount -= nodeValue;
				treeIndex++;
			}
		}
		/*
		 * We have arrived at the element of the tree array that corresponds to
		 * the desired block in the bitset. We will need to determine the
		 * positions to scan in the bitset
		 */
		// int bitSetStartIndex = (treeIndex - midTreeIndex) << BLOCK_POWER;
		int bitSetStartIndex = (treeIndex - blockStartingIndex) * blockSize;
		int bitSetStopIndex = bitSetStartIndex + blockSize;
		/*
		 * Finally, we scan the bits and reduce the target count until it
		 * reaches zero.
		 */
		for (int i = bitSetStartIndex; i < bitSetStopIndex; i++) {
			if (bitSet.get(i)) {
				targetCount--;
				if (targetCount == 0) {
					return peopleDataManager.getBoxedPersonId(i).get();
				}
			}
		}
		return null;
	}

	@Override
	public List<PersonId> getPeople() {
		List<PersonId> result = new ArrayList<>(size());
		int n = bitSet.size();
		for (int i = 0; i < n; i++) {
			if (bitSet.get(i)) {
				result.add(peopleDataManager.getBoxedPersonId(i).get());
			}
		}
		return result;
	}

}
