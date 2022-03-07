package util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;

import annotations.UnitTest;
import annotations.UnitTestConstructor;
import annotations.UnitTestMethod;
import util.stats.MutableStat;
import util.stats.Stat;

/**
 * Test class for {@link MutableStat}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = MutableStat.class)
public class AT_MutableStat {
	

	private static final double TOLERANCE = 0.0001;

	private static void showSimilar(Stat stat1, Stat stat2) {
		assertEquals(stat1.size(), stat2.size());
		if (stat1.size() > 0) {
			showSimilarValues(stat1.getMin().get(), stat2.getMin().get());
			showSimilarValues(stat1.getMax().get(), stat2.getMax().get());
			showSimilarValues(stat1.getMean().get(), stat2.getMean().get());
			showSimilarValues(stat1.getStandardDeviation().get(), stat2.getStandardDeviation().get());
			showSimilarValues(stat1.getVariance().get(), stat2.getVariance().get());
		}
	}

	private static void showSimilarValues(double value1, double value2) {
		double mid = (value1 + value2) / 2;
		double portion = Math.abs((mid - value1) / mid);
		if (Double.isFinite(portion)) {
			assertTrue(portion < TOLERANCE);
		} else {
			double diff = Math.abs(value2 - value1);
			assertTrue(diff < TOLERANCE);
		}
	}

	/**
	 * Tests {@link MutableStat#combineStats(Stat...)}
	 */
	@Test
	@UnitTestMethod(name = "combineStats", args = { Stat[].class })
	public void testCombineStats() {
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(5642328443843803200L);

		for (int k = 0; k < 100; k++) {
			int n = randomGenerator.nextInt(10);
			MutableStat stat1 = new MutableStat();
			List<Double> values1 = new ArrayList<>();
			for (int i = 0; i < n; i++) {
				double value = randomGenerator.nextDouble() * 100 + 50;
				values1.add(value);
				stat1.add(value);
			}

			// make sure that at least one of the stat objects is non-empty
			n = randomGenerator.nextInt(10) + 1;
			MutableStat stat2 = new MutableStat();
			List<Double> values2 = new ArrayList<>();
			for (int i = 0; i < n; i++) {
				double value = randomGenerator.nextDouble() * 90 + 70;
				values2.add(value);
				stat2.add(value);
			}

			MutableStat expectedStat = new MutableStat();
			for (int i = 0; i < values1.size(); i++) {
				expectedStat.add(values1.get(i));
			}
			for (int i = 0; i < values2.size(); i++) {
				expectedStat.add(values2.get(i));
			}

			Stat combinedStat = MutableStat.combineStats(stat1, stat2);

			showSimilar(expectedStat, combinedStat);
		}
	}

	/**
	 * Tests {@link MutableStat#combineStatsCollection(Collection)}
	 */
	@Test
	@UnitTestMethod(name = "combineStatsCollection", args = { Collection.class })
	public void testCombineStatsCollection() {
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(2697343520338303649L);

		for (int k = 0; k < 100; k++) {
			int n = randomGenerator.nextInt(10);
			MutableStat stat1 = new MutableStat();
			List<Double> values1 = new ArrayList<>();
			for (int i = 0; i < n; i++) {
				double value = randomGenerator.nextDouble() * 100 + 50;
				values1.add(value);
				stat1.add(value);
			}

			// make sure that at least one of the stat objects is non-empty
			n = randomGenerator.nextInt(10) + 1;
			MutableStat stat2 = new MutableStat();
			List<Double> values2 = new ArrayList<>();
			for (int i = 0; i < n; i++) {
				double value = randomGenerator.nextDouble() * 90 + 70;
				values2.add(value);
				stat2.add(value);
			}

			MutableStat expectedStat = new MutableStat();
			for (int i = 0; i < values1.size(); i++) {
				expectedStat.add(values1.get(i));
			}
			for (int i = 0; i < values2.size(); i++) {
				expectedStat.add(values2.get(i));
			}

			List<Stat> stats = new ArrayList<>();
			stats.add(stat1);
			stats.add(stat2);

			Stat combinedStat = MutableStat.combineStatsCollection(stats);

			showSimilar(expectedStat, combinedStat);
		}
	}

	/**
	 * Tests {@link MutableStat#add(double)}
	 */
	@Test
	@UnitTestMethod(name = "add", args = { double.class })
	public void testAdd() {
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(4090068094278660804L);

		for (int i = 0; i < 1000; i++) {
			MutableStat mutableStat = new MutableStat();
			List<Double> values = new ArrayList<>();
			int n = randomGenerator.nextInt(30) + 1;
			for (int j = 0; j < n; j++) {
				values.add(randomGenerator.nextDouble() * 100 - 50);
			}
			for (Double value : values) {
				mutableStat.add(value);
			}

			// test max
			Optional<Double> actualMaxOptional = mutableStat.getMax();
			assertTrue(actualMaxOptional.isPresent());
			double actualMax = actualMaxOptional.get();
			double expectedMax = Double.NEGATIVE_INFINITY;
			for (Double value : values) {
				expectedMax = FastMath.max(expectedMax, value);
			}
			showSimilarValues(expectedMax, actualMax);

			// test size
			assertEquals(values.size(), mutableStat.size());

			// test min
			Optional<Double> actualMinOptional = mutableStat.getMin();
			assertTrue(actualMinOptional.isPresent());
			double actualMin = actualMinOptional.get();
			double expectedMin = Double.POSITIVE_INFINITY;
			for (Double value : values) {
				expectedMin = FastMath.min(expectedMin, value);
			}
			showSimilarValues(expectedMin, actualMin);

			// test mean
			Optional<Double> actualMeanOptional = mutableStat.getMean();
			assertTrue(actualMeanOptional.isPresent());
			double actualMean = actualMeanOptional.get();
			double expectedMean = 0;
			for (Double value : values) {
				expectedMean += value;
			}
			expectedMean /= values.size();
			showSimilarValues(expectedMean, actualMean);

			// test variance
			double expectedVariance = 0;
			for (Double value : values) {
				expectedVariance += (value - expectedMean) * (value - expectedMean);
			}
			expectedVariance /= values.size();

			Optional<Double> optionalVariance = mutableStat.getVariance();
			assertTrue(optionalVariance.isPresent());
			double actualVariance = optionalVariance.get();
			showSimilarValues(expectedVariance, actualVariance);

			// test standard deviation
			double expectedStandardDeviation = FastMath.sqrt(expectedVariance);
			Optional<Double> optionalStandardDeviation = mutableStat.getStandardDeviation();
			assertTrue(optionalStandardDeviation.isPresent());
			Double actualStandarDeviation = optionalStandardDeviation.get();
			showSimilarValues(expectedStandardDeviation, actualStandarDeviation);
		}

	}

	/**
	 * Tests {@link MutableStat#getMean()}
	 */
	@Test
	@UnitTestMethod(name = "getMean", args = {})
	public void testGetMean() {
		// covered by testAdd()
	}

	/**
	 * Tests {@link MutableStat#getMax()}
	 */
	@Test
	@UnitTestMethod(name = "getMax", args = {})
	public void testGetMax() {
		// covered by testAdd()
	}

	/**
	 * Tests {@link MutableStat#getMin()}
	 */
	@Test
	@UnitTestMethod(name = "getMin", args = {})
	public void testGetMin() {
		// covered by testAdd()
	}

	/**
	 * Tests {@link MutableStat#getStandardDeviation()}
	 */
	@Test
	@UnitTestMethod(name = "getStandardDeviation", args = {})
	public void testGetStandardDeviation() {
		// covered by testAdd()
	}

	/**
	 * Tests {@link MutableStat#getVariance()}
	 */
	@Test
	@UnitTestMethod(name = "getVariance", args = {})
	public void testGetVariance() {
		// covered by testAdd()
	}

	/**
	 * Tests {@link MutableStat#MutableStat()}
	 */
	@Test
	@UnitTestConstructor(args = {})
	public void testConstructor() {
		MutableStat mutableStat = new MutableStat();
		assertEquals(0, mutableStat.size());
		assertFalse(mutableStat.getMax().isPresent());
		assertFalse(mutableStat.getMin().isPresent());
		assertFalse(mutableStat.getMean().isPresent());
		assertFalse(mutableStat.getStandardDeviation().isPresent());
		assertFalse(mutableStat.getVariance().isPresent());
	}

	/**
	 * Tests {@link MutableStat#clear()}
	 */
	@Test
	@UnitTestMethod(name = "clear", args = {})
	public void testClear() {
		MutableStat mutableStat = new MutableStat();
		mutableStat.add(1);
		mutableStat.add(2);

		assertTrue(mutableStat.size() > 0);
		assertTrue(mutableStat.getMax().isPresent());
		assertTrue(mutableStat.getMin().isPresent());
		assertTrue(mutableStat.getMean().isPresent());
		assertTrue(mutableStat.getStandardDeviation().isPresent());
		assertTrue(mutableStat.getVariance().isPresent());

		mutableStat.clear();

		assertEquals(0, mutableStat.size());
		assertFalse(mutableStat.getMax().isPresent());
		assertFalse(mutableStat.getMin().isPresent());
		assertFalse(mutableStat.getMean().isPresent());
		assertFalse(mutableStat.getStandardDeviation().isPresent());
		assertFalse(mutableStat.getVariance().isPresent());

	}

	/**
	 * Tests {@link MutableStat#size()}
	 */
	@Test
	@UnitTestMethod(name = "size", args = {})
	public void testSize() {
		// covered by testAdd()
	}

	/**
	 * Tests {@link MutableStat#toString()}
	 */
	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		MutableStat mutableStat = new MutableStat();
		mutableStat.add(1);
		mutableStat.add(2);
		String expected = "MutableStat [getMean()=Optional[1.5], getVariance()=Optional[0.25], getStandardDeviation()=Optional[0.5], getMax()=Optional[2.0], getMin()=Optional[1.0], size()=2]";

		String actual = mutableStat.toString();

		assertEquals(expected, actual);

	}

}
