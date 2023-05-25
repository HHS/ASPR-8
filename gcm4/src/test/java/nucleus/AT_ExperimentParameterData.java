package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import util.annotations.UnitTag;
import util.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

public class AT_ExperimentParameterData {

	@Test
	@UnitTestMethod(target = ExperimentParameterData.Builder.class, name = "build", args = {})
	public void testBuild() {
		ExperimentParameterData experimentParameterData = ExperimentParameterData.builder().build();
		assertNotNull(experimentParameterData);

		// default values are covered by the other tests
	}

	@Test
	@UnitTestMethod(target = ExperimentParameterData.Builder.class, name = "setExperimentProgressLog", args = { Path.class }, tags = { UnitTag.MANUAL })
	public void testSetExperimentProgressLog() {
		// if no path is specified
		ExperimentParameterData experimentParameterData = ExperimentParameterData	.builder()//
																					.build();

		Optional<Path> optional = experimentParameterData.getExperimentProgressLogPath();
		assertFalse(optional.isPresent());

		// if a path is specified
		for (int i = 0; i < 10; i++) {
			String pathName = "somePath_" + i;
			Path expectedPath = Paths.get(pathName);

			experimentParameterData = ExperimentParameterData	.builder()//
																.setExperimentProgressLog(expectedPath)//
																.build();
			optional = experimentParameterData.getExperimentProgressLogPath();
			assertTrue(optional.isPresent());
			Path actualPath = optional.get();
			assertEquals(expectedPath, actualPath);
		}
	}

	@Test
	@UnitTestMethod(target = ExperimentParameterData.Builder.class, name = "setContinueFromProgressLog", args = { boolean.class }, tags = { UnitTag.MANUAL })
	public void testSetContinueFromProgressLog() {
		// if the policy is not set
		ExperimentParameterData experimentParameterData = ExperimentParameterData	.builder()//
																					.build();

		assertFalse(experimentParameterData.continueFromProgressLog());

		// if the policy is set to false
		experimentParameterData = ExperimentParameterData	.builder()//
															.setContinueFromProgressLog(false)//
															.build();

		assertFalse(experimentParameterData.continueFromProgressLog());

		// if the policy is set to true
		experimentParameterData = ExperimentParameterData	.builder()//
															.setContinueFromProgressLog(true)//
															.build();

		assertTrue(experimentParameterData.continueFromProgressLog());

	}

	@Test
	@UnitTestMethod(target = ExperimentParameterData.Builder.class, name = "setThreadCount", args = { int.class })
	public void testSetThreadCount() {

		fail();

	}

	@Test
	@UnitTestMethod(target = ExperimentParameterData.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(ExperimentParameterData.builder());
	}

	@Test
	@UnitTestMethod(target = ExperimentParameterData.Builder.class, name = "setHaltOnException", args = { boolean.class })
	public void testSetHaltOnException() {
		fail();
	}

	@Test
	@UnitTestMethod(target = ExperimentParameterData.Builder.class, name = "addExplicitScenarioId", args = { Integer.class })
	public void testAddExplicitScenarioId() {
		// if no scenarios are added
		ExperimentParameterData experimentParameterData = ExperimentParameterData	.builder()//
																					.build();
		Set<Integer> expectedScenarioIds = new LinkedHashSet<>();
		Set<Integer> actualScenarioIds = experimentParameterData.getExplicitScenarioIds();
		assertEquals(expectedScenarioIds, actualScenarioIds);

		// if some scenarios are added
		experimentParameterData = ExperimentParameterData	.builder()//
															.addExplicitScenarioId(12)//
															.addExplicitScenarioId(45)//
															.addExplicitScenarioId(12)//
															.addExplicitScenarioId(11)//
															.addExplicitScenarioId(-3)//
															.addExplicitScenarioId(45)//
															.build();

		expectedScenarioIds.add(-3);
		expectedScenarioIds.add(11);
		expectedScenarioIds.add(12);
		expectedScenarioIds.add(45);

		actualScenarioIds = experimentParameterData.getExplicitScenarioIds();
		assertEquals(expectedScenarioIds, actualScenarioIds);

	}

	@Test
	@UnitTestMethod(target = ExperimentParameterData.Builder.class, name = "setRecordState", args = { boolean.class })
	public void testSetRecordState() {
		fail();
	}

