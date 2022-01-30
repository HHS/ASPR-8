package nucleus;

import java.util.List;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.jcip.annotations.ThreadSafe;
import util.ContractException;
import util.TriConsumer;

/**
 * Interface for the thread safe access of experiment/scenario state and meta
 * data. This context provides subscription for the five key events that occur
 * outside of the simulation; 1) Experiment Open, 2) Scenario Open, 3) Scenario
 * Output, 4) Scenario Close and 5) Experiment Close
 */

@ThreadSafe
public interface ExperimentContext {

	public void subscribeToSimulationOpen(BiConsumer<ExperimentContext, Integer> consumer);

	public void subscribeToSimulationClose(BiConsumer<ExperimentContext, Integer> consumer);

	public void subscribeToExperimentOpen(Consumer<ExperimentContext> consumer);

	public void subscribeToExperimentClose(Consumer<ExperimentContext> consumer);

	/**
	 * Subscribes the output handler to output of the given type.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_EVENT_CLASS} if the event class
	 *             is null
	 *             <li>{@link NucleusError#NULL_EVENT_CONSUMER} if the resolver
	 *             event consumer is null
	 */
	public <T> void subscribeToOutput(Class<T> outputClass, TriConsumer<ExperimentContext, Integer, T> consumer);

	public Optional<ScenarioStatus> getScenarioStatus(int scenarioId);

	public int getStatusCount(ScenarioStatus scenarioStatus);

	public double getElapsedSeconds();

	public Optional<List<String>> getScenarioMetaData(int scenarioId);

	public List<String> getExperimentMetaData();

	public int getScenarioCount();

	public List<Integer> getScenarios(ScenarioStatus scenarioStatus);

}
