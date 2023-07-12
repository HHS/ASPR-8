package plugins.groups.testsupport;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.random.RandomGenerator;

import plugins.groups.support.GroupTypeId;

/**
 * Enumeration of GroupTypeId to support unit testing
 */
public enum TestGroupTypeId implements GroupTypeId {
	GROUP_TYPE_1, GROUP_TYPE_2, GROUP_TYPE_3;

	/**
	 * Returns a randomly selected member of this enumeration.
	 * 
	 * Precondition: The random generator must not be null
	 */
	public static TestGroupTypeId getRandomGroupTypeId(final RandomGenerator randomGenerator) {
		return TestGroupTypeId.values()[randomGenerator.nextInt(TestGroupTypeId.values().length)];
	}

	public static int size() {
		return values().length;
	}

	private TestGroupTypeId next;

	/**
	 * Returns the next member of this enumeration
	 */
	public TestGroupTypeId next() {
		if (next == null) {
			next = TestGroupTypeId.values()[(ordinal() + 1) % TestGroupTypeId.values().length];
		}
		return next;
	}

	/**
	 * Returns a new {@link GroupTypeId} instance.
	 */
	public static GroupTypeId getUnknownGroupTypeId() {
		return new GroupTypeId() {
		};
	}
	
	public static List<TestGroupTypeId> getTestGroupTypeIds(){
		return Arrays.asList(TestGroupTypeId.values());
	}
	
	public static List<TestGroupTypeId> getShuffledTestGroupTypeIds(RandomGenerator randomGenerator){
		List<TestGroupTypeId> result = getTestGroupTypeIds();
		Random random = new Random(randomGenerator.nextLong());
		Collections.shuffle(result,random);
		return result;
	}

}
