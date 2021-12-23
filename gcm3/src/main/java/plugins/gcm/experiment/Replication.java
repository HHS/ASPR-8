package plugins.gcm.experiment;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import net.jcip.annotations.Immutable;
import util.SeedProvider;

/**
 * A replication represents an immutable random number generator seed that is to
 * be used across all scenarios within an experiment. Replications are used with
 * scenarios within an experiment to show the effects of random perturbation on
 * each scenario. Each scenario in an experiment is paired with each replication
 * in an experiment to form the simulation runs. Replications are uniquely
 * numbered starting with ID 1.
 *
 * @author Shawn Hatch
 *
 */
@Immutable
public interface Replication {

	/**
	 * Returns a list of Replication having the size indicated by the
	 * replication count. Seeds for each replication are generated using a
	 * random generator using the given seed value. The resulting list of
	 * replications will have positive long-valued identifiers starting with 1.
	 *
	 * @param replicationCount
	 * @param seed
	 * @throws IllegalArgumentException
	 *             if the replication count is negative
	 */
	public static List<Replication> getReplications(final int replicationCount, final long seed) {
		final List<Replication> result = new ArrayList<>();
		if (replicationCount < 0) {
			throw new IllegalArgumentException("negative count");
		}
		final RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(seed);

		for (int i = 1; i <= replicationCount; i++) {
			result.add(new ReplicationImpl(new ReplicationId(i), randomGenerator.nextLong()));
		}
		return result;
	}

	/**
	 * Returns an indexed Replication as generated from the given seed. Seeds
	 * for each replication are generated using a random generator using the
	 * given seed value.
	 *
	 * @param replicationId
	 * @param seed
	 * @throws IllegalArgumentException
	 *             if the replication id is not positive
	 */
	public static Replication getReplication(final int replicationId, final long seed) {
		if (replicationId < 1) {
			throw new IllegalArgumentException("non-positive replication id");
		}
		final RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(seed);

		for (int i = 1; i < replicationId; i++) {
			randomGenerator.nextLong();
		}
		return new ReplicationImpl(new ReplicationId(replicationId), randomGenerator.nextLong());
	}

	/**
	 * Returns the id of this replication. Replications within an experiment are
	 * numbered 1, 2, 3, ..., N.
	 *
	 * @return
	 */
	public ReplicationId getId();

	/**
	 * Returns the immutable random number generator seed that starts a
	 * simulation run.
	 *
	 * @return
	 */
	public Long getSeed();

}
