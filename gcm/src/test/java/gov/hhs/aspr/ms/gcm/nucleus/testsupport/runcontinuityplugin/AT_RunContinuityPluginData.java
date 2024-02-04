package gov.hhs.aspr.ms.gcm.nucleus.testsupport.runcontinuityplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
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

			RunContinuityPluginData cloneRunContinuityPluginData = //
					(RunContinuityPluginData) runContinuityPluginData.getCloneBuilder().build();

			assertEquals(runContinuityPluginData.getConsumers(), cloneRunContinuityPluginData.getConsumers());

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

}
