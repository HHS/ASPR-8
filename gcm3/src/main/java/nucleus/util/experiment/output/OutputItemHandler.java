package nucleus.util.experiment.output;

import java.util.Set;

import net.jcip.annotations.ThreadSafe;
import nucleus.util.experiment.progress.ExperimentProgressLog;


/**
 * Defines the behavior of a thread-safe handler of {@link OutputItem}
 *
 * @author Shawn Hatch
 *
 */
@ThreadSafe
public interface OutputItemHandler {
	/**
	 * Invoked to indicate that the given simulation has started. This is a
	 * blocking method.
	 */
	public void openSimulation(int scenarioId);

	/**
	 * Invoked to indicate that the experiment has started. All output from
	 * previous experiment executions consistent with the
	 * {@link ExperimentProgressLog} should be preserved while all other output
	 * should be eliminated. This is a blocking method.
	 */
	public void openExperiment(ExperimentProgressLog experimentProgressLog);

	/**
	 * Invoked to indicate that no more handle invocations will be called for
	 * the given simulation. This is a blocking method and all retained
	 * information about the specific simulation execution should be flushed.
	 */
	public void closeSimulation(int scenarioId);

	/**
	 * Invoked to indicate that no more handle invocations will be called for
	 * the experiment. This is a blocking method and all resources should be
	 * finalized/closed.
	 */
	public void closeExperiment();

	/**
	 * Handles the OutputItem. This method is not required to block.
	 *
	 */
	public void handle(int scenarioId, Object output);

	/**
	 * Returns a set of class references indicating which sub-types of
	 * OutputItem this OutputItemHandler handles. This is a blocking method that
	 * is called once at the start of each simulation execution.
	 */
	public Set<Class<?>> getHandledClasses();
}
