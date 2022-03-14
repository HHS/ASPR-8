package nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import annotations.UnitTest;
import annotations.UnitTestMethod;
import nucleus.ExperimentContext;
import nucleus.ScenarioStatus;
import nucleus.util.TriConsumer;
import util.RandomGeneratorProvider;

@UnitTest(target = ExperimentPlanCompletionObserver.class)
public class AT_TestExperimentObserver {

	private static class MetaOutputConsumer<T> {
		private MetaOutputConsumer(TriConsumer<ExperimentContext, Integer, T> consumer) {
			this.consumer = consumer;
		}

		private TriConsumer<ExperimentContext, Integer, T> consumer;

		@SuppressWarnings("unchecked")
		public void accept(ExperimentContext experimentContext, Integer scenarioId, Object object) {
			consumer.accept(experimentContext, scenarioId, (T) object);
		}
	}

	private static class MockExperimentContext implements ExperimentContext {

		@Override
		public void subscribeToSimulationOpen(BiConsumer<ExperimentContext, Integer> consumer) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void subscribeToSimulationClose(BiConsumer<ExperimentContext, Integer> consumer) {
			throw new UnsupportedOperationException();

		}

		@Override
		public void subscribeToExperimentOpen(Consumer<ExperimentContext> consumer) {
			throw new UnsupportedOperationException();

		}

		@Override
		public void subscribeToExperimentClose(Consumer<ExperimentContext> consumer) {
			throw new UnsupportedOperationException();

		}

		public void sendTestScenarioReport(Integer scenarioId, TestScenarioReport testScenarioReport) {
			metaOutputConsumer.accept(this, scenarioId, testScenarioReport);
		}

		private MetaOutputConsumer<?> metaOutputConsumer;

		@Override
		public <T> void subscribeToOutput(Class<T> outputClass, TriConsumer<ExperimentContext, Integer, T> consumer) {

			metaOutputConsumer = new MetaOutputConsumer<T>(consumer);
		}

		@Override
		public Optional<ScenarioStatus> getScenarioStatus(int scenarioId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int getStatusCount(ScenarioStatus scenarioStatus) {
			throw new UnsupportedOperationException();
		}

		@Override
		public double getElapsedSeconds() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Optional<List<String>> getScenarioMetaData(int scenarioId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<String> getExperimentMetaData() {
			throw new UnsupportedOperationException();
		}

		@Override
		public int getScenarioCount() {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<Integer> getScenarios(ScenarioStatus scenarioStatus) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Optional<Exception> getSceanarioFailureCause(int scenarioId) {
			return Optional.empty();
		}

	}

	@Test
	@UnitTestMethod(name = "init", args = { ExperimentContext.class })
	public void testInit() {
		// nothing to test : covered by test of getActionCompletionReport
	}

	@Test
	@UnitTestMethod(name = "getActionCompletionReport", args = { Integer.class })
	public void getActionCompletionReport() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7048252990105726149L);

		//create a mock experiment that has the minimal capability
		MockExperimentContext mockExperimentContext = new MockExperimentContext();

		//create a TestExperimentObserver to test
		ExperimentPlanCompletionObserver experimentPlanCompletionObserver = new ExperimentPlanCompletionObserver();
		experimentPlanCompletionObserver.init(mockExperimentContext);

		//create TestScenarioReport items using random scenario id and completion states
		Map<Integer, TestScenarioReport> expectedTestScenarioReports = new LinkedHashMap<>();
		for (int i = 0; i < 10; i++) {
			Integer scenarioId = randomGenerator.nextInt(1000);
			Boolean completion = randomGenerator.nextBoolean();
			TestScenarioReport expectedTestScenarioReport = new TestScenarioReport(completion);
			expectedTestScenarioReports.put(scenarioId, expectedTestScenarioReport);
		}
		
		//send the  TestScenarioReport items to the TestExperimentObserver
		for(Integer scenarioId : expectedTestScenarioReports.keySet()) {
			TestScenarioReport expectedTestScenarioReport = expectedTestScenarioReports.get(scenarioId);
			mockExperimentContext.sendTestScenarioReport(scenarioId, expectedTestScenarioReport);	
		}
		
		//show that the correct TestScenarioReport items can be retrieved
		for(Integer scenarioId : expectedTestScenarioReports.keySet()) {
			TestScenarioReport expectedTestScenarioReport = expectedTestScenarioReports.get(scenarioId);
			Optional<TestScenarioReport> optional = experimentPlanCompletionObserver.getActionCompletionReport(scenarioId);
			assertTrue(optional.isPresent());
			TestScenarioReport actualTestScenarioReport = optional.get();
			assertEquals(expectedTestScenarioReport, actualTestScenarioReport);
		}
		
		//show that an unknown scenario id will not retrieve TestScenarioReport items
		
		Optional<TestScenarioReport> optional = experimentPlanCompletionObserver.getActionCompletionReport(1000);
		assertFalse(optional.isPresent());

		optional = experimentPlanCompletionObserver.getActionCompletionReport(null);
		assertFalse(optional.isPresent());


	}
}
