package util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well44497b;

public class SeedProvider {

	private final Map<Integer, SeedRecord> seedRecordMap = new LinkedHashMap<>();

	private RandomGenerator randomGenerator;

	private static class SeedRecord {
		private boolean used;
		private long value;
	}

	public SeedProvider(long seed) {
		randomGenerator = getRandomGenerator(seed);
	}

	private static class Range {
		private int low;
		private int high;

		@Override
		public String toString() {
			return low + "-" + high;
		}
	}

	private List<Range> getRanges() {
		List<Range> result = new ArrayList<>();
		List<Integer> unusedSeeds = getUnusedSeedCases();
		Range range = null;
		for (Integer value : unusedSeeds) {
			if (range == null) {
				range = new Range();
				range.low = value;
				range.high = value;
			} else if (value != range.high + 1) {
				result.add(range);
				range = new Range();
				range.low = value;
				range.high = value;
			} else {
				range.high = value;
			}
		}
		if (range != null) {
			result.add(range);
		}
		return result;

	}

	public String generateUnusedSeedReport() {
		List<Range> ranges = getRanges();
		StringBuilder sb = new StringBuilder();
		sb.append("Unused Seeds : ");
		boolean first = true;
		for (Range range : ranges) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(range);
		}
		if (!first) {
			sb.append(", ");
		}
		sb.append(seedRecordMap.size());
		sb.append("+");
		return sb.toString();
	}

	/*
	 * Returns the seed value for the given seed case. Seed cases correspond to
	 * the construction of a TestPlanExecutor and should not be reused. Seed
	 * values are generated this way so that each simulation instance is
	 * slightly different from other simulation instances for each test and the
	 * internal behavior of each test is repeatable without regard to the order
	 * of execution within the tests.
	 */
	public long getSeedValue(final int seedCase) {

		if (seedCase < 0) {
			throw new RuntimeException();
		}
		SeedRecord seedRecord = seedRecordMap.get(seedCase);
		if (seedRecord == null) {
			while (seedRecordMap.size() <= seedCase) {
				seedRecord = new SeedRecord();
				seedRecord.value = randomGenerator.nextLong();
				seedRecordMap.put(seedRecordMap.size(), seedRecord);
			}
		}
		if (seedRecord.used) {
			String message = "Seed case " + seedCase + " should not be re-used";
			System.err.println(message);
			throw new RuntimeException(message);
		}
		seedRecord.used = true;
		return seedRecord.value;
	}

	/*
	 * Returns each seed case that is not used for which there is a used seed of
	 * higher value
	 */
	private List<Integer> getUnusedSeedCases() {
		List<Integer> result = new ArrayList<>();
		for (Integer key : seedRecordMap.keySet()) {
			SeedRecord seedRecord = seedRecordMap.get(key);
			if (!seedRecord.used) {
				result.add(key);
			}
		}
		return result;
	}

	public boolean hasUnusedSeeds() {
		for (Integer key : seedRecordMap.keySet()) {
			SeedRecord seedRecord = seedRecordMap.get(key);
			if (!seedRecord.used) {
				return true;
			}
		}
		return false;
	}

	public static RandomGenerator getRandomGenerator(long seed) {
		return new Well44497b(seed);
	}

}