	@Test
	@UnitTestMethod(target = ExperimentParameterData.Builder.class, name = "setSimulationHaltTime", args = { double.class })
	public void testSetSimulationHaltTime() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4483845375009238350L);

		// if the scenario halt time is not set
		ExperimentParameterData experimentParameterData = ExperimentParameterData	.builder()//
																					.build();

		Optional<Double> optional = experimentParameterData.getSimulationHaltTime();
		assertTrue(optional.isEmpty());

		for (int i = 0; i < 10; i++) {
			double expectedTime = randomGenerator.nextDouble() - 0.5;
			experimentParameterData = ExperimentParameterData	.builder()//
																.setSimulationHaltTime(expectedTime)//
																.build();

			optional = experimentParameterData.getSimulationHaltTime();
			assertTrue(optional.isPresent());
			double actualTime = optional.get();

			assertEquals(expectedTime, actualTime);
		}

	}

	@Test
	@UnitTestMethod(target = ExperimentParameterData.class, name = "continueFromProgressLog", args = {})
	public void testContinueFromProgressLog() {
		// if the policy is not set
		ExperimentParameterData experimentParameterData = ExperimentParameterData	.builder()//
																					.build();

		assertFalse(experimentParameterData.continueFromProgressLog());

		// if the policy is set to false
		experimentParameterData = ExperimentParameterData	.builder()//
															.setContinueFromProgressLog(false)//
															.build();

		assertFalse(experimentParameterData.continueFromProgressLog());

		// if the policy is set to true
		experimentParameterData = ExperimentParameterData	.builder()//
															.setContinueFromProgressLog(true)//
															.build();

		assertTrue(experimentParameterData.continueFromProgressLog());
	}

	@Test
	@UnitTestMethod(target = ExperimentParameterData.class, name = "getExperimentProgressLogPath", args = {})
	public void testGetExperimentProgressLogPath() {
		// if no path is specified
		ExperimentParameterData experimentParameterData = ExperimentParameterData	.builder()//
																					.build();

		Optional<Path> optional = experimentParameterData.getExperimentProgressLogPath();
		assertFalse(optional.isPresent());

		// if a path is specified
		for (int i = 0; i < 10; i++) {
			String pathName = "somePath_" + i;
			Path expectedPath = Paths.get(pathName);

			experimentParameterData = ExperimentParameterData	.builder()//
																.setExperimentProgressLog(expectedPath)//
																.build();
			optional = experimentParameterData.getExperimentProgressLogPath();
			assertTrue(optional.isPresent());
			Path actualPath = optional.get();
			assertEquals(expectedPath, actualPath);
		}
	}

	@Test
	@UnitTestMethod(target = ExperimentParameterData.class, name = "getExplicitScenarioIds", args = {})
	public void testGetExplicitScenarioIds() {
		// if no scenarios are added
		ExperimentParameterData experimentParameterData = ExperimentParameterData	.builder()//
																					.build();
		Set<Integer> expectedScenarioIds = new LinkedHashSet<>();
		Set<Integer> actualScenarioIds = experimentParameterData.getExplicitScenarioIds();
		assertEquals(expectedScenarioIds, actualScenarioIds);

		// if some scenarios are added
		experimentParameterData = ExperimentParameterData	.builder()//
															.addExplicitScenarioId(12)//
															.addExplicitScenarioId(45)//
															.addExplicitScenarioId(12)//
															.addExplicitScenarioId(11)//
															.addExplicitScenarioId(-3)//
															.addExplicitScenarioId(45)//
															.build();

		expectedScenarioIds.add(-3);
		expectedScenarioIds.add(11);
		expectedScenarioIds.add(12);
		expectedScenarioIds.add(45);

		actualScenarioIds = experimentParameterData.getExplicitScenarioIds();
		assertEquals(expectedScenarioIds, actualScenarioIds);
	}

	@Test
	@UnitTestMethod(target = ExperimentParameterData.class, name = "getSimulationHaltTime", args = {})
	public void testGetSimulationHaltTime() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4483845375009238350L);

		// if the scenario halt time is not set
		ExperimentParameterData experimentParameterData = ExperimentParameterData	.builder()//
																					.build();

		Optional<Double> optional = experimentParameterData.getSimulationHaltTime();
		assertTrue(optional.isEmpty());

		for (int i = 0; i < 10; i++) {
			double expectedTime = randomGenerator.nextDouble() - 0.5;
			experimentParameterData = ExperimentParameterData	.builder()//
																.setSimulationHaltTime(expectedTime)//
																.build();

			optional = experimentParameterData.getSimulationHaltTime();
			assertTrue(optional.isPresent());
			double actualTime = optional.get();

			assertEquals(expectedTime, actualTime);
		}
	}

	@Test
	@UnitTestMethod(target = ExperimentParameterData.class, name = "getThreadCount", args = {})
	public void testGetThreadCount() {
		fail();
	}

	@Test
	@UnitTestMethod(target = ExperimentParameterData.class, name = "haltOnException", args = {})
	public void testHaltOnException() {
		fail();
	}

	@Test
	@UnitTestMethod(target = ExperimentParameterData.class, name = "stateRecordingIsScheduled", args = {})
	public void testStateRecordingIsScheduled() {
		fail();
	}

}
