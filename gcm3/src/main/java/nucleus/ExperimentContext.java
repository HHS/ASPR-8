package nucleus;

import java.util.List;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.jcip.annotations.ThreadSafe;
import nucleus.util.ContractException;
import nucleus.util.TriConsumer;

/**
 * Interface for the thread safe access of experiment/scenario state and meta
 * data. This context provides subscription for the five key events that occur
 * outside of the simulation; 1) Experiment Open, 2) Scenario Open, 3) Scenario
 * Output, 4) Scenario Close and 5) Experiment Close
 */

@ThreadSafe
public interface ExperimentContext {

	/**
	 * Subscribes to the open of simulations
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@link NucleusError.NULL_EXPERIMENT_CONTEXT_CONSUMER} if
	 *             the consumer is null</li>
	 */
	public void subscribeToSimulationOpen(BiConsumer<ExperimentContext, Integer> consumer);

	/**
	 * Subscribes to the close of simulations
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@link NucleusError.NULL_EXPERIMENT_CONTEXT_CONSUMER} if
	 *             the consumer is null</li>
	 */

	public void subscribeToSimulationClose(BiConsumer<ExperimentContext, Integer> consumer);

	/**
	 * Subscribes to the open of the experiment
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@link NucleusError.NULL_EXPERIMENT_CONTEXT_CONSUMER} if
	 *             the consumer is null</li>
	 */
	public void subscribeToExperimentOpen(Consumer<ExperimentContext> consumer);

	/**
	 * Subscribes to the close of the experiment
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@link NucleusError.NULL_EXPERIMENT_CONTEXT_CONSUMER} if
	 *             the consumer is null</li>
	 */
	public void subscribeToExperimentClose(Consumer<ExperimentContext> consumer);

	/**
	 * Subscribes the output handler to output of the given type.
	 * 
	 * @throws ContractException
	 * 
	 * 
	 *             <li>{@link NucleusError#NULL_EXPERIMENT_CONTEXT_CONSUMER} if
	 *             the consumer is null</li>
	 *             <li>{@link NucleusError#NULL_EXPERIMENT_CONTEXT_CONSUMER} if
	 *             the consumer is null</li>
	 * 
	 * 
	 * 
	 */
	public <T> void subscribeToOutput(Class<T> outputClass, TriConsumer<ExperimentContext, Integer, T> consumer);

	/**
	 * 
	 * Returns the current status for the given scenario id if the scenario
	 * exists.
	 * 
	 */
	public Optional<ScenarioStatus> getScenarioStatus(int scenarioId);

	/**
	 * 
	 * Returns the current number of scenarios with the given status
	 * 
	 */
	public int getStatusCount(ScenarioStatus scenarioStatus);

	/**
	 * Returns the number of seconds that have elapsed since the start of the
	 * experimnet
	 */
	public double getElapsedSeconds();

	/**
	 * Returns the list of meta data for the given scenario if it is available.
	 */
	public Optional<List<String>> getScenarioMetaData(int scenarioId);

	/**
	 * Returns the list of meta data for the experiment. These meta data are
	 * descriptors of the scenario meta data produced by each execution of the
	 * simulation.
	 */
	public List<String> getExperimentMetaData();

	/**
	 * Returns the number of scenarios in the experiment
	 */
	public int getScenarioCount();

	/**
	 * Returns the current list of scenario ids for the given scenario status
	 */
	public List<Integer> getScenarios(ScenarioStatus scenarioStatus);

}
