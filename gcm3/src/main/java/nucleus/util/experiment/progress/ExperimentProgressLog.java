package nucleus.util.experiment.progress;

import java.util.LinkedHashSet;
import java.util.Set;

import net.jcip.annotations.ThreadSafe;
import plugins.gcm.experiment.ReplicationId;
import plugins.gcm.experiment.ScenarioId;

/**
 * A thread-safe, immutable container for (ScenarioId,ReplicationId) pairs
 * representing simulation executions that were previously run by GCM and can be
 * skipped during the execution of an experiment. It is constructed via the
 * contained builder class.
 * 
 * 
 * 
 * @author Shawn Hatch
 *
 */
@ThreadSafe
public final class ExperimentProgressLog {
	
	private final Set<Integer> scenarioIds;

	
	private ExperimentProgressLog(Scaffold scaffold) {
		scenarioIds = scaffold.scenarioIds;
	}

	/*
	 * A contained used by the builder class to hold the
	 * (ScenarioId,ReplicationId) pairs
	 */
	private static class Scaffold {
		Set<Integer> scenarioIds = new LinkedHashSet<>();
	}

	public static Builder builder() {
		return new Builder();
	}

	/**
	 * A builder class for {@link ExperimentProgressLog}
	 * 
	 * @author Shawn Hatch
	 *
	 */
	public static class Builder {

		private Builder() {

		}

		/*
		 * Data structure for holding client inputs. Is refreshed on each
		 * invocation of build().
		 */
		private Scaffold scaffold = new Scaffold();

		/**
		 * Adds a (ScenarioId,ReplicationId) pair
		 * 
		 * @param scenarioId
		 *            the {@link ScenarioId} of a (ScenarioId,ReplicationId)
		 *            pair
		 * 
		 * @param replicationId
		 *            the {@link ReplicationId} id of a
		 *            (ScenarioId,ReplicationId) pair
		 * 
		 * @throws RuntimeException
		 *             if either entry is null
		 * 
		 */
		public Builder add(Integer scenarioId) {

			if (scenarioId == null) {
				throw new RuntimeException("null scenario id");
			}
			scaffold.scenarioIds.add(scenarioId);
			return this;
		}

		/**
		 * Builds a {@link ExperimentProgressLog} from the collected
		 * (ScenarioId,ReplicationId) pairs.
		 */
		public ExperimentProgressLog build() {
			try {
				return new ExperimentProgressLog(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}
	}

	/**
	 * Returns the set of scenario ids 
	 *
	 */
	public Set<Integer> getScenarioIds() {
		return new LinkedHashSet<>(scenarioIds);
	}


	/**
	 * Returns true if and only if the given scenario id is contained
	 */
	public boolean contains(Integer scenarioId) {
		return scenarioIds.contains(scenarioId);		
	}

	/**
	 * Returns the number of scenario ids contained in this
	 * {@link ExperimentProgressLog}.
	 */
	public int size() {
		return scenarioIds.size();
	}

	/**
	 * Returns true if and only if this {@link ExperimentProgressLog} contains no scenario ids.
	 */
	public boolean isEmpty() {
		return scenarioIds.isEmpty();
	}

}
