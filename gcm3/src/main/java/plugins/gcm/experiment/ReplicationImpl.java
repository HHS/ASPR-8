package plugins.gcm.experiment;

import net.jcip.annotations.Immutable;

/**
 * Implementor of {@link ReplicationImpl}. A replication represents an immutable
 * random number generator seed that is to be used across all scenarios within
 * an experiment. Replications are used with scenarios within an experiment to
 * show the effects of random perturbation on each scenario. Each scenario in an
 * experiment is paired with each replication in an experiment to form the
 * simulation runs. Replications are uniquely numbered starting with ID 1.
 *
 * @author Shawn Hatch
 *
 */
@Immutable
public final class ReplicationImpl implements Replication {

	private final ReplicationId id;

	private final Long seed;

	public ReplicationImpl(final ReplicationId id, final Long seed) {
		super();
		this.id = id;
		this.seed = seed;
	}

	/**
	 * Returns the id of this replication. Replications within an experiment are
	 * numbered 1, 2, 3, ..., N.
	 *
	 * @return
	 */
	@Override
	public ReplicationId getId() {
		return id;
	}

	/**
	 * Returns the immutable random number generator seed that starts a
	 * simulation run.
	 *
	 * @return
	 */
	@Override
	public Long getSeed() {
		return seed;
	}

}
