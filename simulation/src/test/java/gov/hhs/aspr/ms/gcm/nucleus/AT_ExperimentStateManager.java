package gov.hhs.aspr.ms.gcm.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.wrappers.MutableBoolean;
import gov.hhs.aspr.ms.util.wrappers.MutableInteger;

public class AT_ExperimentStateManager {

	private final static Path PROGRESS_LOG_PATH = Paths.get("src/test/resources/nucleus/progress_log.txt");
	private final static Path NONEXISTENT_PROGRESS_LOG_PATH = Paths
			.get("src/test/resources/nucleus/nonexistentfile.txt");
	private final static Path NONFILE_PROGRESS_LOG_PATH = Paths.get("src/test/resources/nucleus");

	/*
	 * Tests that the set of scenario ids found in the progress log matches the
	 * given expected scenario ids.
	 */
	private static void testProgressLogContents(Set<Integer> expectedScenarioIds) {
		Set<Integer> actualScenarioIds = new LinkedHashSet<>();
		try {
			List<String> lines = Files.readAllLines(PROGRESS_LOG_PATH);
			for (int i = 1; i < lines.size(); i++) {
				int scenarioId = Integer.parseInt(lines.get(i));
				actualScenarioIds.add(scenarioId);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		assertEquals(expectedScenarioIds, actualScenarioIds);
	}

	/*
	 * Clears the progress log in the test resources folder
	 */
	private static void clearProgressLog() {
		try {

			/*
			 * Remove the old file and write to the file the header and any retained lines
			 * from the previous execution.
			 */

			Files.deleteIfExists(PROGRESS_LOG_PATH);
			Files.createFile(PROGRESS_LOG_PATH);
			// CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
			// OutputStream out = Files.newOutputStream(path, StandardOpenOption.CREATE);
			// writer = new BufferedWriter(new OutputStreamWriter(out, encoder));

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * Builds the progress log from the expected scenario ids without any meta data
	 */
	private static void initializeProgressLog(Set<Integer> expectedScenarioIds) {
		try {
			List<String> scenarioIds = new ArrayList<>();
			scenarioIds.add("scenario");
			for (Integer scenarioId : expectedScenarioIds) {
				scenarioIds.add(Integer.toString(scenarioId));
			}
			Files.write(PROGRESS_LOG_PATH, scenarioIds);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * Builds the progress log from the expected scenario ids with two columns of
	 * meta data, A and B
	 */
	private static void initializeProgressLogWithMetaData(Set<Integer> expectedScenarioIds) {
		try {
			List<String> lines = new ArrayList<>();
			StringBuilder sb = new StringBuilder();
			sb.append("scenario");
			sb.append("\t");
			sb.append("A");
			sb.append("\t");
			sb.append("B");
			lines.add(sb.toString());

			for (Integer scenarioId : expectedScenarioIds) {
				sb = new StringBuilder();
				sb.append(scenarioId);
				sb.append("\t");
				sb.append("1");
				sb.append("\t");
				sb.append("2");
				lines.add(sb.toString());
			}
			Files.write(PROGRESS_LOG_PATH, lines);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@BeforeEach
	public void beforeEach() {
		clearProgressLog();
	}

	@AfterAll
	public static void afterAll() {
		clearProgressLog();
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(ExperimentStateManager.builder());
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.class, name = "closeExperiment", args = {})
	public void testCloseExperiment() {

		MutableInteger consumerActivationCounter = new MutableInteger();

		// precondition test: if the experiment is not currently open, it cannot be
		// closed.
		ContractException contractException = assertThrows(ContractException.class,
				() -> ExperimentStateManager.builder().build().closeExperiment());

		// post condition tests
		assertEquals(NucleusError.UNCLOSABLE_EXPERIMENT, contractException.getErrorType());
		ExperimentStateManager experimentStateManager = ExperimentStateManager//
				.builder()//
				.setScenarioCount(5)//
				.setScenarioProgressLogFile(PROGRESS_LOG_PATH).addExperimentContextConsumer((c1) -> {
					c1.subscribeToExperimentClose((c2 -> consumerActivationCounter.increment()));
				})//
				.addExperimentContextConsumer((c1) -> {
					c1.subscribeToExperimentClose((c2 -> consumerActivationCounter.increment(2)));
				})//
				.addExperimentContextConsumer((c1) -> {
					c1.subscribeToExperimentClose((c2 -> consumerActivationCounter.increment(4)));
				})//
				.build();
		experimentStateManager.openExperiment();

		experimentStateManager.closeScenarioAsSuccess(0);
		experimentStateManager.closeScenarioAsSuccess(1);
		experimentStateManager.closeScenarioAsSuccess(2);

		experimentStateManager.closeExperiment();

		// show that each experiment context consumer that subscribed to close got the
		// notification
		assertEquals(7, consumerActivationCounter.getValue());

		// show that the progress log closed and thus flushed out the three completed
		// scenarios
		Set<Integer> expectedScenarioIds = new LinkedHashSet<>();
		expectedScenarioIds.add(0);
		expectedScenarioIds.add(1);
		expectedScenarioIds.add(2);
		testProgressLogContents(expectedScenarioIds);

	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.class, name = "closeScenarioAsFailure", args = { Integer.class,
			Exception.class })
	public void testCloseScenarioAsFailure() {
		Set<Integer> observedClosedScenarios = new LinkedHashSet<>();

		// precondition test: if the scenario id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			ExperimentStateManager experimentStateManager = ExperimentStateManager.builder().setScenarioCount(2)
					.build();//
			experimentStateManager.closeScenarioAsFailure(null, null);
		});
		assertEquals(NucleusError.NULL_SCENARIO_ID, contractException.getErrorType());

		// precondition test: if the scenario id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			ExperimentStateManager experimentStateManager = ExperimentStateManager.builder().setScenarioCount(2)
					.build();//
			experimentStateManager.closeScenarioAsFailure(2, null);
		});
		assertEquals(NucleusError.UNKNOWN_SCENARIO_ID, contractException.getErrorType());

		// post condition tests
		ExperimentStateManager experimentStateManager = ExperimentStateManager//
				.builder()//
				.setScenarioCount(5)//
				.setScenarioProgressLogFile(PROGRESS_LOG_PATH)//
				.addExperimentContextConsumer((c1) -> {
					c1.subscribeToSimulationClose((c2, s) -> observedClosedScenarios.add(s));
				})//
				.build();
		experimentStateManager.openExperiment();

		// close all scenarios, with scenarios 0 and 2 failing
		experimentStateManager.closeScenarioAsFailure(0, null);
		experimentStateManager.closeScenarioAsSuccess(1);
		experimentStateManager.closeScenarioAsFailure(2, new ContractException(NucleusError.ACCESS_VIOLATION));
		experimentStateManager.closeScenarioAsSuccess(3);
		experimentStateManager.closeScenarioAsSuccess(4);

		experimentStateManager.closeExperiment();

		// show that the experiment context consumer that subscribed to simulation close
		// got the
		// notification
		Set<Integer> expectedClosedScenarios = new LinkedHashSet<>();
		expectedClosedScenarios.add(0);
		expectedClosedScenarios.add(1);
		expectedClosedScenarios.add(2);
		expectedClosedScenarios.add(3);
		expectedClosedScenarios.add(4);
		assertEquals(expectedClosedScenarios, observedClosedScenarios);

		// show that scenario 0 failed and has no known cause
		Optional<ScenarioStatus> optionalScenarioStatus = experimentStateManager.getScenarioStatus(0);
		assertTrue(optionalScenarioStatus.isPresent());
		assertEquals(ScenarioStatus.FAILED, optionalScenarioStatus.get());
		Optional<Exception> optionalFailureCause = experimentStateManager.getScenarioFailureCause(0);
		assertFalse(optionalFailureCause.isPresent());

		// show that scenario 2 failed and has known cause
		// ContractException(NucleusError.ACCESS_VIOLATION)
		optionalScenarioStatus = experimentStateManager.getScenarioStatus(2);
		assertTrue(optionalScenarioStatus.isPresent());
		assertEquals(ScenarioStatus.FAILED, optionalScenarioStatus.get());
		optionalFailureCause = experimentStateManager.getScenarioFailureCause(2);
		assertTrue(optionalFailureCause.isPresent());
		Exception exception = optionalFailureCause.get();
		assertTrue(exception instanceof ContractException);
		contractException = (ContractException) exception;
		assertEquals(NucleusError.ACCESS_VIOLATION, contractException.getErrorType());

		// show that the progress log does not contain the failed scenarios
		Set<Integer> expectedScenarioIds = new LinkedHashSet<>();
		expectedScenarioIds.add(1);
		expectedScenarioIds.add(3);
		expectedScenarioIds.add(4);
		testProgressLogContents(expectedScenarioIds);
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.class, name = "closeScenarioAsSuccess", args = { Integer.class })
	public void testCloseScenarioAsSuccess() {
		Set<Integer> observedClosedScenarios = new LinkedHashSet<>();

		// precondition test: if the scenario id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			ExperimentStateManager experimentStateManager = ExperimentStateManager.builder().setScenarioCount(2)
					.build();//
			experimentStateManager.closeScenarioAsSuccess(null);
		});
		assertEquals(NucleusError.NULL_SCENARIO_ID, contractException.getErrorType());

		// precondition test: if the scenario id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			ExperimentStateManager experimentStateManager = ExperimentStateManager.builder().setScenarioCount(2)
					.build();//
			experimentStateManager.closeScenarioAsSuccess(2);
		});
		assertEquals(NucleusError.UNKNOWN_SCENARIO_ID, contractException.getErrorType());

		// post condition tests
		ExperimentStateManager experimentStateManager = ExperimentStateManager//
				.builder()//
				.setScenarioCount(5)//
				.setScenarioProgressLogFile(PROGRESS_LOG_PATH)//
				.addExperimentContextConsumer((c1) -> {
					c1.subscribeToSimulationClose((c2, s) -> observedClosedScenarios.add(s));
				})//
				.build();
		experimentStateManager.openExperiment();

		// close all scenarios, with scenarios 0 and 2 failing
		experimentStateManager.closeScenarioAsFailure(0, null);
		experimentStateManager.closeScenarioAsSuccess(1);
		experimentStateManager.closeScenarioAsFailure(2, new ContractException(NucleusError.ACCESS_VIOLATION));
		experimentStateManager.closeScenarioAsSuccess(3);
		experimentStateManager.closeScenarioAsSuccess(4);

		experimentStateManager.closeExperiment();

		/*
		 * show that the experiment context consumer that subscribed to simulation close
		 * got the notification
		 */
		Set<Integer> expectedClosedScenarios = new LinkedHashSet<>();
		expectedClosedScenarios.add(0);
		expectedClosedScenarios.add(1);
		expectedClosedScenarios.add(2);
		expectedClosedScenarios.add(3);
		expectedClosedScenarios.add(4);
		assertEquals(expectedClosedScenarios, observedClosedScenarios);

		// show that scenarios 1,3, and 4 succeeded.
		for (int scenarioId : new int[] { 1, 3, 4 }) {
			Optional<ScenarioStatus> optionalScenarioStatus = experimentStateManager.getScenarioStatus(scenarioId);
			assertTrue(optionalScenarioStatus.isPresent());
			assertEquals(ScenarioStatus.SUCCEDED, optionalScenarioStatus.get());
			Optional<Exception> optionalFailureCause = experimentStateManager.getScenarioFailureCause(scenarioId);
			assertFalse(optionalFailureCause.isPresent());
		}
		// show that the progress log contain only the successful scenarios
		Set<Integer> expectedScenarioIds = new LinkedHashSet<>();
		expectedScenarioIds.add(1);
		expectedScenarioIds.add(3);
		expectedScenarioIds.add(4);
		testProgressLogContents(expectedScenarioIds);
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.class, name = "getElapsedSeconds", args = {})
	public void testGetElapsedSeconds() {
		ExperimentStateManager experimentStateManager = ExperimentStateManager//
				.builder()//
				.build();
		experimentStateManager.openExperiment();
		experimentStateManager.closeExperiment();
		assertTrue(experimentStateManager.getElapsedSeconds() > 0);
		assertTrue(experimentStateManager.getElapsedSeconds() < 0.01);
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.class, name = "getExperimentMetaData", args = {})
	public void testGetExperimentMetaData() {

		List<String> scenarioMetaData = new ArrayList<>();
		scenarioMetaData.add("Alpha");
		scenarioMetaData.add("Beta");

		ExperimentStateManager experimentStateManager = ExperimentStateManager//
				.builder()//
				.setExperimentMetaData(scenarioMetaData).build();
		assertEquals(scenarioMetaData, experimentStateManager.getExperimentMetaData());

	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.class, name = "getOutputConsumer", args = { Integer.class })
	public void testGetOutputConsumer() {

		// precondition test: if the scenario id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			ExperimentStateManager experimentStateManager = ExperimentStateManager.builder().setScenarioCount(2)
					.build();//
			experimentStateManager.getOutputConsumer(null);
		});
		assertEquals(NucleusError.NULL_SCENARIO_ID, contractException.getErrorType());

		// precondition test: if the scenario id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			ExperimentStateManager experimentStateManager = ExperimentStateManager.builder().setScenarioCount(2)
					.build();//
			experimentStateManager.getOutputConsumer(2);
		});
		assertEquals(NucleusError.UNKNOWN_SCENARIO_ID, contractException.getErrorType());

		// post condition test:
		ExperimentStateManager experimentStateManager = ExperimentStateManager//
				.builder()//
				.setScenarioCount(3)//
				.build();

		for (int scenarioId = 0; scenarioId < 3; scenarioId++) {
			assertNotNull(experimentStateManager.getOutputConsumer(scenarioId));
		}
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.class, name = "getScenarioCount", args = {})
	public void testGetScenarioCount() {

		for (int i = 0; i < 10; i++) {
			ExperimentStateManager experimentStateManager = ExperimentStateManager//
					.builder()//
					.setScenarioCount(i)//
					.build();

			assertEquals(i, experimentStateManager.getScenarioCount());
		}
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.class, name = "getScenarioFailureCause", args = { int.class })
	public void testGetScenarioFailureCause() {

		ExperimentStateManager experimentStateManager = ExperimentStateManager//
				.builder()//
				.setScenarioCount(5)//
				.build();
		experimentStateManager.openExperiment();

		// close all scenarios, with scenarios 0 and 2 failing
		experimentStateManager.closeScenarioAsFailure(0, null);
		experimentStateManager.closeScenarioAsSuccess(1);
		experimentStateManager.closeScenarioAsFailure(2, new ContractException(NucleusError.ACCESS_VIOLATION));
		experimentStateManager.closeScenarioAsSuccess(3);
		experimentStateManager.closeScenarioAsSuccess(4);

		experimentStateManager.closeExperiment();

		// show that scenario 0 failed and has no known cause
		Optional<ScenarioStatus> optionalScenarioStatus = experimentStateManager.getScenarioStatus(0);
		assertTrue(optionalScenarioStatus.isPresent());
		assertEquals(ScenarioStatus.FAILED, optionalScenarioStatus.get());
		Optional<Exception> optionalFailureCause = experimentStateManager.getScenarioFailureCause(0);
		assertFalse(optionalFailureCause.isPresent());

		// show that scenario 2 failed and has known cause
		// ContractException(NucleusError.ACCESS_VIOLATION)
		optionalScenarioStatus = experimentStateManager.getScenarioStatus(2);
		assertTrue(optionalScenarioStatus.isPresent());
		assertEquals(ScenarioStatus.FAILED, optionalScenarioStatus.get());
		optionalFailureCause = experimentStateManager.getScenarioFailureCause(2);
		assertTrue(optionalFailureCause.isPresent());
		Exception exception = optionalFailureCause.get();
		assertTrue(exception instanceof ContractException);
		ContractException contractException = (ContractException) exception;
		assertEquals(NucleusError.ACCESS_VIOLATION, contractException.getErrorType());

		// show that scenarios 1,3,and 4 did not fail and have no know cause

		for (int i : new int[] { 1, 3, 4 }) {
			optionalScenarioStatus = experimentStateManager.getScenarioStatus(i);
			assertTrue(optionalScenarioStatus.isPresent());
			assertEquals(ScenarioStatus.SUCCEDED, optionalScenarioStatus.get());
			optionalFailureCause = experimentStateManager.getScenarioFailureCause(0);
			assertFalse(optionalFailureCause.isPresent());
		}

	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.class, name = "getScenarioMetaData", args = { Integer.class })
	public void testGetScenarioMetaData() {
		ExperimentStateManager experimentStateManager = ExperimentStateManager//
				.builder()//
				.setScenarioCount(5)//
				.build();
		experimentStateManager.openExperiment();

		// open scenarios 0 and 2 and provide them with meta data
		List<String> scenarioMetaData0 = new ArrayList<>();
		scenarioMetaData0.add("3");
		scenarioMetaData0.add("7");
		experimentStateManager.openScenario(0, scenarioMetaData0);

		List<String> scenarioMetaData2 = new ArrayList<>();
		scenarioMetaData2.add("4");
		scenarioMetaData2.add("6");
		experimentStateManager.openScenario(2, scenarioMetaData2);

		// show that a null scenario id is tolerated, but has no associated meta data
		assertTrue(experimentStateManager.getScenarioMetaData(null).isEmpty());

		// show that each scenario has the expected meta data
		assertEquals(scenarioMetaData0, experimentStateManager.getScenarioMetaData(0));
		assertTrue(experimentStateManager.getScenarioMetaData(1).isEmpty());
		assertEquals(scenarioMetaData2, experimentStateManager.getScenarioMetaData(2));
		assertTrue(experimentStateManager.getScenarioMetaData(3).isEmpty());
		assertTrue(experimentStateManager.getScenarioMetaData(4).isEmpty());
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.class, name = "getScenarios", args = { ScenarioStatus.class })
	public void testGetScenarios() {

		/*
		 * We will have 10 scenarios spanning the various ScenarioStatus types
		 * 
		 * 0 PREVIOUSLY_SUCCEEDED
		 * 
		 * 1 FAILED
		 * 
		 * 2 SUCCEDED
		 * 
		 * 3 PREVIOUSLY_SUCCEEDED
		 * 
		 * 4 FAILED
		 * 
		 * 5 SUCCEDED
		 * 
		 * 6 RUNNING
		 * 
		 * 7 RUNNING
		 * 
		 * 8 READY
		 * 
		 * 9 SKIPPED
		 * 
		 */

		// we fill the progress log with 0 and 3 as PREVIOUSLY_SUCCEEDED
		Set<Integer> previouslyCompleteScenarios = new LinkedHashSet<>();
		previouslyCompleteScenarios.add(0);
		previouslyCompleteScenarios.add(3);
		initializeProgressLog(previouslyCompleteScenarios);

		// we will use explicit scenarios to force 9 to be SKIPPED
		ExperimentStateManager experimentStateManager = ExperimentStateManager//
				.builder()//
				.setContinueFromProgressLog(true)//
				.setScenarioProgressLogFile(PROGRESS_LOG_PATH)//
				.setScenarioCount(10)//
				.addExplicitScenarioId(1)//
				.addExplicitScenarioId(2)//
				.addExplicitScenarioId(4)//
				.addExplicitScenarioId(5)//
				.addExplicitScenarioId(6)//
				.addExplicitScenarioId(7)//
				.addExplicitScenarioId(8)//
				.build();

		// we open some of the scenarios to move them from READY to RUNNING
		experimentStateManager.openScenario(1, new ArrayList<>());
		experimentStateManager.openScenario(2, new ArrayList<>());
		experimentStateManager.openScenario(4, new ArrayList<>());
		experimentStateManager.openScenario(5, new ArrayList<>());
		experimentStateManager.openScenario(6, new ArrayList<>());
		experimentStateManager.openScenario(7, new ArrayList<>());

		// we close some of the RUNNING to create SUCCEDED and FAILED scenarios
		experimentStateManager.closeScenarioAsFailure(1, null);
		experimentStateManager.closeScenarioAsSuccess(2);
		experimentStateManager.closeScenarioAsFailure(4, null);
		experimentStateManager.closeScenarioAsSuccess(5);

		// we build a map of lists of scenarios that is what we expect to find -- note
		// that we expect the lists to be ordered.
		Map<ScenarioStatus, List<Integer>> expectedStatusMap = new LinkedHashMap<>();
		expectedStatusMap.put(ScenarioStatus.READY, Arrays.asList(new Integer[] { 8 }));
		expectedStatusMap.put(ScenarioStatus.SKIPPED, Arrays.asList(new Integer[] { 9 }));
		expectedStatusMap.put(ScenarioStatus.PREVIOUSLY_SUCCEEDED, Arrays.asList(new Integer[] { 0, 3 }));
		expectedStatusMap.put(ScenarioStatus.RUNNING, Arrays.asList(new Integer[] { 6, 7 }));
		expectedStatusMap.put(ScenarioStatus.SUCCEDED, Arrays.asList(new Integer[] { 2, 5 }));
		expectedStatusMap.put(ScenarioStatus.FAILED, Arrays.asList(new Integer[] { 1, 4 }));

		// we show that the scenarios for each status are as expected.
		for (ScenarioStatus scenarioStatus : ScenarioStatus.values()) {
			List<Integer> actualScenarios = experimentStateManager.getScenarios(scenarioStatus);
			List<Integer> expectedScenarios = expectedStatusMap.get(scenarioStatus);
			assertEquals(expectedScenarios, actualScenarios);
		}

	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.class, name = "getScenarioStatus", args = { int.class })
	public void testGetScenarioStatus() {
		/*
		 * We will have 10 scenarios spanning the various ScenarioStatus types
		 * 
		 * 0 PREVIOUSLY_SUCCEEDED
		 * 
		 * 1 FAILED
		 * 
		 * 2 SUCCEDED
		 * 
		 * 3 PREVIOUSLY_SUCCEEDED
		 * 
		 * 4 FAILED
		 * 
		 * 5 SUCCEDED
		 * 
		 * 6 RUNNING
		 * 
		 * 7 RUNNING
		 * 
		 * 8 READY
		 * 
		 * 9 SKIPPED
		 * 
		 */

		// we fill the progress log with 0 and 3 as PREVIOUSLY_SUCCEEDED
		Set<Integer> previouslyCompleteScenarios = new LinkedHashSet<>();
		previouslyCompleteScenarios.add(0);
		previouslyCompleteScenarios.add(3);
		initializeProgressLog(previouslyCompleteScenarios);

		// we will use explicit scenarios to force 9 to be SKIPPED
		ExperimentStateManager experimentStateManager = ExperimentStateManager//
				.builder()//
				.setContinueFromProgressLog(true)//
				.setScenarioProgressLogFile(PROGRESS_LOG_PATH)//
				.setScenarioCount(10)//
				.addExplicitScenarioId(1)//
				.addExplicitScenarioId(2)//
				.addExplicitScenarioId(4)//
				.addExplicitScenarioId(5)//
				.addExplicitScenarioId(6)//
				.addExplicitScenarioId(7)//
				.addExplicitScenarioId(8)//
				.build();

		// we open some of the scenarios to move them from READY to RUNNING
		experimentStateManager.openScenario(1, new ArrayList<>());
		experimentStateManager.openScenario(2, new ArrayList<>());
		experimentStateManager.openScenario(4, new ArrayList<>());
		experimentStateManager.openScenario(5, new ArrayList<>());
		experimentStateManager.openScenario(6, new ArrayList<>());
		experimentStateManager.openScenario(7, new ArrayList<>());

		// we close some of the RUNNING to create SUCCEDED and FAILED scenarios
		experimentStateManager.closeScenarioAsFailure(1, null);
		experimentStateManager.closeScenarioAsSuccess(2);
		experimentStateManager.closeScenarioAsFailure(4, null);
		experimentStateManager.closeScenarioAsSuccess(5);

		// we build a map of lists of scenarios that is what we expect to find -- note
		// that we expect the lists to be ordered.
		Map<Integer, ScenarioStatus> expectedStatusMap = new LinkedHashMap<>();
		expectedStatusMap.put(0, ScenarioStatus.PREVIOUSLY_SUCCEEDED);
		expectedStatusMap.put(1, ScenarioStatus.FAILED);
		expectedStatusMap.put(2, ScenarioStatus.SUCCEDED);
		expectedStatusMap.put(3, ScenarioStatus.PREVIOUSLY_SUCCEEDED);
		expectedStatusMap.put(4, ScenarioStatus.FAILED);
		expectedStatusMap.put(5, ScenarioStatus.SUCCEDED);
		expectedStatusMap.put(6, ScenarioStatus.RUNNING);
		expectedStatusMap.put(7, ScenarioStatus.RUNNING);
		expectedStatusMap.put(8, ScenarioStatus.READY);
		expectedStatusMap.put(9, ScenarioStatus.SKIPPED);

		// we show that the scenarios for each status are as expected.
		for (Integer scenarioId : expectedStatusMap.keySet()) {
			Optional<ScenarioStatus> optionalScenarioStatus = experimentStateManager.getScenarioStatus(scenarioId);
			assertTrue(optionalScenarioStatus.isPresent());
			ScenarioStatus actualScenarioStatus = optionalScenarioStatus.get();
			ScenarioStatus expectedScenarioStatus = expectedStatusMap.get(scenarioId);
			assertEquals(expectedScenarioStatus, actualScenarioStatus);
		}
		Optional<ScenarioStatus> optionalScenarioStatus = experimentStateManager.getScenarioStatus(10);
		assertFalse(optionalScenarioStatus.isPresent());

	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.class, name = "getStatusCount", args = {
			ScenarioStatus.class })
	public void testGetStatusCount() {
		/*
		 * We will have 10 scenarios spanning the various ScenarioStatus types
		 * 
		 * 0 PREVIOUSLY_SUCCEEDED
		 * 
		 * 1 FAILED
		 * 
		 * 2 SUCCEDED
		 * 
		 * 3 PREVIOUSLY_SUCCEEDED
		 * 
		 * 4 FAILED
		 * 
		 * 5 SUCCEDED
		 * 
		 * 6 RUNNING
		 * 
		 * 7 RUNNING
		 * 
		 * 8 READY
		 * 
		 * 9 SKIPPED
		 * 
		 */

		// we fill the progress log with 0 and 3 as PREVIOUSLY_SUCCEEDED
		Set<Integer> previouslyCompleteScenarios = new LinkedHashSet<>();
		previouslyCompleteScenarios.add(0);
		previouslyCompleteScenarios.add(3);
		initializeProgressLog(previouslyCompleteScenarios);

		// we will use explicit scenarios to force 9 to be SKIPPED
		ExperimentStateManager experimentStateManager = ExperimentStateManager//
				.builder()//
				.setContinueFromProgressLog(true)//
				.setScenarioProgressLogFile(PROGRESS_LOG_PATH)//
				.setScenarioCount(10)//
				.addExplicitScenarioId(1)//
				.addExplicitScenarioId(2)//
				.addExplicitScenarioId(4)//
				.addExplicitScenarioId(5)//
				.addExplicitScenarioId(6)//
				.addExplicitScenarioId(7)//
				.addExplicitScenarioId(8)//
				.build();

		// we open some of the scenarios to move them from READY to RUNNING
		experimentStateManager.openScenario(1, new ArrayList<>());
		experimentStateManager.openScenario(2, new ArrayList<>());
		experimentStateManager.openScenario(4, new ArrayList<>());
		experimentStateManager.openScenario(5, new ArrayList<>());
		experimentStateManager.openScenario(6, new ArrayList<>());
		experimentStateManager.openScenario(7, new ArrayList<>());

		// we close some of the RUNNING to create SUCCEDED and FAILED scenarios
		experimentStateManager.closeScenarioAsFailure(1, null);
		experimentStateManager.closeScenarioAsSuccess(2);
		experimentStateManager.closeScenarioAsFailure(4, null);
		experimentStateManager.closeScenarioAsSuccess(5);

		// we build a map of lists of scenarios that is what we expect to find -- note
		// that we expect the lists to be ordered.
		Map<ScenarioStatus, List<Integer>> expectedStatusMap = new LinkedHashMap<>();
		expectedStatusMap.put(ScenarioStatus.READY, Arrays.asList(new Integer[] { 8 }));
		expectedStatusMap.put(ScenarioStatus.SKIPPED, Arrays.asList(new Integer[] { 9 }));
		expectedStatusMap.put(ScenarioStatus.PREVIOUSLY_SUCCEEDED, Arrays.asList(new Integer[] { 0, 3 }));
		expectedStatusMap.put(ScenarioStatus.RUNNING, Arrays.asList(new Integer[] { 6, 7 }));
		expectedStatusMap.put(ScenarioStatus.SUCCEDED, Arrays.asList(new Integer[] { 2, 5 }));
		expectedStatusMap.put(ScenarioStatus.FAILED, Arrays.asList(new Integer[] { 1, 4 }));

		// we show that the scenarios for each status are as expected.
		for (ScenarioStatus scenarioStatus : ScenarioStatus.values()) {
			int actualCount = experimentStateManager.getStatusCount(scenarioStatus);
			List<Integer> expectedScenarios = expectedStatusMap.get(scenarioStatus);
			int expectedCount = expectedScenarios.size();
			assertEquals(expectedCount, actualCount);
		}
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.class, name = "openExperiment", args = {})
	public void testOpenExperiment() {

		/*
		 * Create a local consumer of ExperimentContext implementation. Each implementor
		 * is signaled via its constructor to possibly subscribe to the experiment open
		 * event.
		 */
		class LocalConsumer implements Consumer<ExperimentContext> {
			LocalConsumer(boolean subscribeToExperimentOpen) {
				this.subscribeToExperimentOpen = subscribeToExperimentOpen;
			}

			private final boolean subscribeToExperimentOpen;

			private boolean baseInvocation;

			private boolean experimentOpenInvocation;

			@Override
			public void accept(ExperimentContext c) {
				baseInvocation = true;
				if (subscribeToExperimentOpen) {
					c.subscribeToExperimentOpen((c2) -> {
						experimentOpenInvocation = true;
					});
				}
			}

		}

		// create a few local consumers that will sometimes subscribe to the experiment
		// open
		List<LocalConsumer> localConsumers = new ArrayList<>();
		localConsumers.add(new LocalConsumer(false));
		localConsumers.add(new LocalConsumer(true));
		localConsumers.add(new LocalConsumer(false));
		localConsumers.add(new LocalConsumer(true));
		localConsumers.add(new LocalConsumer(true));
		localConsumers.add(new LocalConsumer(false));

		// build the experiment state manager with the local consumers
		ExperimentStateManager.Builder builder = ExperimentStateManager//
				.builder();//
		for (LocalConsumer localConsumer : localConsumers) {
			builder.addExperimentContextConsumer(localConsumer);
		}

		ExperimentStateManager experimentStateManager = builder.build();

		// open the experiment
		experimentStateManager.openExperiment();

		// show that all consumers are initialized
		for (LocalConsumer localConsumer : localConsumers) {
			assertTrue(localConsumer.baseInvocation);
		}

		// show that the opening of the experiment was announced to all consumers that
		// subscribed to that event
		for (LocalConsumer localConsumer : localConsumers) {
			assertEquals(localConsumer.subscribeToExperimentOpen, localConsumer.experimentOpenInvocation);
		}

		// precondition test: if invoked more that once
		ContractException contractException = assertThrows(ContractException.class,
				() -> experimentStateManager.openExperiment());
		assertEquals(NucleusError.DUPLICATE_EXPERIMENT_OPEN, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.class, name = "openScenario", args = { Integer.class, List.class })
	public void testOpenScenario() {
		List<String> metaData = new ArrayList<>();
		metaData.add("alpha");
		metaData.add("beta");

		List<String> metaDataWithNull = new ArrayList<>();
		metaDataWithNull.add("alpha");
		metaDataWithNull.add(null);

		// precondition test: if the scenario id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> ExperimentStateManager.builder().setScenarioCount(5).build().openScenario(null, metaData));
		assertEquals(NucleusError.NULL_SCENARIO_ID, contractException.getErrorType());

		// precondition test: if the meta data is null
		contractException = assertThrows(ContractException.class,
				() -> ExperimentStateManager.builder().setScenarioCount(5).build().openScenario(0, null));
		assertEquals(NucleusError.NULL_META_DATA, contractException.getErrorType());

		// precondition test: if the meta data contains a null datum
		contractException = assertThrows(ContractException.class,
				() -> ExperimentStateManager.builder().setScenarioCount(5).build().openScenario(0, metaDataWithNull));
		assertEquals(NucleusError.NULL_META_DATA, contractException.getErrorType());

		// precondition test: if the scenario is not known
		contractException = assertThrows(ContractException.class,
				() -> ExperimentStateManager.builder().setScenarioCount(5).build().openScenario(10, metaData));
		assertEquals(NucleusError.UNKNOWN_SCENARIO_ID, contractException.getErrorType());

		// precondition test: if the scenario's current status is not READY
		contractException = assertThrows(ContractException.class, () -> {
			ExperimentStateManager experimentStateManager = ExperimentStateManager.builder().setScenarioCount(5)
					.build();
			experimentStateManager.closeScenarioAsFailure(0, null);
			experimentStateManager.openScenario(0, metaData);
		});
		assertEquals(NucleusError.SCENARIO_CANNOT_BE_EXECUTED, contractException.getErrorType());

		// post condition test: Updates the scenario's status to RUNNING
		ExperimentStateManager experimentStateManager = ExperimentStateManager.builder().setScenarioCount(5).build();

		experimentStateManager.openScenario(0, metaData);
		experimentStateManager.openScenario(1, metaData);
		experimentStateManager.openScenario(4, metaData);

		assertEquals(ScenarioStatus.RUNNING, experimentStateManager.getScenarioStatus(0).get());
		assertEquals(ScenarioStatus.RUNNING, experimentStateManager.getScenarioStatus(1).get());
		assertEquals(ScenarioStatus.READY, experimentStateManager.getScenarioStatus(2).get());
		assertEquals(ScenarioStatus.READY, experimentStateManager.getScenarioStatus(3).get());
		assertEquals(ScenarioStatus.RUNNING, experimentStateManager.getScenarioStatus(4).get());

		// post condition test: Sets the meta data for the scenario
		experimentStateManager = ExperimentStateManager.builder().setScenarioCount(5).build();

		experimentStateManager.openScenario(0, metaData);
		experimentStateManager.openScenario(1, metaData);
		experimentStateManager.openScenario(4, metaData);

		assertEquals(metaData, experimentStateManager.getScenarioMetaData(0));
		assertEquals(metaData, experimentStateManager.getScenarioMetaData(1));
		assertTrue(experimentStateManager.getScenarioMetaData(2).isEmpty());
		assertTrue(experimentStateManager.getScenarioMetaData(3).isEmpty());
		assertEquals(metaData, experimentStateManager.getScenarioMetaData(4));

		// post condition test: Announces the opening of the scenario to subscribed
		// experiment context consumers.
		Set<Integer> observedSceanrioOpens = new LinkedHashSet<>();

		experimentStateManager = ExperimentStateManager.builder()//
				.addExperimentContextConsumer((c) -> {
					c.subscribeToSimulationOpen((c2, s) -> {
						observedSceanrioOpens.add(s);
					});
				}).setScenarioCount(5)//
				.build();//

		experimentStateManager.openExperiment();

		experimentStateManager.openScenario(0, metaData);
		experimentStateManager.openScenario(1, metaData);
		experimentStateManager.openScenario(4, metaData);

		Set<Integer> expectedSceanrioOpens = new LinkedHashSet<>();
		expectedSceanrioOpens.add(0);
		expectedSceanrioOpens.add(1);
		expectedSceanrioOpens.add(4);

		assertEquals(expectedSceanrioOpens, observedSceanrioOpens);
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.Builder.class, name = "addExperimentContextConsumer", args = {
			Consumer.class })
	public void testAddExperimentContextConsumer() {

		// precondition test: if the context consumer is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> ExperimentStateManager.builder().addExperimentContextConsumer(null));
		assertEquals(NucleusError.NULL_EXPERIMENT_CONTEXT_CONSUMER, contractException.getErrorType());

		/*
		 * post condition test: Adds a experiment context consumer that will be
		 * initialized at the start of the experiment.
		 */
		MutableBoolean consumerInitialized = new MutableBoolean();

		ExperimentStateManager experimentStateManager = ExperimentStateManager.builder()//
				.addExperimentContextConsumer((c) -> {
					c.subscribeToSimulationOpen((c2, s) -> {
						consumerInitialized.setValue(true);
					});
				}).setScenarioCount(5)//
				.build();//

		experimentStateManager.openExperiment();
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.Builder.class, name = "build", args = {})
	public void testBuild() {

		// precondition test: if an explicit scenario id is not in the span of the
		// experiment's scenario ids
		ContractException contractException = assertThrows(ContractException.class, () -> {
			ExperimentStateManager.builder()//
					.setScenarioCount(5)//
					.addExplicitScenarioId(10).build();//
		});
		assertEquals(NucleusError.UNKNOWN_SCENARIO_ID, contractException.getErrorType());

		// precondition test: if continue from progress file was chosen, but the path to
		// the file is null
		contractException = assertThrows(ContractException.class, () -> {
			ExperimentStateManager.builder()//
					.setContinueFromProgressLog(true)//
					.build();//
		});
		assertEquals(NucleusError.NULL_SCENARIO_PROGRESS_FILE, contractException.getErrorType());

		// precondition test: if continue from progress file was chosen, but the path to
		// the file does not exist
		contractException = assertThrows(ContractException.class, () -> {
			ExperimentStateManager.builder()//
					.setContinueFromProgressLog(true)//
					.setScenarioProgressLogFile(NONEXISTENT_PROGRESS_LOG_PATH).build();//
		});
		assertEquals(NucleusError.NON_EXISTANT_SCEANARIO_PROGRESS, contractException.getErrorType());

		// precondition test: if continue from progress file was chosen, but the path
		// lead to a non-file
		contractException = assertThrows(ContractException.class, () -> {
			ExperimentStateManager.builder()//
					.setContinueFromProgressLog(true)//
					.setScenarioProgressLogFile(NONFILE_PROGRESS_LOG_PATH).build();//
		});
		assertEquals(NucleusError.UNREADABLE_SCEANARIO_PROGRESS, contractException.getErrorType());

		/*
		 * precondition test: if the lines of the file cannot be loaded :
		 * NucleusError.UNREADABLE_SCEANARIO_PROGRESS
		 * 
		 * It is not feasible to test this precondition since it requires a file that is
		 * somehow corrupt, enormous or otherwise malformed.
		 * 
		 */

		// precondition test: if the header line of the file does not match the expected
		// header line for the current experiment
		contractException = assertThrows(ContractException.class, () -> {
			Set<Integer> previouslyExecutedScenarios = new LinkedHashSet<>();
			previouslyExecutedScenarios.add(0);
			previouslyExecutedScenarios.add(3);
			initializeProgressLogWithMetaData(previouslyExecutedScenarios);
			
			List<String> metaData = new ArrayList<>();
			metaData.add("A");
			metaData.add("B");
			metaData.add("C");

			ExperimentStateManager.builder()//
					.setContinueFromProgressLog(true)//
					.setScenarioCount(7)//
					.setExperimentMetaData(metaData)
					.setScenarioProgressLogFile(PROGRESS_LOG_PATH)//
					.build();//
		});
		assertEquals(NucleusError.INCOMPATIBLE_SCEANARIO_PROGRESS, contractException.getErrorType());

		// precondition test: if a scenario id is encountered that is not valid for the
		// the current experiment
		contractException = assertThrows(ContractException.class, () -> {
			Set<Integer> previouslyExecutedScenarios = new LinkedHashSet<>();
			previouslyExecutedScenarios.add(0);
			previouslyExecutedScenarios.add(3);
			initializeProgressLogWithMetaData(previouslyExecutedScenarios);
			
			List<String> metaData = new ArrayList<>();
			metaData.add("A");
			metaData.add("B");

			ExperimentStateManager.builder()//
					.setContinueFromProgressLog(true)//
					.setScenarioCount(2)//
					.setExperimentMetaData(metaData)
					.setScenarioProgressLogFile(PROGRESS_LOG_PATH)//
					.build();//
			
			
		});
		assertEquals(NucleusError.INCOMPATIBLE_SCEANARIO_PROGRESS, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.Builder.class, name = "setContinueFromProgressLog", args = {
			boolean.class })
	public void testSetContinueFromProgressLog() {

		Set<Integer> previouslyCompletedScenarios = new LinkedHashSet<>();
		previouslyCompletedScenarios.add(0);
		previouslyCompletedScenarios.add(1);
		previouslyCompletedScenarios.add(3);
		initializeProgressLog(previouslyCompletedScenarios);

		ExperimentStateManager experimentStateManager = ExperimentStateManager.builder()
				.setScenarioProgressLogFile(PROGRESS_LOG_PATH)//
				.setContinueFromProgressLog(true)//
				.setScenarioCount(5).build();

		assertEquals(ScenarioStatus.PREVIOUSLY_SUCCEEDED, experimentStateManager.getScenarioStatus(0).get());
		assertEquals(ScenarioStatus.PREVIOUSLY_SUCCEEDED, experimentStateManager.getScenarioStatus(1).get());
		assertEquals(ScenarioStatus.READY, experimentStateManager.getScenarioStatus(2).get());
		assertEquals(ScenarioStatus.PREVIOUSLY_SUCCEEDED, experimentStateManager.getScenarioStatus(3).get());
		assertEquals(ScenarioStatus.READY, experimentStateManager.getScenarioStatus(4).get());

		experimentStateManager = ExperimentStateManager.builder().setScenarioProgressLogFile(PROGRESS_LOG_PATH)
				.setContinueFromProgressLog(false).setScenarioCount(5).build();

		assertEquals(ScenarioStatus.READY, experimentStateManager.getScenarioStatus(0).get());
		assertEquals(ScenarioStatus.READY, experimentStateManager.getScenarioStatus(1).get());
		assertEquals(ScenarioStatus.READY, experimentStateManager.getScenarioStatus(2).get());
		assertEquals(ScenarioStatus.READY, experimentStateManager.getScenarioStatus(3).get());
		assertEquals(ScenarioStatus.READY, experimentStateManager.getScenarioStatus(4).get());

	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.Builder.class, name = "setExperimentMetaData", args = {
			List.class })
	public void testSetExperimentMetaData() {
		/*
		 * Covered by tests for Experiment and ExperimentContext classes
		 */

		List<String> expectedMetaData = new ArrayList<>();

		expectedMetaData.add("A");
		expectedMetaData.add("B");
		expectedMetaData.add("C");
		expectedMetaData.add("D");

		ExperimentStateManager experimentStateManager = ExperimentStateManager.builder()
				.setExperimentMetaData(expectedMetaData)//
				.build();

		assertEquals(expectedMetaData, experimentStateManager.getExperimentMetaData());

	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.Builder.class, name = "setScenarioCount", args = { Integer.class })
	public void testSetScenarioCount() {
		for (int i = 0; i < 10; i++) {
			ExperimentStateManager experimentStateManager = ExperimentStateManager.builder().setScenarioCount(i)
					.build();
			assertEquals(i, experimentStateManager.getScenarioCount());
		}
	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.Builder.class, name = "setScenarioProgressLogFile", args = {
			Path.class })
	public void testSetScenarioProgressLogFile() {

		// we fill the progress log with 0 and 3 as PREVIOUSLY_SUCCEEDED
		Set<Integer> previouslyCompleteScenarios = new LinkedHashSet<>();
		previouslyCompleteScenarios.add(0);
		previouslyCompleteScenarios.add(3);
		initializeProgressLog(previouslyCompleteScenarios);

		ExperimentStateManager experimentStateManager = ExperimentStateManager//
				.builder()//
				.setContinueFromProgressLog(true)//
				.setScenarioProgressLogFile(PROGRESS_LOG_PATH)//
				.setScenarioCount(5)//
				.build();

		// We demonstrate that the progress file was utilized as expected
		assertEquals(ScenarioStatus.PREVIOUSLY_SUCCEEDED, experimentStateManager.getScenarioStatus(0).get());
		assertEquals(ScenarioStatus.PREVIOUSLY_SUCCEEDED, experimentStateManager.getScenarioStatus(3).get());

	}

	@Test
	@UnitTestMethod(target = ExperimentStateManager.Builder.class, name = "addExplicitScenarioId", args = {
			Integer.class })
	public void testAddExplicitScenarioId() {
		// we fill the progress log with 0 and 3 as PREVIOUSLY_SUCCEEDED
		Set<Integer> previouslyCompleteScenarios = new LinkedHashSet<>();
		previouslyCompleteScenarios.add(0);
		previouslyCompleteScenarios.add(3);
		initializeProgressLog(previouslyCompleteScenarios);

		/*
		 * We use some explicit values. Note that 3 is explicit, which overrides it
		 * having been previously executed.
		 */
		ExperimentStateManager experimentStateManager = ExperimentStateManager//
				.builder()//
				.setContinueFromProgressLog(true)//
				.setScenarioProgressLogFile(PROGRESS_LOG_PATH)//
				.setScenarioCount(10)//
				.addExplicitScenarioId(1)//
				.addExplicitScenarioId(2)//
				.addExplicitScenarioId(3)//
				.addExplicitScenarioId(4)//
				.addExplicitScenarioId(5)//
				.build();

		// build our expectations
		Map<Integer, ScenarioStatus> expectedStatusValues = new LinkedHashMap<>();
		expectedStatusValues.put(0, ScenarioStatus.PREVIOUSLY_SUCCEEDED);
		expectedStatusValues.put(1, ScenarioStatus.READY);
		expectedStatusValues.put(2, ScenarioStatus.READY);
		expectedStatusValues.put(3, ScenarioStatus.READY);
		expectedStatusValues.put(4, ScenarioStatus.READY);
		expectedStatusValues.put(5, ScenarioStatus.READY);
		expectedStatusValues.put(6, ScenarioStatus.SKIPPED);
		expectedStatusValues.put(7, ScenarioStatus.SKIPPED);
		expectedStatusValues.put(8, ScenarioStatus.SKIPPED);
		expectedStatusValues.put(9, ScenarioStatus.SKIPPED);

		// We demonstrate that each scenario has the expected status.
		for (int i = 0; i < 10; i++) {
			ScenarioStatus actualScenarioStatus = experimentStateManager.getScenarioStatus(i).get();
			ScenarioStatus expectedScenarioStatus = expectedStatusValues.get(i);
			assertEquals(expectedScenarioStatus, actualScenarioStatus);
		}
	}
}
