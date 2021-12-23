package plugins.gcm.experiment.progress;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
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
	/*
	 * The (ScenarioId,ReplicationId) pairs
	 */
	private final Map<ScenarioId, Set<ReplicationId>> map;

	/*
	 * The number of (ScenarioId,ReplicationId) pairs contained in the map
	 */
	private final int size;

	/*
	 * Private constructor. The builder class constructor instances of
	 * ExperimentProgressLog
	 */
	private ExperimentProgressLog(Scaffold scaffold) {
		map = scaffold.map;
		int count = 0;
		for (Set<ReplicationId> set : map.values()) {
			count += set.size();
		}
		size = count;
	}

	/*
	 * A contained used by the builder class to hold the
	 * (ScenarioId,ReplicationId) pairs
	 */
	private static class Scaffold {
		Map<ScenarioId, Set<ReplicationId>> map = new LinkedHashMap<>();
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
		public Builder add(ScenarioId scenarioId, ReplicationId replicationId) {

			if (scenarioId == null) {
				throw new RuntimeException("null scenario id");
			}

			if (replicationId == null) {
				throw new RuntimeException("null replication id");
			}

			Set<ReplicationId> set = scaffold.map.get(scenarioId);

			if (set == null) {
				set = new LinkedHashSet<>();
				scaffold.map.put(scenarioId, set);
			}
			set.add(replicationId);
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
	 * Returns the set of {@link ScenarioId} from the contained
	 * (ScenarioId,ReplicationId) pairs.
	 */
	public Set<ScenarioId> getScenarioIds() {
		return new LinkedHashSet<>(map.keySet());
	}

	/**
	 * Returns the set of {@link ReplicationId} from the contained
	 * (ScenarioId,ReplicationId) pairs.
	 */
	public Set<ReplicationId> getReplicationIds(ScenarioId scenarioId) {
		Set<ReplicationId> result = new LinkedHashSet<>();
		Set<ReplicationId> set = map.get(scenarioId);
		if (set != null) {
			result.addAll(set);
		}
		return result;
	}

	/**
	 * Returns true if and only if the given (ScenarioId,ReplicationId) pair is
	 * contained.
	 */
	public boolean contains(ScenarioId scenarioId, ReplicationId replicationId) {
		Set<ReplicationId> set = map.get(scenarioId);
		if (set == null) {
			return false;
		}
		return set.contains(replicationId);
	}

	/**
	 * Returns the number of (ScenarioId,ReplicationId) pairs contained in this
	 * {@link ExperimentProgressLog}.
	 */
	public int size() {
		return size;
	}

	/**
	 * Returns true if and only if the number of (ScenarioId,ReplicationId)
	 * pairs contained in this {@link ExperimentProgressLog} is zero.
	 */
	public boolean isEmpty() {
		return size == 0;
	}

}
