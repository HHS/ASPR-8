package util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import annotations.UnitTest;
import annotations.UnitTestMethod;
import util.stats.ImmutableStat;

/**
 * Test class for {@link ImmutableStat}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = ImmutableStat.class)
public class AT_ImmutableStat {


	/**
	 * Tests {@link ImmutableStat#builder()}
	 */
	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7777875192439812269L);

		ImmutableStat immutableStat = ImmutableStat	.builder().setMax(0)//
													.setMin(0)//
													.setMean(0)//
													.setSize(1)//
													.setVariance(0)//
													.build();//

		assertNotNull(immutableStat);

		/*
		 * if the size is negative
		 */
		assertThrows(RuntimeException.class, () -> {
			ImmutableStat	.builder().setMax(0)//
							.setMin(0)//
							.setMean(0)//
							.setSize(-1)//
							.setVariance(0)//
							.build();//

		});

		/*
		 * if the size value is one and the min mean and max are not equal
		 */
		assertThrows(RuntimeException.class, () -> {
			ImmutableStat	.builder().setMax(1)//
							.setMin(0)//
							.setMean(0)//
							.setSize(1)//
							.setVariance(0)//
							.build();//

		});

		/* if the size value is one and the variance is not zero */
		assertThrows(RuntimeException.class, () -> {
			ImmutableStat	.builder().setMax(0)//
							.setMin(0)//
							.setMean(0)//
							.setSize(1)//
							.setVariance(1)//
							.build();//

		});

		/*
		 * if the size value is greater than one and the min exceeds the max
		 */
		assertThrows(RuntimeException.class, () -> {
			ImmutableStat	.builder().setMax(0)//
							.setMin(1)//
							.setMean(0)//
							.setSize(2)//
							.setVariance(0)//
							.build();//

		});

		/*
		 * if the size value is greater than one and the min exceeds the mean
		 */
		assertThrows(RuntimeException.class, () -> {
			ImmutableStat	.builder().setMax(2)//
							.setMin(1)//
							.setMean(0)//
							.setSize(2)//
							.setVariance(0)//
							.build();//

		});

		/*
		 * if the size value is greater than one and the mean exceeds the max
		 */
		assertThrows(RuntimeException.class, () -> {
			ImmutableStat	.builder().setMax(2)//
							.setMin(1)//
							.setMean(3)//
							.setSize(2)//
							.setVariance(0)//
							.build();//

		});

		/*
		 * if the size value is greater than one and the variance is negative
		 */
		assertThrows(RuntimeException.class, () -> {
			ImmutableStat	.builder().setMax(0)//
							.setMin(0)//
							.setMean(0)//
							.setSize(2)//
							.setVariance(-1)//
							.build();//

		});

		for (int i = 0; i < 100; i++) {
			double max = randomGenerator.nextDouble() * 100;
			double min = randomGenerator.nextDouble() * max;
			double mean = randomGenerator.nextDouble() * (max - min) + min;
			int size = randomGenerator.nextInt(30) + 2;
			double variance = (randomGenerator.nextDouble() * 0.1 + 0.5) * (max - min) + min;

			immutableStat = ImmutableStat	.builder()//
											.setMax(max)//
											.setMin(min)//
											.setMean(mean)//
											.setSize(size)//
											.setVariance(variance)//
											.build();//

			assertEquals(max, immutableStat.getMax().get(), 0);
			assertEquals(min, immutableStat.getMin().get(), 0);
			assertEquals(mean, immutableStat.getMean().get(), 0);
			assertEquals(variance, immutableStat.getVariance().get(), 0);
			assertEquals(size, immutableStat.size());
		}

	}

	/**
	 * Tests {@link ImmutableStat#getMean()}
	 */
	@Test
	@UnitTestMethod(name = "getMean", args = {})
	public void testGetMean() {
		// covered by testBuilder()
	}

	/**
	 * Tests {@link ImmutableStat#getMax()}
	 */
	@Test
	@UnitTestMethod(name = "getMax", args = {})
	public void testGetMax() {
		// covered by testBuilder()
	}

	/**
	 * Tests {@link ImmutableStat#getMin()}
	 */
	@Test
	@UnitTestMethod(name = "getMin", args = {})
	public void testGetMin() {
		// covered by testBuilder()
	}

	/**
	 * Tests {@link ImmutableStat#getStandardDeviation()}
	 */
	@Test
	@UnitTestMethod(name = "getStandardDeviation", args = {})
	public void testGetStandardDeviation() {
		// covered by testBuilder()
	}

	/**
	 * Tests {@link ImmutableStat#getVariance()}
	 */
	@Test
	@UnitTestMethod(name = "getVariance", args = {})
	public void testGetVariance() {
		// covered by testBuilder()
	}

	/**
	 * Tests {@link ImmutableStat#size()}
	 */
	@Test
	@UnitTestMethod(name = "size", args = {})
	public void testSize() {
		// covered by testBuilder()
	}

	/**
	 * Tests {@link ImmutableStat#toString()}
	 */
	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		ImmutableStat immutableStat = ImmutableStat.builder().setMax(3.4).setMin(1.1).setMean(1.9).setSize(20).setVariance(0.5).build();

		String expected = "ImmutableStat [mean=1.9, variance=0.5, standardDeviation=0.7071067811865476, max=3.4, min=1.1, size=20]";
		String actual = immutableStat.toString();
		assertEquals(expected, actual);
	}

}
