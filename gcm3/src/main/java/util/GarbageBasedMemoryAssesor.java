package util;

/**
 * A utility class that measures the average size of a large number of instances
 * of a class by forcing garbage collection and utilizing {@link Runtime} memory
 * functions.
 * 
 * @author Shawn Hatch
 *
 */
public final class GarbageBasedMemoryAssesor {

	/**
	 * The InstanceFactory is used to generate new instance of the tested class.
	 * The instance factory must 1)generate a new instance each time its
	 * getNext() method is invoked, 2) not retain the generated instances or any
	 * other state that is not present at the first invocation of the factory's
	 * getNext() method and 3) generate an unspecified number of instances not
	 * limited to the instance count used in the static
	 * getAverageInstanceByteCount() method.
	 * 
	 * 
	 */
	public static interface InstanceFactory {
		public Object getNext();
	}

	private static final int INNER_FORCE_GARBAGE_COLLECTION_COUNT = 5;

	private static final int MAX_ATTEMPTS_TO_RUN_GARBAGE_COLLECTION = 1000;

	private static final Runtime LOCAL_RUNTIME = Runtime.getRuntime();

	/**
	 * Returns the average number of bytes used per instance that is generated
	 * by the given instance factory. The larger the instanceCount, the more
	 * accurate will be the estimate. The instanceCount may be exceeded during
	 * the calculation and serves as a suggested value.
	 * 
	 * @param instanceFactory
	 * @param instanceCount
	 * @return
	 * @throws Exception
	 */
	public static double getAverageInstanceByteCount(InstanceFactory instanceFactory, int instanceCount) throws Exception {

		final Object[] instances = new Object[instanceCount];

		// stimulate the garbage collection process and the instance factory
		instanceFactory.getNext();
		outerForceGarbageCollection();

		final long preAllocationBytesInUse = getBytesInUse();

		// stores the instances from the factory in a local array to maintain
		// reference and guarantee that the JVM does not optimize out the
		// instances because the are not used from its perspective.
		for (int i = 0; i < instanceCount; i++) {
			instances[i] = instanceFactory.getNext();
		}
		outerForceGarbageCollection();
		final long postAllocationBytesInUse = getBytesInUse();

		final long bytesUsedForAllocation = postAllocationBytesInUse - preAllocationBytesInUse;
		final double floatingPointBytes = bytesUsedForAllocation;
		final double instanceFloatingPointBytes = Math.round((floatingPointBytes / instanceCount));

		// we now must continue to reference the instances so that the JVM does
		// not sneak in and clean them up before we get a chance to have measure
		// the memory in use.
		for (Object instance : instances) {
			if (instance == null) {
				throw new RuntimeException("this exception is used purely to guarantee that instance stay in scope");
			}
		}
		return instanceFloatingPointBytes;
	}

	/*
	 * Returns the local runtime's total memory minus its free memory.
	 */
	private static long getBytesInUse() {
		return LOCAL_RUNTIME.totalMemory() - LOCAL_RUNTIME.freeMemory();
	}

	/*
	 * Executes the actual garbage collection calls and uses Thread.yield() to
	 * encourage the JVM to fully clean up
	 */
	private static void innerForceGarbageCollection() throws Exception {
		long lastBytesInUse = Long.MAX_VALUE;
		long currentBytesInUse = getBytesInUse();
		int attemptsToRunGarbageCollection = 0;
		while ((currentBytesInUse < lastBytesInUse) && (attemptsToRunGarbageCollection < MAX_ATTEMPTS_TO_RUN_GARBAGE_COLLECTION)) {
			attemptsToRunGarbageCollection++;
			LOCAL_RUNTIME.runFinalization();
			LOCAL_RUNTIME.gc();
			Thread.yield();
			lastBytesInUse = currentBytesInUse;
			currentBytesInUse = getBytesInUse();
		}

	}

	/*
	 * Forces garbage collection several times. The innerForceGarbageCollection
	 * method actually calls the runtime garbage collection and we use this
	 * outer method to help assure that the exiting of the inner method will
	 * help the JVM actually finish up on garbage collection.
	 */
	private static void outerForceGarbageCollection() throws Exception {
		for (int i = 0; i < INNER_FORCE_GARBAGE_COLLECTION_COUNT; i++) {
			innerForceGarbageCollection();
		}
	}

	/**
	 * Repetitively forces garbage collection and then returns the number of
	 * bytes currently in use.
	 */
	public static long getHeapSize() throws Exception {
		outerForceGarbageCollection();
		return getBytesInUse();
	}

}