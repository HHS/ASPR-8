package util.stats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

/**
 * Test class for {@link ImmutableStat}
 * 
 * @author Shawn Hatch
 *
 */
public class AT_ImmutableStat {

	@Test
	@UnitTestMethod(target = ImmutableStat.Builder.class, name = "build", args = {})
	public void testBuild() {
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

	@Test
	@UnitTestMethod(target = ImmutableStat.Builder.class, name = "setMax", args = { double.class })
	public void testSetMax() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1623204821125929088L);

		for (int i = 0; i < 100; i++) {
			double max = randomGenerator.nextDouble();
			ImmutableStat immutableStat = ImmutableStat	.builder()//
														.setMax(max)//
														.setMin(0)//
														.setMean(0)//
														.setSize(2)//
														.setVariance(0)//
														.build();//

			assertEquals(max, immutableStat.getMax().get(), 0);
		}

	}

	@Test
	@UnitTestMethod(target = ImmutableStat.Builder.class, name = "setMean", args = { double.class })
	public void testSetMean() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5546913373263258838L);

		for (int i = 0; i < 100; i++) {
			double mean = randomGenerator.nextDouble();
			ImmutableStat immutableStat = ImmutableStat	.builder()//
														.setMax(mean + 1)//
														.setMin(mean - 1)//
														.setMean(mean)//
														.setSize(2)//
														.setVariance(0)//
														.build();//

			assertEquals(mean, immutableStat.getMean().get(), 0);
		}
	}

	@Test
	@UnitTestMethod(target = ImmutableStat.Builder.class, name = "setMin", args = { double.class })
	public void testSetMin() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9187585184905019274L);

		for (int i = 0; i < 100; i++) {
			double min = randomGenerator.nextDouble();
			ImmutableStat immutableStat = ImmutableStat	.builder()//
														.setMax(min + 1)//
														.setMin(min)//
														.setMean(min + 0.5)//
														.setSize(2)//
														.setVariance(0)//
														.build();//

			assertEquals(min, immutableStat.getMin().get(), 0);
		}

	}

	@Test
	@UnitTestMethod(target = ImmutableStat.Builder.class, name = "setSize", args = { int.class })
	public void testSetSize() {

		for (int i = 0; i < 100; i++) {
			int size = i + 1;
			ImmutableStat immutableStat = ImmutableStat	.builder()//
														.setMax(0)//
														.setMin(0)//
														.setMean(0)//
														.setSize(size)//
														.setVariance(0)//
														.build();//

			assertEquals(size, immutableStat.size(), 0);
		}
	}

	@Test
	@UnitTestMethod(target = ImmutableStat.Builder.class, name = "setVariance", args = { double.class })
	public void testSetVariance() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6920383800120778067L);

		for (int i = 0; i < 100; i++) {
			double variance = randomGenerator.nextDouble();
			ImmutableStat immutableStat = ImmutableStat	.builder()//
														.setMax(0)//
														.setMin(0)//
														.setMean(0)//
														.setSize(2)//
														.setVariance(variance)//
														.build();//

			assertEquals(variance, immutableStat.getVariance().get(), 0);
		}
	}

	@Test
	@UnitTestMethod(target = ImmutableStat.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(ImmutableStat.builder());
	}

	@Test
	@UnitTestMethod(target = ImmutableStat.class, name = "getMean", args = {})
	public void testGetMean() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6876193335436069229L);

		for (int i = 0; i < 100; i++) {
			double mean = randomGenerator.nextDouble();
			ImmutableStat immutableStat = ImmutableStat	.builder()//
														.setMax(mean + 1)//
														.setMin(mean - 1)//
														.setMean(mean)//
														.setSize(2)//
														.setVariance(0)//
														.build();//

			assertEquals(mean, immutableStat.getMean().get(), 0);
		}
	}

	@Test
	@UnitTestMethod(target = ImmutableStat.class, name = "getMax", args = {})
	public void testGetMax() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1155316418087853792L);

		for (int i = 0; i < 100; i++) {
			double max = randomGenerator.nextDouble();
			ImmutableStat immutableStat = ImmutableStat	.builder()//
														.setMax(max)//
														.setMin(0)//
														.setMean(0)//
														.setSize(2)//
														.setVariance(0)//
														.build();//

			assertEquals(max, immutableStat.getMax().get(), 0);
		}
	}

	@Test
	@UnitTestMethod(target = ImmutableStat.class, name = "getMin", args = {})
	public void testGetMin() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3698755887947285191L);

		for (int i = 0; i < 100; i++) {
			double min = randomGenerator.nextDouble();
			ImmutableStat immutableStat = ImmutableStat	.builder()//
														.setMax(min + 1)//
														.setMin(min)//
														.setMean(min + 0.5)//
														.setSize(2)//
														.setVariance(0)//
														.build();//

			assertEquals(min, immutableStat.getMin().get(), 0);
		}

	}

	@Test
	@UnitTestMethod(target = ImmutableStat.class, name = "getStandardDeviation", args = {})
	public void testGetStandardDeviation() {
		// covered by testBuilder()
	}

	@Test
	@UnitTestMethod(target = ImmutableStat.class, name = "getVariance", args = {})
	public void testGetVariance() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6224775885223191513L);

		for (int i = 0; i < 100; i++) {
			double variance = randomGenerator.nextDouble();
			ImmutableStat immutableStat = ImmutableStat	.builder()//
														.setMax(0)//
														.setMin(0)//
														.setMean(0)//
														.setSize(2)//
														.setVariance(variance)//
														.build();//

			assertEquals(variance, immutableStat.getVariance().get(), 0);
		}
	}

	@Test
	@UnitTestMethod(target = ImmutableStat.class, name = "size", args = {})
	public void testSize() {
		for (int i = 0; i < 100; i++) {
			int size = i + 1;
			ImmutableStat immutableStat = ImmutableStat	.builder()//
														.setMax(0)//
														.setMin(0)//
														.setMean(0)//
														.setSize(size)//
														.setVariance(0)//
														.build();//

			assertEquals(size, immutableStat.size(), 0);
		}
	}

	@Test
	@UnitTestMethod(target = ImmutableStat.class, name = "toString", args = {})
	public void testToString() {
		ImmutableStat immutableStat = ImmutableStat.builder().setMax(3.4).setMin(1.1).setMean(1.9).setSize(20).setVariance(0.5).build();

		String expected = "ImmutableStat [mean=1.9, variance=0.5, standardDeviation=0.7071067811865476, max=3.4, min=1.1, size=20]";
		String actual = immutableStat.toString();
		assertEquals(expected, actual);
	}

}
