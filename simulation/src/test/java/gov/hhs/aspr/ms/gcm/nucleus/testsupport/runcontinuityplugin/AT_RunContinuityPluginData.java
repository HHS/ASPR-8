package gov.hhs.aspr.ms.gcm.nucleus.testsupport.runcontinuityplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.runcontinuityplugin.RunContinuityPluginData.Builder;
import gov.hhs.aspr.ms.util.annotations.UnitTag;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_RunContinuityPluginData {

	@Test
	@UnitTestMethod(target = RunContinuityPluginData.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(RunContinuityPluginData.builder());
	}

	@Test
	@UnitTestMethod(target = RunContinuityPluginData.class, name = "getConsumers", args = {})
	public void testGetConsumers() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5172593709525641500L);
		List<Pair<Double, Consumer<ActorContext>>> expectedPairs = new ArrayList<>();

		for (int i = 0; i < 10; i++) {
			Pair<Double, Consumer<ActorContext>> pair = new Pair<Double, Consumer<ActorContext>>(
					randomGenerator.nextDouble(), (c) -> {
					});
			expectedPairs.add(pair);
		}

		RunContinuityPluginData.Builder builder = RunContinuityPluginData.builder();//
		for (Pair<Double, Consumer<ActorContext>> pair : expectedPairs) {
			builder.addContextConsumer(pair.getFirst(), pair.getSecond());
		}
		RunContinuityPluginData runContinuityPluginData = builder.build();

		assertEquals(expectedPairs, runContinuityPluginData.getConsumers());

	}

	@Test
	@UnitTestMethod(target = RunContinuityPluginData.class, name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5885906221170851986L);

		for (int i = 0; i < 10; i++) {

			RunContinuityPluginData.Builder builder = RunContinuityPluginData.builder();//

			for (int j = 0; j < i; j++) {
				builder.addContextConsumer(randomGenerator.nextDouble(), (c) -> {
				});
			}
			RunContinuityPluginData runContinuityPluginData = builder.build();

			// show that the returned clone builder will build an identical instance if no
			// mutations are made
			RunContinuityPluginData.Builder cloneBuilder = runContinuityPluginData.getCloneBuilder();
			assertNotNull(cloneBuilder);
			assertEquals(runContinuityPluginData, cloneBuilder.build());

			// show that the clone builder builds a distinct instance if any mutation is
			// made

			// addContextConsumer
			cloneBuilder = runContinuityPluginData.getCloneBuilder();
			cloneBuilder.addContextConsumer(2.5, (c) -> {
			});
			assertNotEquals(runContinuityPluginData, cloneBuilder.build());

		}
	}

	@Test
	@UnitTestMethod(target = RunContinuityPluginData.class, name = "allPlansComplete", args = {})
	public void testAllPlansComplete() {
		assertTrue(RunContinuityPluginData.builder()//
				.build().allPlansComplete());

		assertFalse(RunContinuityPluginData.builder()//
				.addContextConsumer(1.0, (c) -> {
				})//
				.build().allPlansComplete());

	}

	@Test
	@UnitTestMethod(target = RunContinuityPluginData.Builder.class, name = "build", args = {}, tags = {
			UnitTag.LOCAL_PROXY })
	public void testBuild() {
		// covered by other tests
	}

	@Test
	@UnitTestMethod(target = RunContinuityPluginData.Builder.class, name = "addContextConsumer", args = { double.class,
			Consumer.class })
	public void testAddContextConsumer() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7652825578007143462L);
		List<Pair<Double, Consumer<ActorContext>>> expectedPairs = new ArrayList<>();

		for (int i = 0; i < 10; i++) {
			Pair<Double, Consumer<ActorContext>> pair = new Pair<Double, Consumer<ActorContext>>(
					randomGenerator.nextDouble(), (c) -> {
					});
			expectedPairs.add(pair);
		}

		RunContinuityPluginData.Builder builder = RunContinuityPluginData.builder();//
		for (Pair<Double, Consumer<ActorContext>> pair : expectedPairs) {
			builder.addContextConsumer(pair.getFirst(), pair.getSecond());
		}
		RunContinuityPluginData runContinuityPluginData = builder.build();

		assertEquals(expectedPairs, runContinuityPluginData.getConsumers());
	}

	private static List<Consumer<ActorContext>> staticConsumers = new ArrayList<>();

	/*
	 * Thread safe means of creating a list of 20 consumers. Comparing consumers for
	 * equality is implemented via == comparison in Java, so we need to create a
	 * static set of them. 
	 */
	private static List<Consumer<ActorContext>> getStaticConsumers() {
		synchronized (staticConsumers) {
			if (staticConsumers.isEmpty()) {
				staticConsumers = new ArrayList<>();
				for (int i = 0; i < 20; i++) {
					staticConsumers.add((c) -> {
					});
				}
			}
		}
		return staticConsumers;
	}

	private static RunContinuityPluginData getRandomRunContinuityPluginData(long seed) {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		Builder builder = RunContinuityPluginData.builder();
		for (Consumer<ActorContext> consumer : getStaticConsumers()) {
			if (randomGenerator.nextBoolean()) {
				builder.addContextConsumer(randomGenerator.nextDouble(), consumer);
			}
		}
		return builder.build();
	}

	@Test
	@UnitTestMethod(target = RunContinuityPluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2949401509033289976L);

		// is never equal to null
		for (int i = 0; i < 30; i++) {
			RunContinuityPluginData runContinuityPluginData = getRandomRunContinuityPluginData(
					randomGenerator.nextLong());
			assertFalse(runContinuityPluginData.equals(null));
		}

		// is never equal to null something else
		for (int i = 0; i < 30; i++) {
			RunContinuityPluginData runContinuityPluginData = getRandomRunContinuityPluginData(
					randomGenerator.nextLong());
			assertFalse(runContinuityPluginData.equals(new Object()));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			RunContinuityPluginData runContinuityPluginData = getRandomRunContinuityPluginData(
					randomGenerator.nextLong());
			assertTrue(runContinuityPluginData.equals(runContinuityPluginData));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			RunContinuityPluginData runContinuityPluginData1 = getRandomRunContinuityPluginData(seed);
			RunContinuityPluginData runContinuityPluginData2 = getRandomRunContinuityPluginData(seed);

			for (int j = 0; j < 5; j++) {
				assertTrue(runContinuityPluginData1.equals(runContinuityPluginData2));
				assertTrue(runContinuityPluginData2.equals(runContinuityPluginData1));
			}
		}

		// different inputs yield unequal objects -- extremely low probability of collision
		Set<RunContinuityPluginData> runContinuityPluginDatas = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			RunContinuityPluginData runContinuityPluginData = getRandomRunContinuityPluginData(
					randomGenerator.nextLong());
			runContinuityPluginDatas.add(runContinuityPluginData);
		}
		assertEquals(100, runContinuityPluginDatas.size());
	}

	@Test
	@UnitTestMethod(target = RunContinuityPluginData.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2596156632230447726L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			RunContinuityPluginData runContinuityPluginData1 = getRandomRunContinuityPluginData(seed);
			RunContinuityPluginData runContinuityPluginData2 = getRandomRunContinuityPluginData(seed);
			assertEquals(runContinuityPluginData1, runContinuityPluginData2);
			assertEquals(runContinuityPluginData1.hashCode(), runContinuityPluginData2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			RunContinuityPluginData runContinuityPluginData = getRandomRunContinuityPluginData(randomGenerator.nextLong());
			hashCodes.add(runContinuityPluginData.hashCode());
		}
		assertEquals(100, hashCodes.size());
	}

}
